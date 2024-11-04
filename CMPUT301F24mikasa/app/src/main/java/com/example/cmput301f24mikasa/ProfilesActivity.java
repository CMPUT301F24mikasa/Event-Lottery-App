package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProfilesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

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
    }
}
