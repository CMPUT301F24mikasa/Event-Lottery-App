/**
 * This is the Admin Dashboard, where the Admin can manage events, profiles, images, and facilities.
 * Each button redirects to the respective section of the admin panel.
 */

package com.example.cmput301f24mikasa;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminActivity provides the main dashboard for admins to manage events,
 * user profiles, images, and facilities. It includes navigation buttons that
 * redirect to the appropriate management sections.
 */
public class AdminActivity extends AppCompatActivity {

    /**
     * Initializes the activity, setting up the admin dashboard and button click listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        NavigatonActivity.setupBottomNavigation(this);
        Button buttonManageEvents = findViewById(R.id.btn_manage_events);
        Button buttonManageProfiles = findViewById(R.id.btn_manage_profiles);
        Button buttonManageImages = findViewById(R.id.btn_manage_images);
        Button buttonManageFacilities = findViewById(R.id.btn_manage_facilities);

        buttonManageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, AdminManageEvents.class);
                startActivity(intent);
            }
        });

        buttonManageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, ManageProfileActivity.class);
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
    }
}
