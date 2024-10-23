package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class UserProfileViewActivity extends AppCompatActivity {

    private ListView waitingListView;
    private UserProfileArrayAdapter adapter;
    private List<UserProfile> userProfileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waiting_list);

        waitingListView = findViewById(R.id.waiting_list_view);
        userProfileList = new ArrayList<>();

        // Initialize adapter
        adapter = new UserProfileArrayAdapter(this, userProfileList);
        waitingListView.setAdapter(adapter);

        // Load user profiles (from Firebase or another source)
        loadUserProfiles();
    }

    private void loadUserProfiles() {
        // Code to fetch user profiles from Firebase Firestore
        // Example: userProfileList.add(new UserProfile("John Doe", "john@example.com"));
        adapter.notifyDataSetChanged();
    }
}
