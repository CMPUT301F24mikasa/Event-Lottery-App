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

public class WaitingListArrayAdapter extends android.widget.ArrayAdapter<UserProfile> {
    private Context context;
    private List<UserProfile> userProfiles;
    private String eventID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public WaitingListArrayAdapter(@NonNull Context context, @NonNull List<UserProfile> userProfiles, String eventID) {
        super(context, 0, userProfiles);
        this.context = context;
        this.userProfiles = userProfiles;
        this.eventID = eventID;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.waiting_list_item, parent, false);
        }

        // Get the current user profile
        UserProfile userProfile = userProfiles.get(position);

        // Bind the views
        TextView userNameTextView = convertView.findViewById(R.id.user_name_text_view);
        TextView locationTextView = convertView.findViewById(R.id.location_text_view);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        // Set data to views
        userNameTextView.setText(userProfile.getName());
        locationTextView.setText(userProfile.getLocation());

        // Handle delete button click
        deleteButton.setOnClickListener(v -> {
            String deviceID = userProfile.getDeviceId();
            removeUserFromWaitingList(deviceID, position);
        });

        return convertView;
    }

    private void removeUserFromWaitingList(String deviceID, int position) {
        db.collection("event").document(eventID)
                .update(
                        "waitingList", FieldValue.arrayRemove(deviceID), // Remove from waitingList
                        "cancelledEntrants", FieldValue.arrayUnion(deviceID), // Add to cancelledEntrants
                        "LocationList." + deviceID, FieldValue.delete() // Remove from LocationList
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
