package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to manage the waiting list of an event.
 * Allows the admin to view and delete users from the waiting list of a specific event.
 */
public class ManagingWaitListActivity extends AppCompatActivity {

    private String eventID;
    private List<String> waitingList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView waitingListView;
    private String selectedUserToDelete = null;

    /**
     * Called when the activity is created.
     * Initializes the waiting list view and sets up the functionality for managing the waiting list.
     *
     * @param savedInstanceState the saved instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_waitlist);

        // Retrieve eventID from Intent
        eventID = getIntent().getStringExtra("eventID");

        String eventTitle = getIntent().getStringExtra("eventTitle");
        TextView changetext = findViewById(R.id.headerTextView);
        changetext.setText("Managing List For " + eventTitle);

        // Initialize ListView
        waitingListView = findViewById(R.id.manage_wait_list_view);

        // Initialize back button and set its OnClickListener
        Button backButton = findViewById(R.id.back_button_for_final_list);


        // Fetch the waiting list data from Firestore
        fetchWaitingList();

        // Set up the ListView and adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, waitingList);
        waitingListView.setAdapter(adapter);

        // Handle item clicks (to delete or cancel)
        waitingListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUser = waitingList.get(position);
            selectedUserToDelete = selectedUser;

            // Confirm deletion or cancel it
            new android.app.AlertDialog.Builder(ManagingWaitListActivity.this)
                    .setMessage("Do you want to delete " + selectedUser + " from the waiting list?")
                    .setCancelable(false)
                    .setPositiveButton("Delete", (dialog, id1) -> {
                        deleteUserFromWaitingList(selectedUser);
                    })
                    .setNegativeButton("Cancel", (dialog, id12) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultsIntent = new Intent(ManagingWaitListActivity.this, WaitingListActivity.class);
                resultsIntent.putExtra("eventID", eventID);
                resultsIntent.putExtra("eventTitle", eventTitle);
                startActivity(resultsIntent);
                finish();
            }
        });
    }

    /**
     * Fetches the waiting list for the event from Firestore.
     * Updates the waiting list view with the fetched data.
     */
    private void fetchWaitingList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("event").document(eventID);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Fetch the waiting list from the event document
                    List<String> waitingListFromFirestore = (List<String>) document.get("waitingList");
                    if (waitingListFromFirestore != null) {
                        waitingList.clear();
                        waitingList.addAll(waitingListFromFirestore);
                        adapter.notifyDataSetChanged(); // Refresh the ListView
                    }
                } else {
                    Toast.makeText(this, "Event not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to fetch waiting list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a user from the waiting list in Firestore.
     * Removes the user from the local waiting list and updates the UI.
     *
     * @param userToDelete the user to be deleted from the waiting list
     */
    private void deleteUserFromWaitingList(String userToDelete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("event").document(eventID);

        // Remove the user from the waiting list in Firestore
        eventRef.update("waitingList", com.google.firebase.firestore.FieldValue.arrayRemove(userToDelete))
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed user
                    waitingList.remove(userToDelete);
                    adapter.notifyDataSetChanged(); // Refresh the ListView
                    Toast.makeText(ManagingWaitListActivity.this, "User removed from waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to remove user
                    Toast.makeText(ManagingWaitListActivity.this, "Failed to remove user", Toast.LENGTH_SHORT).show();
                });
    }
}
