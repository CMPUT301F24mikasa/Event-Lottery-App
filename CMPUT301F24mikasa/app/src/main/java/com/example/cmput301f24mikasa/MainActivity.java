package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.ImageHeaderParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainActivity serves as the entry point of the app. It manages navigation to other sections of the app,
 * such as user profiles, events, notifications, and admin tools. It also checks if the user profile exists
 * in Firebase Firestore and prompts the user to create a profile if none is found.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private static final int REQUEST_CODE_CREATE_PROFILE = 1;
    private ImageButton buttonAdmin;

    /**
     * Initializes the main activity, checks if a user profile exists, and sets up navigation buttons.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state, if any.
     */
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up the ImageButtons
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        buttonAdmin = findViewById(R.id.button_admin);

        buttonAdmin.setVisibility(ImageButton.GONE);

        checkUserProfile();  // Check if user profile exists on launch
        checkIfAdmin();

        // Set onClick listeners for navigation
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AdminActivity.class)));
    }

    /**
     * Checks if the user profile exists in Firestore by searching for the device ID.
     * If no profile is found, redirects the user to the UserProfileActivity to create a new profile.
     */
    private void checkUserProfile() {
        db.collection("users").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // User profile exists, proceed with MainActivity
                            Log.d("MainActivity", "User profile exists for device ID: " + deviceId);
                        } else {
                            // User profile does not exist, redirect to UserProfileActivity
                            Log.d("MainActivity", "No user profile found for device ID: " + deviceId);
                            Toast.makeText(this, "Please create your profile", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_CREATE_PROFILE);
                        }
                    } else {
                        Log.e("MainActivity", "Error checking user profile", task.getException());
                        Toast.makeText(this, "Error checking profile. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles the result from the UserProfileActivity after the user creates a profile.
     * Displays a success message if the profile is created successfully.
     *
     * @param requestCode The request code passed in startActivityForResult().
     * @param resultCode The result code returned by the called activity.
     * @param data The Intent that contains result data.
     */
    // Handle result from UserProfileActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_PROFILE && resultCode == RESULT_OK) {
            // Profile was created successfully, proceed with MainActivity
            Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfAdmin() {
        db.collection("admin").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Device ID matches an admin entry
                            Log.d("MainActivity", "User is an admin: " + deviceId);
                            Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                            buttonAdmin.setVisibility(ImageButton.VISIBLE);
                        } else {
                            Log.d("MainActivity", "User is not an admin: " + deviceId);
                        }
                    } else {
                        Log.e("MainActivity", "Error checking admin status", task.getException());
                        Toast.makeText(this, "Error checking admin status. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
            });
    }
}
