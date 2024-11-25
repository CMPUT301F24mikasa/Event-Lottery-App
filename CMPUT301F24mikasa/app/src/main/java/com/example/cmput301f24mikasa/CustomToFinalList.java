package com.example.cmput301f24mikasa;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Combines functionality for managing the selected list and sending notifications
 * in a single popup-like activity without using an XML layout.
 */
public class CustomToFinalList extends AppCompatActivity {

    private String eventID;
    private String eventTitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private CollectionReference eventsRef = db.collection("event");

    private ArrayList<UserProfile> fetchFinalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve event details from the Intent
        Intent intent = getIntent();
        eventID = intent.getStringExtra("eventID");
        eventTitle = intent.getStringExtra("eventTitle");

        // Fetch the selected entrants list and show the popup
        fetchFinalList(eventID, eventTitle);
    }

    /**
     * Fetches the selected entrants for the event from Firestore.
     * Once the list is fetched, it displays the popup dialog.
     */
    private void fetchFinalList(String eventID, String eventTitle) {
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the selectedEntrants array from the document
                        List<String> selectedIDs = (List<String>) documentSnapshot.get("finalEntrants");
                        if (selectedIDs != null && !selectedIDs.isEmpty()) {
                            // For each deviceID, create a placeholder UserProfile object
                            for (String deviceID : selectedIDs) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Using deviceID as a placeholder for the name
                                fetchFinalList.add(userProfile);
                            }
                            sendNotificationDialog(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "Selected list is empty for this event.", Toast.LENGTH_SHORT).show();
                            navigateBackToEventResultList();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        navigateBackToEventResultList();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    navigateBackToEventResultList();
                });
    }

    /**
     * Displays a popup dialog allowing the organizer to edit the notification message
     * and confirm sending notifications to all users in the selected entrants list.
     */
    private void sendNotificationDialog(String eventID, String eventTitle) {
        // Create the dialog layout programmatically
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);

        // Add title text
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Send Notifications");
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setTextSize(20);
        dialogLayout.addView(titleTextView);

        // Add event description
        TextView eventDescription = new TextView(this);
        eventDescription.setText("You are about to send notifications to all selected entrants for \"" + eventTitle + "\".");
        eventDescription.setPadding(0, 20, 0, 20);
        dialogLayout.addView(eventDescription);

        // Add EditText for custom message
        EditText messageInput = new EditText(this);
        messageInput.setHint("Enter notification message here...");
        messageInput.setText("You have been selected for the event \"" + eventTitle + "\". Stay tuned for updates!");
        messageInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageInput.setPadding(0, 20, 0, 20);
        dialogLayout.addView(messageInput);

        // Create a ScrollView for dynamic height handling if needed
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(dialogLayout);

        // Build the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(scrollView);
        dialogBuilder.setPositiveButton("Send", (dialog, which) -> {
            String customMessage = messageInput.getText().toString().trim();
            if (customMessage.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
            } else {
                sendNotifications(eventID, customMessage);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(this, "Action cancelled.", Toast.LENGTH_SHORT).show();
            navigateBackToEventResultList();
        });

        // Show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Sends notifications to all users in the selected entrants list.
     * Each notification contains the custom message provided by the organizer.
     *
     * @param eventID       The event ID to associate with the notification.
     * @param customMessage The custom notification message to send.
     */
    private void sendNotifications(String eventID, String customMessage) {
        if (fetchFinalList.isEmpty()) {
            Toast.makeText(this, "No selected entrants to notify.", Toast.LENGTH_SHORT).show();
            navigateBackToEventResultList();
            return;
        }

        for (UserProfile user : fetchFinalList) {
            // Create a new notification document
            DocumentReference notificationDoc = notificationRef.document();
            String notificationID = notificationDoc.getId();

            // Prepare the notification data
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", user.getName()); // Using deviceID as placeholder
            notificationData.put("eventID", eventID);
            notificationData.put("text", customMessage);
            notificationData.put("notificationID", notificationID);

            // Save the notification to Firestore
            notificationDoc.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification sent to: " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send notification to: " + user.getName(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Navigate back to EventResultList after notifications are sent
        Toast.makeText(this, "All notifications have been sent!", Toast.LENGTH_SHORT).show();
        navigateBackToEventResultList();
    }

    /**
     * Navigates back to the EventResultList activity.
     */
    private void navigateBackToEventResultList() {
        Intent intent = new Intent(this, EventFinalListActivity.class);
        intent.putExtra("eventID", eventID); // Pass eventID if needed
        intent.putExtra("eventTitle", eventTitle); // Pass eventTitle if needed
        startActivity(intent);
        finish(); // Close the current activity
    }
}
