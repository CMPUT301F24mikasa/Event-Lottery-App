package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        // Initialize buttons
        Button btnCreateEvent = findViewById(R.id.btn_create_event);
        Button btnManageEvents = findViewById(R.id.btn_manage_events);
        Button btnFacilityProfile = findViewById(R.id.btn_facility_profile);
        Button btnBack = findViewById(R.id.btn_back);

        // Set click listeners
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, OrganizerManageEventsActivity.class);
            startActivity(intent);
        });

        btnFacilityProfile.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this, FacilityProfileActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
