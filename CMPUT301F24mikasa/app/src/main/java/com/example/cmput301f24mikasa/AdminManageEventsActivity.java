package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class AdminManageEventsActivity extends AppCompatActivity implements AdminEventAdapter.OnEventClickListener {
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

        adapter = new AdminEventAdapter(this, eventList, this);
        recyclerView.setAdapter(adapter);

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
                        Log.e("AdminManageEvents", "Error fetching events: ", task.getException());
                        Toast.makeText(this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onViewButtonClick(Event event) {
        Intent intent = new Intent(this, AdminEventDetailsActivity.class);
        intent.putExtra("EVENT_ID", event.getEventID());
        startActivity(intent);
    }

    @Override
    public void onDeleteButtonClick(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event").document(event.getEventID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    eventList.remove(event);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminManageEventsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerManageEvents", "Error deleting event", e);
                    Toast.makeText(AdminManageEventsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                });
        Toast.makeText(AdminManageEventsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
    }
}




