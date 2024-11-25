package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to view event details and sign up for an event by adding the device to the waiting list.
 * Displays event information such as title, description, date, and price.
 * Allows users to sign up by adding their device ID to the event's waiting list.
 */
public class ViewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    private TextView txtEventTitle, txtEventDescription, txtEventDate, txtEventPrice;
    private Button btnSignUp;

    private EditText editTextCity, editTextProvince;

    /**
     * Default constructor for ViewEventActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ViewEventActivity() {
        // Constructor is provided by default
    }

    /**
     * Called when the activity is created. Initializes UI components, loads event details,
     * and sets up the sign-up button functionality.
     *
     * @param savedInstanceState The saved instance state bundle from previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        txtEventTitle = findViewById(R.id.txtEventTitle);
        txtEventDescription = findViewById(R.id.txtEventDescription);
        txtEventDate = findViewById(R.id.txtEventDate);
        txtEventPrice = findViewById(R.id.txtEventPrice);
        btnSignUp = findViewById(R.id.btnSignUp);

        editTextCity = findViewById(R.id.editTextCity);  
        editTextProvince = findViewById(R.id.editTextProvince);  

        // Load event details
        loadEventDetails();

        btnSignUp.setOnClickListener(v -> addDeviceToWaitingList());
    }

    /**
     * Loads the event details (title, description, date, price) from Firestore
     * and updates the corresponding UI components.
     */
    void loadEventDetails() {
        db.collection("event").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        txtEventTitle.setText(documentSnapshot.getString("title"));
                        txtEventDescription.setText(documentSnapshot.getString("description"));
                        txtEventDate.setText(documentSnapshot.getString("startDate"));
                        txtEventPrice.setText(documentSnapshot.getString("price"));

                        // Check if geo-location is required for the event
                        String geoLocationRequired = documentSnapshot.getString("geo-location required");
                        if ("yes".equals(geoLocationRequired)) {
                            // Show the city and province input fields
                            editTextCity.setVisibility(View.VISIBLE);
                            editTextProvince.setVisibility(View.VISIBLE);
                        } else {
                            // Hide the city and province input fields
                            editTextCity.setVisibility(View.GONE);
                            editTextProvince.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Adds the current device to the event's waiting list if the list is not full
     * and if the device is not already in the list.
     */
    void addDeviceToWaitingList() {
        DocumentReference eventRef = db.collection("event").document(eventId);
        String deviceId = fetchDeviceId();

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                if (waitingList == null) waitingList = new ArrayList<>();

                // Retrieve the waiting list limit
                Long waitingListLimit = documentSnapshot.getLong("waitingListLimit");

                // Check if the limit is set and if it will be exceeded
                if (waitingListLimit != null && waitingList.size() >= waitingListLimit) {
                    Toast.makeText(this, "The waiting list is full. You cannot sign up for this event.", Toast.LENGTH_SHORT).show();
                } else {
                    // Only add if the device is not already in the waiting list
                    if (!waitingList.contains(deviceId)) {
                        waitingList.add(deviceId);

                        // Get the user input location if geo-location is required
                        String geoLocation = documentSnapshot.getString("geo-location required");
                        if ("yes".equals(geoLocation)) {
                            String city = editTextCity.getText().toString();
                            String province = editTextProvince.getText().toString();
                            String userLocation = city + ", " + province;

                            if (city.isEmpty() || province.isEmpty()) {
                                Toast.makeText(this, "Please enter all location fields", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                // Save the entered location to the event's "LocationList" field
                                eventRef.update("LocationList." + deviceId, userLocation)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "location saved!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show());
                            }
                        }

                        // Update the event's waiting list
                        eventRef.update("waitingList", waitingList)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to sign up", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "You are already signed up for this event", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches the unique device ID for the current Android device.
     *
     * @return The device ID as a string.
     */
    @SuppressLint("HardwareIds")
    public String fetchDeviceId() {
        // Replace with your device ID retrieval method
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}
