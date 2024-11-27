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
 * Combines functionality for managing notification sending and dynamic list fetching
 * based on the intent type (1 = Waiting List, 2 = Selected Entrants, 3 = Final Entrants).
 */
public class CustomToAllActivity extends AppCompatActivity {

    private String eventID;
    private String eventTitle;
    private int listType;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private CollectionReference eventsRef = db.collection("event");

    private List<UserProfile> entrantsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve event details and list type from the Intent
        Intent intent = getIntent();
        eventID = intent.getStringExtra("eventID");
        eventTitle = intent.getStringExtra("eventTitle");
        listType = intent.getIntExtra("listType", 1); // Default to 1 (Waiting List)

        // Fetch the appropriate list and show the popup
        fetchEntrantsList(eventID, eventTitle, listType);
    }

    /**
     * Fetches the appropriate list (waiting list, selected entrants, or final entrants) for the event from Firestore.
     * Once the list is fetched, it displays the popup dialog.
     */
    private void fetchEntrantsList(String eventID, String eventTitle, int listType) {
        String listKey; // Firestore document key for the list to fetch
        switch (listType) {
            case 1:
                listKey = "waitingList";
                break;
            case 2:
                listKey = "selectedEntrants";
                break;
            case 3:
                listKey = "finalEntrants";
                break;
            default:
                Toast.makeText(this, "Invalid list type", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }

        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the list from the document
                        List<String> entrantIDs = (List<String>) documentSnapshot.get(listKey);
                        if (entrantIDs != null && !entrantIDs.isEmpty()) {
                            for (String deviceID : entrantIDs) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Using deviceID as a placeholder for the name
                                entrantsList.add(userProfile);
                            }
                            sendNotificationDialog(eventID, eventTitle, listType);
                        } else {
                            Toast.makeText(this, "The list is empty for this event.", Toast.LENGTH_SHORT).show();
                            navigateBack(listType);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        navigateBack(listType);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    navigateBack(listType);
                });
    }

    /**
     * Displays a popup dialog allowing the organizer to edit the notification message
     * and confirm sending notifications to all users in the list.
     */
    private void sendNotificationDialog(String eventID, String eventTitle, int listType) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);

        TextView titleTextView = new TextView(this);
        titleTextView.setText("Send Notifications");
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setTextSize(20);
        dialogLayout.addView(titleTextView);

        TextView eventDescription = new TextView(this);
        eventDescription.setText("You are about to send notifications to all users in the list for \"" + eventTitle + "\".");
        eventDescription.setPadding(0, 20, 0, 20);
        dialogLayout.addView(eventDescription);

        EditText messageInput = new EditText(this);
        messageInput.setHint("Enter notification message here...");
        messageInput.setText("You have been selected for the event \"" + eventTitle + "\". Stay tuned for updates!");
        messageInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageInput.setPadding(0, 20, 0, 20);
        dialogLayout.addView(messageInput);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(dialogLayout);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(scrollView);
        dialogBuilder.setPositiveButton("Send", (dialog, which) -> {
            String customMessage = messageInput.getText().toString().trim();
            if (customMessage.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
            } else {
                sendNotifications(eventID, customMessage, listType);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(this, "Action cancelled.", Toast.LENGTH_SHORT).show();
            navigateBack(listType);
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Sends notifications to all users in the list.
     */
    private void sendNotifications(String eventID, String customMessage, int listType) {
        if (entrantsList.isEmpty()) {
            Toast.makeText(this, "No users to notify.", Toast.LENGTH_SHORT).show();
            navigateBack(listType);
            return;
        }

        for (UserProfile user : entrantsList) {
            DocumentReference notificationDoc = notificationRef.document();
            String notificationID = notificationDoc.getId();

            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", user.getName());
            notificationData.put("eventID", eventID);
            notificationData.put("text", customMessage);
            notificationData.put("notificationID", notificationID);
            notificationData.put("appeared", "no");

            notificationDoc.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification sent to: " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send notification to: " + user.getName(), Toast.LENGTH_SHORT).show();
                    });
        }

        Toast.makeText(this, "All notifications have been sent!", Toast.LENGTH_SHORT).show();
        navigateBack(listType);
    }

    /**
     * Navigates back to the appropriate activity based on the list type.
     */
    private void navigateBack(int listType) {
        Intent intent;
        switch (listType) {
            case 1:
                intent = new Intent(this, WaitingListActivity.class);
                break;
            case 2:
                intent = new Intent(this, EventResultList.class);
                break;
            case 3:
                intent = new Intent(this, EventFinalListActivity.class);
                break;
            default:
                Toast.makeText(this, "Invalid list type", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }
        intent.putExtra("eventID", eventID);
        intent.putExtra("eventTitle", eventTitle);
        startActivity(intent);
        finish();
    }
}
