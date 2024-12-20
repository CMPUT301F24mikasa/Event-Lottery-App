package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ManageNotificationsActivity is responsible for displaying and managing the list of notifications for the user.
 * It fetches notifications from Firebase Firestore based on the device's ID, displays them in a ListView,
 * and allows the user to interact with them by responding to notifications.
 */
public class ManageNotificationsActivity extends AppCompatActivity {
    private ListView notificationListView;
    private NotificationArrayAdapter adapter;
    private List<Notifications> notificationList;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    /**
     * Default constructor for ManageNotificationsActivity.
     */
    public ManageNotificationsActivity() {
    }


    /**
     * Initializes the activity, sets up the ListView to display notifications,
     * and loads the notifications from Firebase Firestore if they are enabled.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        // Initialize notification list and the adapter for the list of notifications
        notificationList = new ArrayList<>();
        notificationListView = findViewById(R.id.notification_list_view);
        adapter = new NotificationArrayAdapter(this, notificationList);
        notificationListView.setAdapter(adapter);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load notifications if enabled
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        if (notificationsEnabled) {
            loadNotifications();
            setupNotificationClickListener();
        } else {
            Toast.makeText(this, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }

        // Initialize and set click listener for Settings button
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageNotificationsActivity.this, NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // Initialize Reload Button
        ImageButton reloadButton = findViewById(R.id.reloadButton);

        // Check notifications enabled status and toggle visibility of the reload button
        updateReloadButtonVisibility(reloadButton);

        // OnClickListener for the reload button
        reloadButton.setOnClickListener(view -> {
            // Load notifications only if enabled
            if (notificationsEnabled) {
                loadNotifications();
                Toast.makeText(ManageNotificationsActivity.this, "Notifications reloaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageNotificationsActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Add a listener to the SharedPreferences to update the visibility dynamically
        sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPrefs, key) -> {
            if (KEY_NOTIFICATIONS_ENABLED.equals(key)) {
                updateReloadButtonVisibility(reloadButton);
            }
        });


        // Setup bottom navigation buttons
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Initialize Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        AdminVerification.checkIfAdmin(this, buttonAdmin, deviceId, firestore);

        // Bottom Navigation onClick Listeners
        buttonHome.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, MainActivity.class)));
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, AdminActivity.class)));
    }

    /**
     * Updates the visibility of the reload button based on the notification settings.
     * If notifications are enabled, the reload button is made visible; otherwise, it is hidden.
     *
     * @param reloadButton The ImageButton representing the reload button whose visibility is updated if notifications are enabled or disabled.
     */
    private void updateReloadButtonVisibility(ImageButton reloadButton) {
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        if (notificationsEnabled) {
            reloadButton.setVisibility(View.VISIBLE);
        } else {
            reloadButton.setVisibility(View.GONE);
        }
    }


    /**
     * Loads notifications for the current device from Firebase Firestore.
     * Notifications are retrieved based on the device's unique ID and displayed in the ListView.
     */
    private void loadNotifications() {
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationList.clear();

        db.collection("notification")
                .whereEqualTo("deviceID", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No notifications found for your device.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String text = document.getString("text");
                            String notificationId = document.getId();
                            String eventID = document.getString("eventID");
                            Notifications notification = new Notifications(notificationId, text, eventID); // Include ID for reference
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up a click listener for the notification ListView items.
     * When a notification is clicked, the user is prompted to accept or decline it if the notification is marked as responsive.
     */
    private void setupNotificationClickListener() {
        notificationListView.setOnItemClickListener((parent, view, position, id) -> {
            Notifications selectedNotification = notificationList.get(position);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference notificationRef = db.collection("notification").document(selectedNotification.getNotificationID());

            notificationRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String responsive = documentSnapshot.getString("responsive");

                    if ("1".equals(responsive)) {
                        // Show Accept/Decline dialog for responsive notifications
                        showAcceptDeclineDialog(notificationRef, selectedNotification);
                    } else {
                        // Show Delete/Cancel dialog for non-responsive notifications
                        showDeleteDialog(selectedNotification);
                    }
                }
            }).addOnFailureListener(e -> {
            });
        });
    }
    private void showAcceptDeclineDialog(DocumentReference notificationRef, Notifications selectedNotification) {
        new AlertDialog.Builder(this)
                .setTitle("Respond to Notification")
                .setMessage("Would you like to accept or decline this invitation?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    // Update Firestore with "yes"
                    updateNotificationResponse(notificationRef, "yes");
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    // Update Firestore with "no"
                    updateNotificationResponse(notificationRef, "no");

                    // Handle removing from selected list if it's an invitation
                    String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    String eventID = selectedNotification.getEventID();
                    updateSelectedList(eventID, deviceID);

                    Toast.makeText(this, "You have been removed from the selected list.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    private void showDeleteDialog(Notifications selectedNotification) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notification")
                .setMessage("This notification has already been responded to. Do you want to delete it?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the notification from Firestore
                    deleteNotification(selectedNotification.getNotificationID());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void deleteNotification(String notificationID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notification").document(notificationID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Notification deleted successfully.", Toast.LENGTH_SHORT).show();
                    // Reload notifications after deletion
                    loadNotifications();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete notification.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the list of selected entrants for a specific event when a user declines a notification.
     * The device ID is removed from the "selectedEntrants" list and added to the "cancelledEntrants" list.
     *
     * @param eventID The ID of the event associated with the notification.
     * @param deviceID The ID of the device that is being removed from the event's selected entrants.
     */
    private void updateSelectedList(String eventID, String deviceID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("event").document(eventID);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get the current selectedEntrants list
                List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");
                List<String> cancelledEntrants = (List<String>) documentSnapshot.get("cancelledEntrants");
                if (selectedEntrants != null && selectedEntrants.contains(deviceID)) {
                    // Remove the deviceID from selectedEntrants list
                    selectedEntrants.remove(deviceID);
                    assert cancelledEntrants != null;
                    cancelledEntrants.add(deviceID);
                    // Update the event document with the new selectedEntrants list
                    eventRef.update("selectedEntrants", selectedEntrants)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                    //update cancelled list
                    eventRef.update("cancelledEntrants", cancelledEntrants)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                }
            }
        }).addOnFailureListener(e -> {
        });
    }

    /**
     * Updates the response to a notification in Firebase Firestore.
     * The response is either "yes" (accepted) or "no" (declined).
     *
     * @param notificationRef A reference to the notification document in Firestore.
     * @param response The response ("yes" or "no") to the notification.
     */
    private void updateNotificationResponse(DocumentReference notificationRef, String response) {
        notificationRef.update("responsive", response)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Response updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update response", Toast.LENGTH_SHORT).show();
                });
    }
}
