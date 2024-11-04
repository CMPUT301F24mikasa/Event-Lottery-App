package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    private TextView txtEventTitle, txtEventDescription, txtEventDate, txtEventPrice;
    private Button btnSignUp;

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

        loadEventDetails();

        btnSignUp.setOnClickListener(v -> addDeviceToWaitingList());
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        txtEventTitle.setText(documentSnapshot.getString("title"));
                        txtEventDescription.setText(documentSnapshot.getString("description"));
                        txtEventDate.setText(documentSnapshot.getString("date"));
                        txtEventPrice.setText(documentSnapshot.getString("price"));
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show());
    }

    private void addDeviceToWaitingList() {
        DocumentReference eventRef = db.collection("events").document(eventId);
        String deviceId = fetchDeviceId();

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                if (waitingList == null) waitingList = new ArrayList<>();

                if (!waitingList.contains(deviceId)) {
                    waitingList.add(deviceId);
                    eventRef.update("waitingList", waitingList)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to sign up", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "You are already signed up for this event", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("HardwareIds")
    public String fetchDeviceId() {
        // Replace with your device ID retrieval method
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}

