package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;

import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import com.bumptech.glide.load.ImageHeaderParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

/**
 * MainActivity serves as the entry point of the app. It manages navigation to other sections of the app,
 * such as user profiles, events, notifications, and admin tools. It also checks if the user profile exists
 * in Firebase Firestore and prompts the user to create a profile if none is found.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private static final int REQUEST_CODE_CREATE_PROFILE = 1;

    /**
     * Default constructor for MainActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public MainActivity() {
        // Constructor is provided by default
    }

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
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonProfiles = findViewById(R.id.button_profiles);
        ImageButton buttonEvents = findViewById(R.id.button_events);
        ImageButton buttonNotifications = findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = findViewById(R.id.button_admin);


        checkUserProfile();  // Check if user profile exists on launch
        AdminVerification.checkIfAdmin(this, buttonAdmin);

        // Set onClick listeners for navigation
        buttonHome.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MainActivity.class)));
        buttonProfiles.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfilesActivity.class)));
        buttonEvents.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, EventsActivity.class)));
        buttonNotifications.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ManageNotificationsActivity.class)));
        buttonAdmin.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AdminActivity.class)));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        /**
        createNotificationChannel();
        scheduleNotificationWorker();
        saveDeviceID();
         */
        // Start the foreground service
        Intent serviceIntent = new Intent(this, NotificationForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    /**
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    */
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
                            // User profile exists, retrieve and display the name
                            String userName = document.getString("name");
                            if (userName != null && !userName.isEmpty()) {
                                TextView welcomeMessage = findViewById(R.id.welcome_message);
                                welcomeMessage.setText("Welcome, " + userName + "!");
                                welcomeMessage.setVisibility(View.VISIBLE);
                            }
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
    /**
    private void createNotificationChannel() {
        CharSequence name = "Event Notifications";
        String description = "Channel for event notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("event_notifications_channel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void scheduleNotificationWorker() {
        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 5, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }
    private void saveDeviceID() {
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putString("deviceID", deviceID)
                .apply();
    }
     */
}

