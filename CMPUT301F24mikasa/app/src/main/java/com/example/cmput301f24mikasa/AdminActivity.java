package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminActivity provides the main dashboard for administrators to manage various
 * application aspects such as facilities, user profiles, images, and events. It
 * offers navigation buttons that redirect to specific management sections for each
 * functionality.
 *
 * <p>This activity contains:
 * <ul>
 *   <li>Buttons to navigate to sections for managing facilities, user profiles, images, and events.</li>
 *   <li>Bottom navigation buttons for quick access to profiles, events, notifications, and the admin dashboard.</li>
 * </ul>
 *
 * <p><b>Note:</b> Some buttons have placeholders for functionality that needs to be implemented
 * in future updates (e.g., managing images and events).
 *
 * @version 1.0
 * @since 2024-11-08
 */
public class AdminActivity extends AppCompatActivity {
    private Button manageFacilitiesButton;
    private Button manageProfilesButton;
    private Button manageEventsButton;

    /**
     * Default constructor for AdminActivity.
     * Required for Android activity lifecycle and initialization.
     */
    public AdminActivity() {
        // Constructor is provided by default
    }

    /**
     * Called when the activity is first created. This method sets up the layout, initializes
     * buttons, and assigns click listeners to provide navigation functionality.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the most recent saved data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize buttons
        manageFacilitiesButton = findViewById(R.id.button_manage_facilities);
        manageProfilesButton = findViewById(R.id.button_manage_profiles);
        manageEventsButton = findViewById(R.id.button_manage_events);

        // Set click listener to open ManageFacilitiesActivity
        manageFacilitiesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageFacilitiesActivity.class);
            startActivity(intent);
        });

        // Set click listener to open ManageProfilesActivity
        manageProfilesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageProfileActivity.class);
            startActivity(intent);
        });

        manageEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, AdminManageEventsActivity.class);
            startActivity(intent);
        });


        // Setup bottom navigation buttons
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);

        // Bottom Navigation onClick Listeners
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(AdminActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(AdminActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(AdminActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(AdminActivity.this, AdminActivity.class)));
    }
}

