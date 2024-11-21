package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing the random selection of participants from the event's waiting list.
 * Displays the current entrants, allows the user to specify how many participants to select,
 * and updates the Firestore database accordingly.
 */
public class ListSamplingActivity extends AppCompatActivity {
    EditText participantInput;
    Button generateButton;
    Button cancelButton;
    ArrayList<String> dataList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference eventsRef = db.collection("event");
    private ArrayList<String> selectedEntrants;

    /**
     * Default constructor for ListSamplingActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ListSamplingActivity() {
        // Constructor is provided by default
    }

    /**
     * Initializes the activity by setting up the UI components, retrieving event data from the Intent,
     * and fetching the waiting list of participants from Firestore.
     * Also sets up the event listeners for the buttons.
     *
     * @param savedInstanceState A bundle containing any saved state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_waiting_list);

        TextView changetext = findViewById(R.id.current_entrants_text);
        String newtext = "Current Entrants = ";
        Intent intent = getIntent();

        // Retrieve the number of entrants and the list of UserProfile objects
        int numberOfEntrants = intent.getIntExtra("size",0);
        String eventID = intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");
        Toast.makeText(ListSamplingActivity.this, "number" + numberOfEntrants, Toast.LENGTH_SHORT).show();

        // Display the number of entrants
        changetext.setText(newtext + numberOfEntrants);

        // Initialize views
        participantInput = findViewById(R.id.participant_input);
        generateButton = findViewById(R.id.generate_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Initialize the list to hold selected entrants
        selectedEntrants = new ArrayList<>();
        dataList = new ArrayList<>();
        fetchWaitingList(eventID); // Copy to avoid modifying the original list

        // Set up the Generate button to handle random selection
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = participantInput.getText().toString().trim();

                if (inputText.isEmpty()) {
                    Toast.makeText(ListSamplingActivity.this, "Please enter a number", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int participants = Integer.parseInt(inputText);
                        // Check if the entered number exceeds the available entrants
                        if (participants > numberOfEntrants) {
                            Toast.makeText(ListSamplingActivity.this, "Entered number exceeds available participants", Toast.LENGTH_SHORT).show();
                        } else {
                            // Clear the selectedEntrants list
                            selectedEntrants.clear();

                            // Randomly select participants from userProfiles
                            for (int i = 0; i < participants; i++) {
                                int randomIndex = (int) (Math.random() * dataList.size());
                                String selectedUser = dataList.get(randomIndex);

                                // Add the selected UserProfile to the selectedEntrants list and remove it from tempProfiles
                                selectedEntrants.add(selectedUser);
                                dataList.remove(randomIndex);
                            }
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference eventDocRef = db.collection("event").document(eventID);

                            // Prepare the data to update
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("selectedEntrants", selectedEntrants);
                            updateData.put("waitingList", dataList);
                            updateData.put("alreadySampled", "1");
                            updateData.put("chosenAmount", participants);

                            // Update the Firestore document
                            eventDocRef.update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ListSamplingActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ListSamplingActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show();
                                    });
                            Intent intent = new Intent(ListSamplingActivity.this, EventResultList.class);
                            intent.putExtra("eventID", eventID);
                            intent.putExtra("eventTitle", eventTitle);
                            startActivity(intent);
                            //finish(); // Finish the current activity (EventResultList)
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(ListSamplingActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Handle Cancel button click
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the input field and show a toast message
                participantInput.setText("");
                Toast.makeText(ListSamplingActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    /**
     * Fetches the waiting list for a given event ID from Firestore.
     * Populates the dataList with the device IDs of participants in the waiting list.
     *
     * @param eventID The ID of the event to fetch the waiting list for.
     */
    void fetchWaitingList(String eventID) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array from the document
                        List<String> waitingListIds = (List<String>) documentSnapshot.get("waitingList");

                        if (waitingListIds != null && !waitingListIds.isEmpty()) {
                            // For each deviceID in the waiting list, add it as a placeholder user name
                            for (String deviceID : waitingListIds) {

                                // Create a UserProfile with deviceID as the name
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                //later on, instead of displaying deviceID will want to display later
                                //name associated with deviceID
                                //could do it this way however we will address this later
                                //dataList.add(userProfile);
                                dataList.add(userProfile.getName());
                            }
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

//NOTTTTTTTTTEEEEEEEEEEE TO ADJUST WHEN YOU GRAB A USER OBJECT SO THAT YOU ALSO GET A
//USER NAME, MAKE SURE IT IS CONSISTENT AND WORDS ACROOS WAITINGLISTACITVITY
//LISTINGSAMPLINGACTIVITY
//AND EVENTRESULTLIST
//change sp tjat you use a proper getter for device id not name lol
//also notify the user that have not been chosen, and also the chosen too, and the cancelled user notifies themselves in a way