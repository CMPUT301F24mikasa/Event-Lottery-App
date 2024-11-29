package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity that displays the waiting list for a specific event.
 * The list is populated by retrieving data from Firebase Firestore.
 * This activity allows the organizer to manage the event's waiting list, view results, and start a sampling activity.
 */
public class WaitingListActivity extends AppCompatActivity {
    private int setting_buttons = 0;
    private ArrayList<UserProfile> dataList;
    private ListView userList;
    private WaitingListArrayAdapter userAdapter; // Updated adapter
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventsRef = db.collection("event");

    /**
     * Called when the activity is created.
     * Sets up the UI, retrieves the event details passed from the previous activity,
     * and loads the waiting list from Firestore.
     *
     * @param savedInstanceState The saved instance state bundle from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waiting_list);

        // Retrieve the event title and eventID passed from OrganizerManageEventsActivity
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");

        // Set the header text with the event title
        TextView headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText("Waiting List For " + eventTitle);

        // Initialize the back button
        Button backButton = findViewById(R.id.back_button_for_waiting);

        // Initialize the list and adapter for waiting list users
        dataList = new ArrayList<>();
        userList = findViewById(R.id.waiting_list_view);
        userAdapter = new WaitingListArrayAdapter(this, dataList, eventID); // Pass eventID to the adapter
        userList.setAdapter(userAdapter);

        Button viewMapButton = findViewById(R.id.view_map_button);
        viewMapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(WaitingListActivity.this, MapActivity.class);
            mapIntent.putExtra("eventID", eventID);
            startActivity(mapIntent);
        });

        // Initialize buttons
        Button sampleButton = findViewById(R.id.sample_button);
        Button viewResults = findViewById(R.id.view_results);

        // Fetch the waiting list from Firestore for the given event ID
        fetchWaitingList(eventID, sampleButton, viewResults);

        sampleButton.setOnClickListener(v -> {
            Toast.makeText(WaitingListActivity.this, "Sampling!", Toast.LENGTH_SHORT).show();
            Intent samplingIntent = new Intent(WaitingListActivity.this, ListSamplingActivity.class);
            samplingIntent.putExtra("eventID", eventID);
            samplingIntent.putExtra("eventTitle", eventTitle);
            samplingIntent.putExtra("size", dataList.size());
            startActivity(samplingIntent);
        });

        backButton.setOnClickListener(v -> {
            Intent samplingIntent = new Intent(WaitingListActivity.this, ManageEventsActivity.class);
            startActivity(samplingIntent);
        });

        viewResults.setOnClickListener(v -> {
            Intent resultsIntent = new Intent(WaitingListActivity.this, EventResultList.class);
            resultsIntent.putExtra("eventID", eventID);
            resultsIntent.putExtra("eventTitle", eventTitle);
            resultsIntent.putExtra("size", dataList.size());
            startActivity(resultsIntent);
        });

        Button customNotify = findViewById(R.id.custom_notify_waiting_list);
        customNotify.setOnClickListener(view -> {
            Intent resultsIntent = new Intent(WaitingListActivity.this, CustomToAllActivity.class);
            resultsIntent.putExtra("eventID", eventID);
            resultsIntent.putExtra("eventTitle", eventTitle);
            resultsIntent.putExtra("listType", 1);
            startActivity(resultsIntent);
        });
    }

    /**
     * Fetches the waiting list for the given event from Firestore.
     * Updates the UI to display the waiting list and adjust button visibility based on the event's status.
     *
     * @param eventID The ID of the event whose waiting list is to be fetched.
     * @param sampleButton The button to trigger sampling of the waiting list.
     * @param viewResults The button to view the results after sampling.
     */
    void fetchWaitingList(String eventID, Button sampleButton, Button viewResults) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> waitingListIds = (List<String>) documentSnapshot.get("waitingList");
                        String alreadySampled = documentSnapshot.getString("alreadySampled");
                        String geoLocationRequired = documentSnapshot.getString("geo-location required");
                        Map<String, String> locationList = (Map<String, String>) documentSnapshot.get("LocationList");

                        if (alreadySampled != null) {
                            setting_buttons = Integer.parseInt(alreadySampled);
                            if (setting_buttons == 0) {
                                viewResults.setVisibility(View.GONE);
                                sampleButton.setVisibility(View.VISIBLE);
                            } else {
                                viewResults.setVisibility(View.VISIBLE);
                                sampleButton.setVisibility(View.GONE);
                            }
                        }

                        CollectionReference usersRef = db.collection("users");
                        if (waitingListIds != null && !waitingListIds.isEmpty()) {
                            dataList.clear();
                            for (String deviceID : waitingListIds) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setDeviceId(deviceID);

                                if ("yes".equalsIgnoreCase(geoLocationRequired) && locationList != null) {
                                    String location = locationList.get(deviceID);
                                    userProfile.setLocation(location != null ? location : "Unknown Location");
                                } else {
                                    userProfile.setLocation("N/A");
                                }

                                usersRef.document(deviceID).get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            if (userSnapshot.exists()) {
                                                String userName = userSnapshot.getString("name");
                                                userProfile.setName(userName != null ? userName : "Unknown User");
                                            } else {
                                                userProfile.setName("Unknown User");
                                            }
                                            dataList.add(userProfile);
                                            userAdapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Waiting list is empty for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details.", Toast.LENGTH_SHORT).show();
                });
    }

}
