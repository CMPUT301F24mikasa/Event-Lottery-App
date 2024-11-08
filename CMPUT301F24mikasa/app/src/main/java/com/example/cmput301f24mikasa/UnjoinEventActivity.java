package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Activity that allows users to unjoin an event they have previously signed up for.
 * This activity interacts with Firebase Firestore to update the event's waiting list
 * by removing the device ID from it.
 */
public class UnjoinEventActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;
    Button unjoinButton = findViewById(R.id.unjoin_button);


    /**
     * Initializes the activity and retrieves event ID and device ID.
     * Sets up the Unjoin button click listener to trigger the unjoinEvent method.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_joined_event);  // Reusing the existing XML layout

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        // Get the device ID
        ContentResolver contentResolver = getContentResolver();
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);


        unjoinButton.setOnClickListener(v -> {
            Toast.makeText(this, "Unjoin button clicked", Toast.LENGTH_SHORT).show();
            unjoinEvent();
        });
    }

    /**
     * Unjoins the current user from the event by removing their device ID from the event's waiting list.
     * If the event is found and the device is on the waiting list, the device ID is removed.
     * Provides feedback on success or failure via Toast messages.
     */
    private void unjoinEvent() {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("UnjoinEventActivity", "Event ID is missing.");
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("event").document(eventId);
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");

                if (waitingList != null && waitingList.contains(deviceId)) {
                    eventRef.update("waitingList", FieldValue.arrayRemove(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Successfully unjoined event", Toast.LENGTH_SHORT).show();
                                Log.d("UnjoinEventActivity", "Unjoin successful for device ID: " + deviceId);
                                finish(); // Close the activity after unjoining
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UnjoinEventActivity", "Error unjoining event", e);
                                Toast.makeText(this, "Error unjoining event", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "You are not signed up for this event", Toast.LENGTH_SHORT).show();
                    Log.d("UnjoinEventActivity", "Device ID not found in waiting list.");
                }
            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                Log.e("UnjoinEventActivity", "Event document not found for ID: " + eventId);
            }
        }).addOnFailureListener(e -> {
            Log.e("UnjoinEventActivity", "Error retrieving event document", e);
            Toast.makeText(this, "Error retrieving event. Please try again later.", Toast.LENGTH_SHORT).show();
        });
    }

}