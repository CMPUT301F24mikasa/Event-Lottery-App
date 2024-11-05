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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsActivity extends AppCompatActivity implements EventArrayAdapter.OnEventClickListener {
    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        NavigatonActivity.setupBottomNavigation(this);

        eventList = new ArrayList<>();

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadEvents(); // Load events from Firestore

        eventListView = findViewById(R.id.event_list_view);
        adapter = new EventArrayAdapter(this, eventList, this, false); // Pass the activity as the listener
        eventListView.setAdapter(adapter);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());
    }

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

    @Override
    public void onViewButtonClick(Event event) {
        Log.d("OrganizerManageEvents", "View button clicked for event with ID: " + event.getEventID());

        // Check if eventID is not null
        if (event.getEventID() != null) {
            // Show a Toast message to confirm eventID is not null


            // Initialize Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a reference to the event document in Firestore using the eventID
            DocumentReference eventRef = db.collection("event").document(event.getEventID());

            // Retrieve the event document to get the event title
            eventRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Get the event title from the document
                        String eventTitle = document.getString("title");

                        if (eventTitle != null) {
                            // Create an Intent to start WaitingListActivity
                            Intent intent = new Intent(ManageEventsActivity.this, WaitingListActivity.class);

                            // Pass eventTitle to the WaitingListActivity
                            intent.putExtra("eventID", event.getEventID());
                            intent.putExtra("eventTitle", eventTitle);

                            // Start WaitingListActivity
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Event title not found", Toast.LENGTH_SHORT).show();
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
                    Log.e("OrganizerManageEvents", "Error deleting event", e);
                    Toast.makeText(ManageEventsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                });
        Toast.makeText(ManageEventsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onEditButtonClick(Event event) {
        // Handle the edit button action for the specific event
        Log.d("OrganizerManageEvents", "Edit button clicked for event: " + event.getTitle());
        // Implement your edit logic here, e.g., open a new activity for editing
    }
}
