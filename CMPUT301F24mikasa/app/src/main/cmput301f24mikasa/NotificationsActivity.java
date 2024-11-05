package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private NotificationsAdapter adapter;
    private List<Notifications> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize the back button
        Button buttonBack = findViewById(R.id.button);

        // Initialize the reload button
        //Button reload_Button = findViewById(R.id.reloadButton);
        //reload_Button.setOnClickListener(view -> reloadNotifications());

        // Set an OnClickListener for the back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // This will close the current activity and return to the previous one
            }
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        notificationsList = new ArrayList<>();
        adapter = new NotificationsAdapter(this, notificationsList);

        // Initialize the ListView and set the adapter
        ListView listView = findViewById(R.id.notificationsList);
        listView.setAdapter(adapter);

    }

    private void loadNotifications(@NonNull String userID) {
        db.collection("users").document(userID)
                .collection("notifications")
                //.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationsList.clear(); // Clear the list to avoid duplication
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Notifications notification = document.toObject(Notifications.class);
                            notificationsList.add(notification); // Add each notification to the list
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log any errors for debugging
                        Log.e("Firestore", "Error loading notifications", task.getException());
                    }
                });
    }

    // New method to reload notifications
    private void reloadNotifications(@NonNull String userID) {
        loadNotifications(userID); // Call the existing load method
    }
}
