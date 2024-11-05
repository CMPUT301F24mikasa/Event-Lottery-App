/**
 * AdminManageEvents class allows admins to view and delete events stored in FireStore
 * Events are listed, and options include view and deleting events.
 */

package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 *  Handles event management for administrators, including viewing and deleting
 *  retrieved from Firebase Firestore.
 */
public class AdminManageEvents extends AppCompatActivity implements EventArrayAdapter.OnEventClickListener {
    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;
    private boolean isAdmin = true;

    /**
     * Handles the creation of the activity, sets up the event list,
     * and initializes the Firebase connection.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        NavigatonActivity.setupBottomNavigation(this);

        eventList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadEvents();

        eventListView = findViewById(R.id.event_list_view);
        adapter = new EventArrayAdapter(this, eventList, this, isAdmin);
        eventListView.setAdapter(adapter);

        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadEvents() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventList.clear();

        db.collection("event")
                .whereEqualTo("deviceID", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching events: ", e);
                });
    }

    /**
     * Handles the click event for the view button of an event.
     * Retrieves the event details and navigates to the WaitingListActivity.
     * @param event that we want to view
     */
    @Override
    public void onViewButtonClick(Event event) {
        Log.d("AdminManageEvents", "View button clicked for event with ID: " + event.getEventID());

        if (event.getEventID() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference eventRef = db.collection("event").document(event.getEventID());

            eventRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String eventTitle = document.getString("title");

                        if (eventTitle != null) {
                            Intent intent = new Intent(AdminManageEvents.this, WaitingListActivity.class);

                            intent.putExtra("eventID", event.getEventID());
                            intent.putExtra("eventTitle", eventTitle);

                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Event title not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve event title", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "CRITICAL OMEGA ALPHA Failure: Event ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the click event for the delete button of an event.
     * Deletes the event from Firestore and updates the event list.
     * @param event that we want to delete
     */
    @Override
    public void onDeleteButtonClick(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event").document(event.getEventID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    eventList.remove(event);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminManageEvents.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminManageEvents", "Error deleting event", e);
                    Toast.makeText(AdminManageEvents.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                });
        Toast.makeText(AdminManageEvents.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the click event for the edit button of an event.
     * @param event that we want to edit
     */
    @Override
    public void onEditButtonClick(Event event) {
        // Handle the edit button action for the specific event
        Log.d("AdminManageEvents", "Edit button clicked for event: " + event.getTitle());
    }
}
