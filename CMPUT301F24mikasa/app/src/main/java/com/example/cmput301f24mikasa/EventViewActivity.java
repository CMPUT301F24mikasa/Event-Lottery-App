package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventViewActivity extends AppCompatActivity implements EventArrayAdapter.OnEventClickListener { // Implement the interface

    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_manage_events); // Ensure this points to the correct layout

        eventListView = findViewById(R.id.event_list_view);
        eventList = new ArrayList<>();

        // Initialize adapter and pass 'this' as the listener
        adapter = new EventArrayAdapter(this, eventList, this);
        eventListView.setAdapter(adapter);

        loadEvents(); // Call to load events from Firestore
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

    @Override
    public void onViewButtonClick(Event event) {
        // Handle the view button action for the specific event
        Log.d("EventViewActivity", "View button clicked for event: " + event.getTitle());
        // Implement your view logic here, such as showing event details
        Toast.makeText(EventViewActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteButtonClick(Event event) {
        // Handle the delete button action for the specific event
        Log.d("EventViewActivity", "Delete button clicked for event: " + event.getTitle());
        // Implement your delete logic here
        Toast.makeText(EventViewActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onEditButtonClick(Event event) {
        // Handle the edit button action for the specific event
        Log.d("EventViewActivity", "Edit button clicked for event: " + event.getTitle());
        // Implement your edit logic here
        Toast.makeText(EventViewActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();

    }
}


/**
package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventViewActivity extends AppCompatActivity {

    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_manage_events);

        eventListView = findViewById(R.id.event_list_view);
        eventList = new ArrayList<>();

        // Initialize adapter
        adapter = new EventArrayAdapter(this, eventList);
        eventListView.setAdapter(adapter);
    }


}
*/
