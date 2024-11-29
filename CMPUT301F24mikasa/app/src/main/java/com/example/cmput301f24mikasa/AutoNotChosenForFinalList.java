package com.example.cmput301f24mikasa;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that handles sending notifications to selected and non-selected entrants
 * for a specific event. Notifications are sent using Firestore to inform users
 * whether they have been chosen for the event or not.
 */
public class AutoNotChosenForFinalList extends AppCompatActivity{

    /**
     * Default constructor for OrganizerSendsNotification.
     * This constructor is required for the Android activity lifecycle.
     */
    public AutoNotChosenForFinalList() {
        // Constructor is provided by default
    }

    ArrayList<UserProfile> nonRespondedEntrants;
    ArrayList<UserProfile> dataList;
    private int totalNotifications=0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private CollectionReference eventsRef = db.collection("event");

    /**
     * Called when the activity is created. It retrieves event details such as
     * the event ID and title, then fetches both the selected entrants and
     * the waiting list for the event.
     *
     * @param savedInstanceState the previous instance's state (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Intent intent = getIntent();
        String eventID=intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");
        nonRespondedEntrants = new ArrayList<>();
        dataList = new ArrayList<>();
        fetchNonRespondedList(eventID, eventTitle);
        fetchWaitingList(eventID,eventTitle);

        Intent intent2 = new Intent(AutoNotChosenForFinalList.this, EventResultList.class);
        intent2.putExtra("eventID", eventID);
        intent2.putExtra("eventTitle", eventTitle);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
        //finish();
    }

    /**
     * Sends notifications to the selected entrants informing them they were chosen for the event.
     * The notification includes the event ID and text.
     *
     * @param eventID    the ID of the event
     * @param eventTitle the title of the event
     */
    private void sendNotificationNonRespondedSelectedList(String eventID, String eventTitle) {
        String eventText = "You did not respond in time! Final list has been created! You were NOT selected for the " + eventTitle;
        totalNotifications = nonRespondedEntrants.size();
        for (UserProfile userProfile : nonRespondedEntrants) {
            String deviceID = userProfile.getName();  // Assuming deviceID is stored in the name field of UserProfile

            DocumentReference documentReference = notificationRef.document();
            String notificationID = documentReference.getId();

            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("notificationID", notificationID);
            notificationData.put("appeared", "no");

            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification sent to: " + deviceID, Toast.LENGTH_SHORT).show();
                        checkAndClearLists(--totalNotifications, eventID); // Decrement and check completion
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send notification to: " + deviceID, Toast.LENGTH_SHORT).show();
                        checkAndClearLists(--totalNotifications, eventID); // Decrement and check even on failure
                    });
        }
    }

    private void fetchNonRespondedList(String eventID, String eventTitle) {
        // Query the notification collection for notifications with responsive = "1"
        notificationRef.whereEqualTo("eventID", eventID)
                .whereEqualTo("responsive", "1")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<String> deviceIDsToNotify = new ArrayList<>();

                        // Collect all deviceIDs from matching documents (those who responded with "1")
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String deviceID = documentSnapshot.getString("deviceID");
                            if (deviceID != null) {
                                deviceIDsToNotify.add(deviceID);
                            }
                        }

                        if (!deviceIDsToNotify.isEmpty()) {
                            // Create UserProfile objects for each non-responding entrant

                            for (String deviceID : deviceIDsToNotify) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID);  // Set deviceID as a placeholder for name
                                nonRespondedEntrants.add(userProfile);
                            }

                            // Send notifications to these non-responding entrants
                            sendNotificationNonRespondedSelectedList(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "No non-responding entrants found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No matching notifications found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error querying notifications.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    /**
     * Sends notifications to the non-selected entrants informing them they were not chosen for the event.
     *
     * @param eventID    the ID of the event
     * @param eventTitle the title of the event
     */
    private void sendNotificationNotAccepted(String eventID, String eventTitle) {
        String eventText = "Final List has been created for " + eventTitle + " and you are not chosen.";
        totalNotifications = dataList.size();
        for (UserProfile userProfile : dataList) {
            String deviceID = userProfile.getName();

            DocumentReference documentReference = notificationRef.document();
            String notificationID = documentReference.getId();

            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("notificationID", notificationID);
            notificationData.put("appeared", "no");

            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification sent to: " + deviceID, Toast.LENGTH_SHORT).show();
                        checkAndClearLists(--totalNotifications, eventID); // Decrement and check completion
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send notification to: " + deviceID, Toast.LENGTH_SHORT).show();
                        checkAndClearLists(--totalNotifications, eventID); // Decrement and check even on failure
                    });
        }
    }

    private void checkAndClearLists(int remainingNotifications, String eventID) {
        if (remainingNotifications <= 0) {
            // Clear Firestore lists once all notifications are sent
            eventsRef.document(eventID)
                    .update("selectedEntrants", new ArrayList<>(), "waitingList", new ArrayList<>())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Lists cleared successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to clear lists.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }


    /**
     * Fetches the list of non-selected entrants (waiting list) for the event from Firestore.
     * This is done by querying the "event" document by eventID and retrieving the
     * "waitingList" array. It then sends notifications to the non-selected entrants.
     *
     * @param eventID    the ID of the event
     * @param eventTitle the title of the event
     */
    private void fetchWaitingList(String eventID, String eventTitle) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array and alreadySampled from the document
                        List<String> waitingListIds = (List<String>) documentSnapshot.get("waitingList");

                        if (waitingListIds != null && !waitingListIds.isEmpty()) {
                            // Populate dataList with deviceIDs from waitingList
                            for (String deviceID : waitingListIds) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                dataList.add(userProfile);
                            }
                            sendNotificationNotAccepted(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "Waiting list iISSSSSS empty for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}