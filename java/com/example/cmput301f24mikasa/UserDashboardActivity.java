package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class UserDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize buttons with specific IDs
        Button btnUserNotifications = findViewById(R.id.btn_user_notifications);
        Button btnUserProfile = findViewById(R.id.btn_user_profile);
        Button btnUserEvents = findViewById(R.id.btn_user_events);
        Button btnUserQRScanner = findViewById(R.id.btn_user_qr_scanner);
        Button btnUserLocation = findViewById(R.id.btn_user_location);
        Button btnUserBack = findViewById(R.id.btn_user_back);

        // Set click listeners for each button
        btnUserNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserNotificationsActivity.class);
            startActivity(intent);
        });

        btnUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        btnUserEvents.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserEventsActivity.class);
            startActivity(intent);
        });

        btnUserQRScanner.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserQRScannerActivity.class);
            startActivity(intent);
        });

        btnUserLocation.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserLocationActivity.class);
            startActivity(intent);
        });

        btnUserBack.setOnClickListener(v -> finish());
    }
}
