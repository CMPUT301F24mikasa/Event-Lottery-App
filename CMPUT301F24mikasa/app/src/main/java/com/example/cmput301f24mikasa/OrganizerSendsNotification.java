package com.example.cmput301f24mikasa;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that handles sending notifications to selected
 * and non-selected entrants for a specific event.
 * Notifications are sent using Firestore to inform users whether they have
 * been chosen for the event or not.
 */
public class OrganizerSendsNotification extends AppCompatActivity{

    /**
     * Default constructor for OrganizerSendsNotification.
     */
    public OrganizerSendsNotification() {
    }

    ArrayList<UserProfile> selectedEntrants;
    ArrayList<UserProfile> dataList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private CollectionReference eventsRef = db.collection("event");

    /**
     * This method retrieves event details such as
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
        selectedEntrants = new ArrayList<>();
        dataList = new ArrayList<>();
        fetchSelectedList(eventID, eventTitle);
        fetchWaitingList(eventID,eventTitle);

        Intent intent2 = new Intent(OrganizerSendsNotification.this, ListSamplingActivity.class);
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
    private void sendNotificationSelectedList(String eventID, String eventTitle) {
        String eventText = "Congratulations! You were selected for the " + eventTitle;
        for (UserProfile userProfile : selectedEntrants) {

            String deviceID = userProfile.getName();  // Assuming deviceID is stored in the name field of UserProfile

            // Create a new document reference in the notification collection
            DocumentReference documentReference = notificationRef.document();

            String notificationID = documentReference.getId();
            // Prepare data for Firestore
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("responsive", "1");
            notificationData.put("notificationID", notificationID);
            notificationData.put("appeared", "no");

            // Add data to Firestore and show success/failure toasts
            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Notification sent successfully to selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Failed to send notification to selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Sends notifications to the non-selected entrants informing them they were not chosen for the event.
     *
     * @param eventID    the ID of the event
     * @param eventTitle the title of the event
     */
    private void sendNotificationNotAccepted(String eventID, String eventTitle) {
        String eventText = "You were not chosen for " + eventTitle + ". You may be chosen if someone else cancels!";
        for (UserProfile userProfile : dataList) {
            String deviceID = userProfile.getName();  // Assuming deviceID is stored in the name field of UserProfile

            // Create a new document reference in the notification collection
            DocumentReference documentReference = notificationRef.document();
            String notificationID = documentReference.getId();
            // Prepare data for Firestore
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("notificationID", notificationID);
            notificationData.put("appeared", "no");

            // Add data to Firestore and show success/failure toasts
            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Notification sent to non-selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Failed to send notification to non-selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Fetches the list of selected entrants for the event from Firestore.
     * This is done by querying the "event" document by eventID and retrieving the
     * "selectedEntrants" list. It then sends notifications to the selected entrants.
     *
     * @param eventID    the ID of the event
     * @param eventTitle the title of the event
     */
    private void fetchSelectedList(String eventID, String eventTitle) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array from the document
                        List<String> selectedIDs = (List<String>) documentSnapshot.get("selectedEntrants");
                        if (selectedIDs != null && !selectedIDs.isEmpty()) {
                            // For each deviceID in the waiting list, add it as a placeholder user name
                            for (String deviceID : selectedIDs) {
                                // Create a UserProfile with deviceID as the name
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                selectedEntrants.add(userProfile);
                            }
                            sendNotificationSelectedList(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "Waiting list is empty for this event.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Waiting list is empty for this event.", Toast.LENGTH_SHORT).show();
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