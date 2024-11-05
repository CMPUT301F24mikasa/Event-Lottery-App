package com.example.cmput301f24mikasa;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

public class NavigatonActivity {
    public static void setupBottomNavigation(final Activity activity) {
        // Bind ImageButtons
        ImageButton buttonProfiles = activity.findViewById(R.id.button_profiles);
        ImageButton buttonEvents = activity.findViewById(R.id.button_events);
        ImageButton buttonNotifications = activity.findViewById(R.id.button_notifications);
        ImageButton buttonAdmin = activity.findViewById(R.id.button_admin);

        // Set OnClickListeners for each button
        buttonProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the current activity is already ProfilesActivity to avoid restarting
                if (!(activity instanceof ProfilesActivity)) {
                    Intent intent = new Intent(activity, ProfilesActivity.class);
                    activity.startActivity(intent);
                }
            }
        });

        buttonEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(activity instanceof EventsActivity)) {
                    Intent intent = new Intent(activity, EventsActivity.class);
                    activity.startActivity(intent);
                }
            }
        });

        buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(activity instanceof NotificationsActivity)) {
                    Intent intent = new Intent(activity, NotificationsActivity.class);
                    activity.startActivity(intent);
                }
            }
        });

        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(activity instanceof AdminActivity)) {
                    Intent intent = new Intent(activity, AdminActivity.class);
                    activity.startActivity(intent);
                }
            }
        });
    }
}
