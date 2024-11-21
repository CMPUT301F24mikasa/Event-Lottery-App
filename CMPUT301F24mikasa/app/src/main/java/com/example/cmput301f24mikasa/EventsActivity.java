package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity serves as the main interface for managing and navigating event-related features
 * in the application. It provides buttons to navigate to activities for scanning QR codes,
 * viewing signed-up events, creating new events, and managing existing events.
 * Additionally, it contains bottom navigation buttons to navigate to other main sections of the app.
 *
 * Activities accessible from this screen include:
 * - QRScannerActivity
 * - EventsJoinedActivity
 * - CreateEventActivity
 * - ManageEventsActivity
 * - ProfilesActivity
 * - ManageNotificationsActivity
 * - AdminActivity
 *
 * @version 1.0
 */
public class EventsActivity extends AppCompatActivity {


    /**
     * Default constructor for EventsActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public EventsActivity() {
        // Constructor is provided by default
    }

    /**
     * Initializes the activity and sets up the button click listeners for the event actions and
     * bottom navigation. This method is called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */

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

        // Setup bottom navigation buttons
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);

        AdminVerification.checkIfAdmin(this, buttonAdmin);

        // Bottom Navigation onClick Listeners
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(EventsActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(EventsActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(EventsActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(EventsActivity.this, AdminActivity.class)));
    }
}
