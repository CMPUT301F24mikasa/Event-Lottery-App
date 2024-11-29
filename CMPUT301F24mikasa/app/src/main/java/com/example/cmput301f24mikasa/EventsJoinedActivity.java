package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a list of events the current device has signed up for.
 * It fetches the events from Firestore and checks if the device's unique ID is present
 * in the "waitingList", "selectedEntrants", or "finalEntrants" lists for each event.
 * If there are matching events, they are displayed in a ListView using a custom adapter.
 * If no events are found, a message is shown to the user.
 *
 * @version 1.0
 */
public class EventsJoinedActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private List<QueryDocumentSnapshot> eventsJoined; // List to store matched event snapshots
    private EventsJoinedAdapter adapter; // Custom adapter to display detailed events

    /**
     * Default constructor for EventsJoinedActivity.
     */
    public EventsJoinedActivity() {
    }


    /**
     * Called when the activity is first created. Initializes Firestore, retrieves the device ID,
     * sets up the ListView and adapter for displaying joined events, and triggers fetching events
     * from the Firestore database. Also, sets up a back button to navigate to the previous screen.
     *
     * @param savedInstanceState If the activity is re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_signed_up_for);

        db = FirebaseFirestore.getInstance();
        deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        eventsJoined = new ArrayList<>();

        ListView listView = findViewById(R.id.list_events_joined);
        adapter = new EventsJoinedAdapter(this, eventsJoined);
        listView.setAdapter(adapter);

        fetchEventsSignedUpFor();

        // Back button functionality
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    /**
     * Fetches the events from Firestore and checks if the device ID is present in any of the following lists:
     * "waitingList", "selectedEntrants", or "finalEntrants". If the device ID is found in any list,
     * the event is added to the eventsJoined list, which is then displayed in the ListView.
     * Shows a message if no events were found for the device.
     */
    private void fetchEventsSignedUpFor() {
        db.collection("event").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Initialize the lists if they exist; otherwise, set them as empty lists
                        List<String> waitingList = document.contains("waitingList") ?
                                (List<String>) document.get("waitingList") : new ArrayList<>();
                        List<String> selectedEntrants = document.contains("selectedEntrants") ?
                                (List<String>) document.get("selectedEntrants") : new ArrayList<>();
                        List<String> finalEntrants = document.contains("finalEntrants") ?
                                (List<String>) document.get("finalEntrants") : new ArrayList<>();

                        // Check if deviceId is in any of the three lists
                        if (waitingList.contains(deviceId) || selectedEntrants.contains(deviceId) || finalEntrants.contains(deviceId)) {
                            eventsJoined.add(document);
                        }
                    }
                    if (eventsJoined.isEmpty()) {
                        findViewById(R.id.no_events_text).setVisibility(View.VISIBLE);
                        Toast.makeText(this, "No events found for this device", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching events: ", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}
