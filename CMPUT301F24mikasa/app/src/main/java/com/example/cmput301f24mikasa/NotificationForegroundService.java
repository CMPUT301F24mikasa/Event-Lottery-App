package com.example.cmput301f24mikasa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationForegroundService extends Service {
    private static final String CHANNEL_ID = "event_notifications_channel";
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        fetchNotifications();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the persistent notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon) // Use a subtle icon
                .setContentTitle("")                        // No title
                .setContentText("")                         // No text
                .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority
                .setSilent(true)                            // No sound or vibration
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());

        // Return START_STICKY to keep the service alive
        return START_STICKY;
    }

    private void fetchNotifications() {
        // Fetch deviceID from shared preferences
        String deviceID = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("deviceID", "");

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
                        error.printStackTrace();
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String notificationText = document.getString("text");

                            // Display each notification
                            showNotification(notificationText);

                            // Mark notification as "appeared"
                            db.collection("notification")
                                    .document(document.getId())
                                    .update("appeared", "yes");
                        }
                    }
                });
    }

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
        }
    }

    private void createNotificationChannel() {
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
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding required for this service
    }
}
