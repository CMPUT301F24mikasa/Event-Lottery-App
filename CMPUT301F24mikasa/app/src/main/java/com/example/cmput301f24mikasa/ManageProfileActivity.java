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
        db.collection("admin")
                .get()
                .addOnCompleteListener(adminTask -> {
                    if (adminTask.isSuccessful()) {
                        List<String> adminDeviceIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : adminTask.getResult()) {
                            // Assuming deviceId is the field name in admin collection
                            String adminDeviceId = document.getString("deviceID");
                            if (adminDeviceId != null) {
                                adminDeviceIds.add(adminDeviceId);
                            }
                        }

                        // Now, load user profiles and filter out those belonging to admins
                        db.collection("users")
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        userProfileList.clear();
                                        for (QueryDocumentSnapshot document : userTask.getResult()) {
                                            UserProfile userProfile = document.toObject(UserProfile.class);

                                            // Only add users whose deviceId is not in the admin list
                                            if (!adminDeviceIds.contains(userProfile.getDeviceId())) {
                                                userProfileList.add(userProfile);
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.w("ManageProfileActivity", "Error getting user profiles.", userTask.getException());
                                        Toast.makeText(ManageProfileActivity.this, "Failed to load profiles", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.w("ManageProfileActivity", "Error getting admin profiles.", adminTask.getException());
                        Toast.makeText(ManageProfileActivity.this, "Failed to load admin profiles", Toast.LENGTH_SHORT).show();
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
     * @param deviceID the device ID of the user to delete
     */
    void deleteUserProfile(String deviceID) {
        db.collection("event")
                .whereEqualTo("deviceID", deviceID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String eventId = document.getId();

                        db.collection("event").document(eventId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Event deleted: " + eventId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error deleting event: " + eventId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error querying events", e));


        db.collection("event")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String eventId = document.getId();

                        db.collection("event").document(eventId)
                                .update(
                                        "waitingList", FieldValue.arrayRemove(deviceID),
                                        "selectedEntrants", FieldValue.arrayRemove(deviceID),
                                        "finalEntrants", FieldValue.arrayRemove(deviceID)
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Removed user from lists in event: " + eventId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error updating lists for event: " + eventId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error querying events", e));


        db.collection("facility")
                .whereEqualTo("ownerDeviceID", deviceID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String facilityId = document.getId();

                        db.collection("facility").document(facilityId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Facility deleted: " + facilityId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error deleting facility: " + facilityId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error querying facilities", e));


        db.collection("users")
                .whereEqualTo("deviceId", deviceID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String facilityId = document.getId();

                        db.collection("users").document(facilityId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Profile deleted: " + deviceID);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error deleting Profile: " + deviceID, e);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error querying Profiles", e));

        loadUserProfiles();
    }
}

