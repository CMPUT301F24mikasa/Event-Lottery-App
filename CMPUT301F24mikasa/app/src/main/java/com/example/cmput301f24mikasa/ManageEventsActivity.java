package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301f24mikasa.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ManageEventsActivity is responsible for managing and displaying events created by the current device.
 * It allows the user to view, edit, and delete events, as well as navigate to different event-related activities
 * (e.g., WaitingListActivity, EventFinalListActivity).
 * This activity interacts with Firebase Firestore to retrieve, modify, and delete event data.
 */
public class ManageEventsActivity extends AppCompatActivity implements EventArrayAdapter.OnEventClickListener {
    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    /**
     * Default constructor for ManageEventsActivity.
     */
    public ManageEventsActivity() {
    }

    /**
     * Initializes the activity, loads events from Firestore, and sets up the event list view.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        eventList = new ArrayList<>();

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadEvents(); // Load events from Firestore

        eventListView = findViewById(R.id.event_list_view);
        adapter = new EventArrayAdapter(this, eventList, this); // Pass the activity as the listener
        eventListView.setAdapter(adapter);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(view -> {
            Intent resultsIntent = new Intent(ManageEventsActivity.this, EventsActivity.class);
            startActivity(resultsIntent);
        });
    }

    /**
     * Loads the events created by the current device from Firestore and updates the event list.
     * It filters events by the device ID and adds them to the list.
     */
    private void loadEvents() {
        // Retrieve the device ID
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear the event list before querying
        eventList.clear();

        // Query Firestore for events created by this device
        db.collection("event")
                .whereEqualTo("deviceID", deviceId) // Filter by device ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if the query returned any documents
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Handle case where no events are found for the device
                        // e.g., display a message to the user
                    } else {
                        // Loop through the results and add them to the event list
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            eventList.add(event);
                        }
                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                    Log.e("FirestoreError", "Error fetching events: ", e);
                });
    }

    /**
     * Handles the view button click for an event. Navigates to either the WaitingListActivity or
     * EventFinalListActivity based on the event's finalListField status.
     *
     * @param event The event object for which the view button was clicked.
     */
    @Override
    public void onViewButtonClick(Event event) {
        Log.d("OrganizerManageEvents", "View button clicked for event with ID: " + event.getEventID());

        // Check if eventID is not null
        if (event.getEventID() != null) {
            // Initialize Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a reference to the event document in Firestore using the eventID
            DocumentReference eventRef = db.collection("event").document(event.getEventID());

            // Retrieve the event document to check the finalListField and event title
            eventRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Get the event title from the document
                        String eventTitle = document.getString("title");
                        String finalListField = document.getString("finalListCreated");

                        // Check if finalListField is set to "1"
                        if ("1".equals(finalListField)) {
                            // If finalListField is "1", navigate to EventFinalListActivity
                            Intent intent = new Intent(ManageEventsActivity.this, EventFinalListActivity.class);
                            intent.putExtra("eventID", event.getEventID());
                            intent.putExtra("eventTitle", eventTitle);  // Optional: Pass the event title as well
                            startActivity(intent);
                        } else {
                            // If finalListField is not "1", navigate to WaitingListActivity as usual
                            if (eventTitle != null) {
                                Intent intent = new Intent(ManageEventsActivity.this, WaitingListActivity.class);
                                intent.putExtra("eventID", event.getEventID());
                                intent.putExtra("eventTitle", eventTitle);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Event title not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Event document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve event title", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Show a Toast message if eventID is null
            Toast.makeText(this, "CRITICAL OMEGA ALPHA Failure: Event ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the delete button click for an event. Deletes the event from Firestore and updates the event list.
     *
     * @param event The event object to be deleted.
     */
    @Override
    public void onDeleteButtonClick(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event").document(event.getEventID()) // Ensure `Event` has an `id` field set with Firestore document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    eventList.remove(event);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ManageEventsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageEventsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                });
        Toast.makeText(ManageEventsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the edit button click for an event. Initiates the logic to edit the event details.
     *
     * @param event The event object for which the edit button was clicked.
     */
    @Override
    public void onEditButtonClick(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event").document(event.getEventID());
        // Check if the eventID is not null
        if (event.getEventID() != null) {
            Intent intent = new Intent(ManageEventsActivity.this, EditEventActivity.class);
            intent.putExtra("eventID", event.getEventID());

            startActivity(intent);
        } else {
            // Show a Toast message if eventID is null
            Toast.makeText(this, "Event ID is null, unable to edit event.", Toast.LENGTH_SHORT).show();
        }
    }
}