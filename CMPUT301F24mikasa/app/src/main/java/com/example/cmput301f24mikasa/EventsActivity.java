package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Button qrScannerButton = findViewById(R.id.qr_scanner_button);
        Button eventsSignedUpButton = findViewById(R.id.events_signed_up_button);
        Button createEventButton = findViewById(R.id.create_event_button);
        Button manageEventsButton = findViewById(R.id.manage_events_button);

        // Navigate to QR Scanner Activity
        qrScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, QRScannerActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Events Signed Up For Activity
        eventsSignedUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, EventsJoinedActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Create Event Activity
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, CreateEventActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Manage Events Activity
        manageEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, ManageEventsActivity.class);
                startActivity(intent);
            }
        });
    }
}
