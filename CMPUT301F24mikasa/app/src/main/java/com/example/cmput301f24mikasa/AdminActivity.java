package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button buttonManageEvents = findViewById(R.id.btn_manage_events);
        Button buttonManageProfiles = findViewById(R.id.btn_manage_profiles);
        Button buttonManageImages = findViewById(R.id.btn_manage_images);
        Button buttonManageFacilities = findViewById(R.id.btn_manage_facilities);

        buttonManageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, ManageEventsActivity.class);
                startActivity(intent);
            }
        });

        buttonManageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        buttonManageImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, ManageImagesActivity.class);
                startActivity(intent);
            }
        });

        buttonManageFacilities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, ManageFacilitiesActivity.class);
                startActivity(intent);
            }
        });

        NavigatonActivity.setupBottomNavigation(this);

    }
}
