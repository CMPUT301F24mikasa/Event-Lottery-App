package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
     * This constructor is required for the Android activity lifecycle.
     */
    public ManageNotificationsActivity() {
        // Constructor is provided by default
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

        // Settings button
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageNotificationsActivity.this, NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // Initialize Reload Button
        ImageButton reloadButton = findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(view -> {
            loadNotifications(); // Reload notifications

            Toast.makeText(ManageNotificationsActivity.this, "Notifications reloaded", Toast.LENGTH_SHORT).show(); // Show toast
        });

        // Setup bottom navigation buttons
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);

        AdminVerification.checkIfAdmin(this, buttonAdmin);
        
        // Bottom Navigation onClick Listeners
        buttonHome.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, MainActivity.class)));
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(ManageNotificationsActivity.this, AdminActivity.class)));
    }




    /**
     * Loads notifications for the current device from Firebase Firestore.
     * Notifications are retrieved based on the device's unique ID and displayed in the ListView.
     */
    // Nikita's code:
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
                            String eventID=document.getString("eventID");
                            Notifications notification = new Notifications(notificationId, text, eventID); // Include ID for reference
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching notifications: ", e);
                    Toast.makeText(this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up a click listener for the notification ListView items.
     * When a notification is clicked, the user is prompted to accept or decline it if the notification is marked as responsive.
     */
    //NEED IMPLEMENT REMOVING NOTIFICATION, OH AND ORDER OF NOTIFICATIONS
    private void setupNotificationClickListener() {
        notificationListView.setOnItemClickListener((parent, view, position, id) -> {
            Notifications selectedNotification = notificationList.get(position);

            // Check if "responsive" is set to "1"
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference notificationRef = db.collection("notification").document(selectedNotification.getNotificationID());

            notificationRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String responsive = documentSnapshot.getString("responsive");

                    if ("1".equals(responsive)) {
                        // Show dialog for Accept/Decline
                        new AlertDialog.Builder(this)
                                .setTitle("Respond to Notification")
                                .setMessage("Would you like to accept or decline?")
                                .setPositiveButton("Accept", (dialog, which) -> {
                                    // Update Firestore with "yes"
                                    updateNotificationResponse(notificationRef, "yes");
                                })
                                .setNegativeButton("Decline", (dialog, which) -> {
                                    // Update Firestore with "no"
                                    updateNotificationResponse(notificationRef, "no");
                                    String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                                    String eventID = selectedNotification.getEventID();
                                    updateSelectedList(eventID, deviceID);
                                    Toast.makeText(this, "You have been removed from the selected list.", Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    } else {
                        Toast.makeText(this, "Notification is not responsive.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("ManageNotificationsActivity", "Error retrieving notification", e);
            });
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
                                Log.d("EventResultList", "User removed from selected list");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("EventResultList", "Error removing user from selected list", e);
                            });
                    //update cancelled list
                    eventRef.update("cancelledEntrants", cancelledEntrants)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("EventResultList", "User added to  from cancelled list");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("EventResultList", "Error adding user to cancelled list", e);
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("EventResultList", "Error retrieving event details", e);
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
                    Log.e("ManageNotificationsActivity", "Error updating response", e);
                    Toast.makeText(this, "Failed to update response", Toast.LENGTH_SHORT).show();
                });
    }
}
