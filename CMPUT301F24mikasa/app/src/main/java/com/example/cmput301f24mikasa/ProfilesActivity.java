package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ProfilesActivity is responsible for displaying the profile options to the user.
 * This activity provides navigation to the UserProfileActivity and FacilityProfileActivity
 * as well as access to bottom navigation buttons for other sections of the app,
 * such as events, notifications, and admin controls.
 */
public class ProfilesActivity extends AppCompatActivity {

    /**
     * Default constructor for ProfilesActivity.
     */
    public ProfilesActivity() {
    }

    /**
     * Initializes the profile buttons and sets up the bottom navigation buttons.
     * Navigates to UserProfileActivity or FacilityProfileActivity based on the user's selection.
     * Sets up listeners for bottom navigation buttons that allow users to navigate
     * to the Events, Notifications, or Admin sections of the app.
     *
     * @param savedInstanceState a bundle containing the activity's previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        // Initialize UI elements
        Button userProfileButton = findViewById(R.id.user_profile_button);
        Button facilityProfileButton = findViewById(R.id.facility_profile_button);

        // Navigate to UserProfileActivity
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilesActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to FacilityProfileActivity
        facilityProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilesActivity.this, FacilityProfileActivity.class);
                startActivity(intent);
            }
        });


        // Setup bottom navigation buttons
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);

        AdminVerification.checkIfAdmin(this, buttonAdmin);
        
        // Bottom Navigation onClick Listeners
        buttonHome.setOnClickListener(view -> startActivity(new Intent(ProfilesActivity.this, MainActivity.class)));
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(ProfilesActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(ProfilesActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(ProfilesActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(ProfilesActivity.this, AdminActivity.class)));
    }
}
