package com.example.cmput301f24mikasa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * NotificationForegroundService is a foreground service that fetches event notifications
 * from a Firestore database and displays them as Android system notifications.
 * This service runs in the background, ensuring notifications are fetched and displayed
 * even when the app is not actively in use.
 */
public class NotificationForegroundService extends Service {
    private static final String CHANNEL_ID = "event_notifications_channel";
    private static final String TAG = "NotificationService"; // Tag for logging
    private FirebaseFirestore db;


    /**
     * This method initializes the Firestore instance, creates the notification channel,
     * and begins fetching notifications.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        fetchNotifications();
    }

    // Referenced the following function from OpenAI, ChatGPT, 2024-11-27
    /**
     * Called when the service is started.
     * Sets up the persistent notification for the foreground service.
     *
     * @param intent  The Intent used to start the service.
     * @param flags   Additional data about the start request.
     * @param startId A unique identifier for this specific start request.
     * @return Returns START_STICKY to indicate that the service should continue running until explicitly stopped.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // Create the persistent notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Event Notifications")
                .setContentText("Fetching event notifications...")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());

        // Return START_STICKY to keep the service alive
        return START_STICKY;
    }

    private void fetchNotifications() {

        // Fetch deviceID from shared preferences
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        if (deviceID.isEmpty()) {
            stopSelf(); // Stop service if no device ID is found
            return;
        }

        // Query Firestore for new notifications
        db.collection("notification")
                .whereEqualTo("deviceID", deviceID)
                .whereEqualTo("appeared", "no")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        Log.d(TAG, "Found " + querySnapshot.size() + " new notifications");
                        for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String notificationText = document.getString("text");

                            // Display each notification
                            showNotification(notificationText);

                            // Mark notification as "appeared"
                            db.collection("notification")
                                    .document(document.getId())
                                    .update("appeared", "yes")
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as appeared"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update notification: " + e.getMessage()));
                        }
                    } else {
                    }
                });
    }

    // Referenced the following function from OpenAI, ChatGPT, 2024-11-27
    private void showNotification(String text) {

        Intent intent = new Intent(this, ManageNotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New Event Notification")
                .setContentText(text)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
        }
    }

    private void createNotificationChannel() {
        Log.d(TAG, "Creating notification channel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for event notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
            }
        }
    }

    /**
     * Called when a client binds to the service.
     * This implementation returns null because binding is not used for this service.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Always returns null as this service does not support binding.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return null; // No binding required for this service
    }
}
