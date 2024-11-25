package com.example.cmput301f24mikasa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
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
 * Combines functionality for managing the waiting list and sending notifications
 * in a single popup-like activity without using an XML layout.
 */
public class CustomToWaitingList extends AppCompatActivity {

    private String eventID;
    private String eventTitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private DocumentReference eventRef;

    private List<String> waitingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve event details from the Intent
        Intent intent = getIntent();
        eventID = intent.getStringExtra("eventID");
        eventTitle = intent.getStringExtra("eventTitle");
        eventRef = db.collection("event").document(eventID);

        // Fetch the waiting list and show the popup
        fetchWaitingList();
    }

    /**
     * Fetches the waiting list for the event from Firestore.
     * Once the waiting list is fetched, it displays the popup dialog.
     */
    private void fetchWaitingList() {
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitingListFromFirestore = (List<String>) documentSnapshot.get("waitingList");
                if (waitingListFromFirestore != null) {
                    waitingList.clear();
                    waitingList.addAll(waitingListFromFirestore);
                }
                showNotificationDialog();
            } else {
                Toast.makeText(this, "Event not found!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch waiting list.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Displays a popup dialog allowing the organizer to edit the notification message
     * and confirm sending notifications to all users in the waiting list.
     */
    private void showNotificationDialog() {
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
        eventDescription.setText("You are about to send notifications to all users on the waiting list for \"" + eventTitle + "\".");
        eventDescription.setPadding(0, 20, 0, 20);
        dialogLayout.addView(eventDescription);

        // Add EditText for custom message
        EditText messageInput = new EditText(this);
        messageInput.setHint("Enter notification message here...");
        messageInput.setText("You are on the waiting list for the event \"" + eventTitle + "\". Stay tuned for updates!");
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
                sendNotifications(customMessage);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(this, "Action cancelled.", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Sends notifications to all users in the waiting list.
     * Each notification contains the custom message provided by the organizer.
     *
     * @param customMessage The custom notification message to send.
     */
    private void sendNotifications(String customMessage) {
        if (waitingList.isEmpty()) {
            Toast.makeText(this, "No users in the waiting list to notify.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        for (String deviceID : waitingList) {
            // Create a new notification document
            DocumentReference notificationDoc = notificationRef.document();
            String notificationID = notificationDoc.getId();

            // Prepare the notification data
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", customMessage);
            notificationData.put("notificationID", notificationID);

            // Save the notification to Firestore
            notificationDoc.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification sent to: " + deviceID, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send notification to: " + deviceID, Toast.LENGTH_SHORT).show();
                    });
        }

        // Finish the activity after notifications are sent
        Toast.makeText(this, "All notifications have been sent!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
