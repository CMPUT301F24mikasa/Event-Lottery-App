package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * WaitingListArrayAdapter for displaying a list of user profiles on the waiting list of an event.
 * Provides functionality to remove users from the waiting list.
 */
public class WaitingListArrayAdapter extends android.widget.ArrayAdapter<UserProfile> {
    private Context context;
    private List<UserProfile> userProfiles;
    private String eventID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Constructor for initializing the adapter.
     *
     * @param context      The activity context to update UI elements.
     * @param userProfiles List of user profiles on the waiting list.
     * @param eventID      The unique ID for each event.
     */
    public WaitingListArrayAdapter(@NonNull Context context, @NonNull List<UserProfile> userProfiles, String eventID) {
        super(context, 0, userProfiles);
        this.context = context;
        this.userProfiles = userProfiles;
        this.eventID = eventID;
    }

    /**
     * Inflates the view for each user profile in the waiting list.
     *
     * @param position    The position of the current user profile in the list.
     * @param convertView The old view to reuse, if possible
     * @param parent      The parent that this view will eventually be attached to.
     * @return  The view for the current user profile in the waiting list.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.waiting_list_item, parent, false);
        }

        // Get the current user profile
        UserProfile userProfile = userProfiles.get(position);

        // Find the view in the layout
        TextView userNameTextView = convertView.findViewById(R.id.user_name_text_view);
        TextView locationTextView = convertView.findViewById(R.id.location_text_view);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        // Set the user name and location
        userNameTextView.setText(userProfile.getName());
        locationTextView.setText(userProfile.getLocation());

        // Handle click listener for the delete button
        deleteButton.setOnClickListener(v -> {
            String deviceID = userProfile.getDeviceId();
            removeUserFromWaitingList(deviceID, position);
        });

        return convertView;
    }

    /**
     * Removes a user from the waiting list, adds them to the cancelled entrants list,
     * and removes their location information from the event document in Firestore.
     *
     * @param deviceID The unique id of the device.
     * @param position The position of the user in the waiting list.
     */
    private void removeUserFromWaitingList(String deviceID, int position) {
        db.collection("event").document(eventID)
                .update(
                        "waitingList", FieldValue.arrayRemove(deviceID), // Remove from waitingList
                        "cancelledEntrants", FieldValue.arrayUnion(deviceID), // Add to cancelled entrants list
                        "LocationList." + deviceID, FieldValue.delete() // Remove the user's location data
                )
                .addOnSuccessListener(aVoid -> {
                    // Remove user from the local list and update the UI
                    userProfiles.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "User removed and added to cancelled entrants.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
