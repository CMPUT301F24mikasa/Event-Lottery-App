package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize buttons
        Button btnBack = findViewById(R.id.btn_back);
        Button btnManageEvents = findViewById(R.id.btn_manage_events);
        Button btnManageProfiles = findViewById(R.id.btn_manage_profiles);
        Button btnManageImages = findViewById(R.id.btn_manage_images);
        Button btnManageFacilities = findViewById(R.id.btn_manage_facilities);
        Button btnViewReports = findViewById(R.id.btn_view_reports);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });

        btnManageProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageProfilesActivity.class);
            startActivity(intent);
        });

        btnManageImages.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageImagesActivity.class);
            startActivity(intent);
        });

        btnManageFacilities.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageFacilitiesActivity.class);
            startActivity(intent);
        });

        btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ViewReportsActivity.class);
            startActivity(intent);
        });
    }
}
