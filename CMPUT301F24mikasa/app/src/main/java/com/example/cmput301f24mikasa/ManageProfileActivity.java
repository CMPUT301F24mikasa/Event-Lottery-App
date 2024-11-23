package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to manage and view user profiles.
 * Displays a list of user profiles and provides options to view or delete them.
 * Also handles removal of users from waiting lists of events when deleted.
 */
public class ManageProfileActivity extends AppCompatActivity {

    private ListView userProfilesListView;
    private UserProfileArrayAdapter adapter;
    private List<UserProfile> userProfileList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Default constructor for ManageProfileActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ManageProfileActivity() {
        // Constructor is provided by default
    }


    /**
     * Called when the activity is created.
     * Initializes the user profiles list and sets up the adapter.
     * Loads user profiles from Firestore.
     *
     * @param savedInstanceState the saved instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profiles);

        userProfilesListView = findViewById(R.id.user_profiles_list);
        userProfileList = new ArrayList<>();
        adapter = new UserProfileArrayAdapter(this, userProfileList);
        userProfilesListView.setAdapter(adapter);

        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });


        loadUserProfiles();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfiles();
    }

    /**
     * Loads user profiles from the Firestore database.
     * Updates the list view with the fetched user profiles.
     */
    void loadUserProfiles() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userProfileList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserProfile userProfile = document.toObject(UserProfile.class);
                            userProfileList.add(userProfile);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("ManageProfileActivity", "Error getting documents.", task.getException());
                        Toast.makeText(ManageProfileActivity.this, "Failed to load profiles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Custom ArrayAdapter for displaying user profiles in the list view.
     * Sets up the profile image, name, and actions (view and delete) for each user.
     */
    private class UserProfileArrayAdapter extends ArrayAdapter<UserProfile> {
        private final Context context;
        private final List<UserProfile> userList;

        /**
         * Constructor for the UserProfileArrayAdapter.
         *
         * @param context the context of the activity
         * @param userList the list of user profiles to display
         */
        public UserProfileArrayAdapter(Context context, List<UserProfile> userList) {
            super(context, R.layout.activity_manage_profile_list_item, userList);
            this.context = context;
            this.userList = userList;
        }

        /**
         * Returns the view for each item in the list.
         * Inflates the item layout and sets up the profile image and buttons.
         *
         * @param position the position of the item in the list
         * @param convertView the recycled view to reuse (if available)
         * @param parent the parent view group
         * @return the view for the item at the given position
         */
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.activity_manage_profile_list_item, parent, false);

            ImageView profileImageView = rowView.findViewById(R.id.profile_image);
            TextView nameTextView = rowView.findViewById(R.id.user_name);
            Button deleteButton = rowView.findViewById(R.id.delete_button);
            Button viewButton = rowView.findViewById(R.id.view_button);

            UserProfile user = userList.get(position);
            nameTextView.setText(user.getName());

            // Load the profile image if available
            if (user.getProfilePicture() != null) {
                Picasso.get().load(user.getProfilePicture()).into(profileImageView);
            }

            // Set up delete button click listener
            deleteButton.setOnClickListener(v -> {
                deleteUserProfile(user.getDeviceId());
            });

            // Set up view button click listener
            viewButton.setOnClickListener(v -> {
                // Set the selected user profile in UserProfileManager
                UserProfileManager.getInstance().setUserProfile(user);
                // Start ProfileDetailActivity
                Intent intent = new Intent(context, ProfileDetailActivity.class);
                context.startActivity(intent);
            });

            return rowView;
        }
    }

    /**
     * Deletes a user profile from Firestore.
     * Also removes the user from any waiting lists in event documents.
     *
     * @param deviceId the device ID of the user to delete
     */
    void deleteUserProfile(String deviceId) {
        // Log the deviceId to ensure it's correct
        Log.d("ManageProfileActivity", "Attempting to delete profile with deviceId: " + deviceId);

        db.collection("users").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                    removeFromWaitingLists(deviceId);
                    loadUserProfiles(); // Reload the list after deletion
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageProfileActivity", "Error deleting document: ", e);
                    Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Removes a user from all event waiting lists in Firestore.
     *
     * @param deviceId the device ID of the user to remove from waiting lists
     */
    void removeFromWaitingLists(String deviceId) {
        db.collection("event")
                .whereArrayContains("waitingList", deviceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("event").document(eventId)
                                    .update("waitingList", FieldValue.arrayRemove(deviceId))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ManageProfileActivity", "Removed user from waiting list of event: " + eventId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ManageProfileActivity", "Error removing user from waiting list of event: " + eventId, e);
                                    });
                        }
                    } else {
                        Log.w("ManageProfileActivity", "Error getting events with the user's waiting list", task.getException());
                    }
                });
    }
}

