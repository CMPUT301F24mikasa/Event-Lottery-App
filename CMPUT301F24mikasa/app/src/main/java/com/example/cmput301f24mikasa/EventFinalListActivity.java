package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * EventFinalListActivity displays the final list of entrants for a specific event.
 * The class retrieves the list of final entrants from Firestore and displays them
 * in a ListView. It also allows navigation back to the ManageEventsActivity.
 *
 * @version 1.0
 * @since 2024-11-08
 */
public class EventFinalListActivity extends AppCompatActivity {
    Button notifyButton;
    private ListView finalListView;
    private UserProfileArrayAdapter finalEntrantsAdapter;
    private ArrayList<UserProfile> finalEntrantsList = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventID;  // Pass the eventID from the previous activity

    /**
     * Default constructor for EventFinalListActivity.
     */
    public EventFinalListActivity() {}

    /**
     * Initializes the activity, sets up the ListView and back button, retrieves
     * the eventID and title passed from the previous activity, and fetches the final
     * entrants list from Firestore.
     *
     * @param savedInstanceState If the activity is being reinitialized after being
     * previously shut down, this Bundle contains the most recent data supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_final_list);

        finalListView = findViewById(R.id.final_list_view);
        db = FirebaseFirestore.getInstance();

        // Get the eventID passed from the previous activity
        eventID = getIntent().getStringExtra("eventID");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        // Set the header text with the event title
        TextView headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText("Final List For " + eventTitle);

        // Fetch the finalEntrants list from Firestore
        fetchFinalEntrants(eventID);

        // Set the adapter for the ListView
        finalEntrantsAdapter = new UserProfileArrayAdapter(this, finalEntrantsList);
        finalListView.setAdapter(finalEntrantsAdapter);

        // Back button functionality
        findViewById(R.id.back_button_for_final_list).setOnClickListener(view -> {
            // Create an intent to navigate to ManageEventsActivity
            Intent intent = new Intent(EventFinalListActivity.this, ManageEventsActivity.class);
            startActivity(intent);  // Start the activity
            finish();  // Optionally finish the current activity to remove it from the stack
        });
        notifyButton = findViewById(R.id.custom_notify_final_list);
        notifyButton.setOnClickListener(new View.OnClickListener() {

            /**
             * Handles the click event for the "Notify" button. Starts the CustomToAllActivity,
             * passing the eventID, eventTitle, and a listType of 3, while clearing the activity stack.
             *
             * @param v The clicked view (Notify button).
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventFinalListActivity.this, CustomToAllActivity.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("eventTitle", eventTitle);
                intent.putExtra("listType", 3);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    /**
     * Fetches the list of final entrants for the specified event from Firestore
     * and populates the ListView with the entrants' device IDs.
     *
     * @param eventID The ID of the event whose final entrants are to be fetched.
     */
    void fetchFinalEntrants(String eventID) {
        DocumentReference eventRef = db.collection("event").document(eventID);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get the finalEntrants field from the event document
                List<String> finalEntrants = (List<String>) documentSnapshot.get("finalEntrants");

                if (finalEntrants != null && !finalEntrants.isEmpty()) {
                    for (String deviceID : finalEntrants) {
                        // Create a UserProfile with deviceID as a placeholder
                        UserProfile userProfile = new UserProfile();
                        userProfile.setDeviceId(deviceID); // Set the deviceID

                        // Query the users collection to get the name associated with this deviceID
                        db.collection("users")
                                .whereEqualTo("deviceId", deviceID)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // Assuming only one document per deviceID
                                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                                        String userName = userDoc.getString("name");
                                        userProfile.setName(userName); // Set the user's name
                                    } else {
                                        userProfile.setName("Unknown User"); // Fallback if no user is found
                                    }

                                    // Add the userProfile to finalEntrantsList
                                    finalEntrantsList.add(userProfile);

                                    // Notify the adapter to update the ListView
                                    finalEntrantsAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EventFinalListActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                });
                    }
                } else {
                    Toast.makeText(EventFinalListActivity.this, "No final entrants found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventFinalListActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(EventFinalListActivity.this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
        });
    }
}
