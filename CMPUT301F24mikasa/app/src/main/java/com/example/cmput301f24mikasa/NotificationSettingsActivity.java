package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Activity that handles the settings for notifications, allowing the user to enable or disable notifications.
 * The notification setting is saved using SharedPreferences.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    /**
     * Switch to toggle notifications on or off.
     */
    private SwitchCompat switchNotifications;

    /**
     * SharedPreferences object to store the user's notification settings.
     */
    private SharedPreferences sharedPreferences;

    /**
     * The name of the SharedPreferences file used to store settings.
     */
    private static final String PREFS_NAME = "notification_prefs";

    /**
     * The key used to store and retrieve the notification setting from SharedPreferences.
     */
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    /**
     * Called when the activity is created. Initializes the UI components, loads the saved notification settings,
     * and sets up the listeners for the notification toggle switch and the back button.
     *
     * @param savedInstanceState the previous instance's state (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        switchNotifications = findViewById(R.id.switch_notifications);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved notification setting
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        switchNotifications.setChecked(notificationsEnabled);

        // Set listener to update preference when toggle state changes
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked);
            editor.apply();
        });

        Button backButton = findViewById(R.id.back_button);
        // Set listener for the back button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationSettingsActivity.this, ManageNotificationsActivity.class);
            startActivity(intent); finish();
        });


    }
}