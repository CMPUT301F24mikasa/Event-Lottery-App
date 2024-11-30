package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminManageEventsActivity displays a list of all events and allows administrators to manage them.
 * Administrators can view event details, delete events, and navigate back to the admin dashboard.
 * This activity fetches event data from Firebase Firestore and updates the UI dynamically.
 */
public class AdminManageEventsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events_list);

        eventList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventAdapter(this, eventList);
        recyclerView.setAdapter(adapter);

        // Handle "Back" button click
        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManageEventsActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        loadEvents(db);
    }

    private void loadEvents(FirebaseFirestore db) {
        db.collection("event")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setEventID(document.getId());
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // The following function from OpenAI, ChatGPT, 2024-11-29
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the result from AdminEventDetailsActivity was successful
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String eventId = data.getStringExtra("eventId");
            if (eventId != null) {
                for (Event event : eventList) {

                    // Find the event and clear its image URL and update the adapter
                    if (event.getEventID().equals(eventId)) {
                        event.setImageURL(null);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }
}




