package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OrganizerManageEventsActivity extends AppCompatActivity {
    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_manage_events);

        eventList = new ArrayList<>();

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear the event list before querying
        eventList.clear();

        loadEvents();
        eventListView = findViewById(R.id.event_list_view);
        adapter = new EventArrayAdapter(this, eventList);
        eventListView.setAdapter(adapter);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());
    }
    private void loadEvents() {
        // Retrieve the device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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
}