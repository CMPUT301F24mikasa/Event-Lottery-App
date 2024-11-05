package com.example.cmput301f24mikasa;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ManageProfileActivity extends AppCompatActivity {

    private ListView userProfilesListView;
    private UserProfileArrayAdapter adapter;
    private List<UserProfile> userProfileList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profiles);

        userProfilesListView = findViewById(R.id.user_profiles_list);
        userProfileList = new ArrayList<>();
        adapter = new UserProfileArrayAdapter(this, userProfileList);
        userProfilesListView.setAdapter(adapter);

        loadUserProfiles();
    }

    private void loadUserProfiles() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userProfileList.clear(); // Clear the list to avoid duplication
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

    private class UserProfileArrayAdapter extends ArrayAdapter<UserProfile> {
        private final Context context;
        private final List<UserProfile> userList;

        public UserProfileArrayAdapter(Context context, List<UserProfile> userList) {
            super(context, R.layout.activity_manage_profile_list_item, userList);
            this.context = context;
            this.userList = userList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.activity_manage_profile_list_item, parent, false);

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

    private void deleteUserProfile(String deviceId) {
        // Log the deviceId to ensure it's correct
        Log.d("ManageProfileActivity", "Attempting to delete profile with deviceId: " + deviceId);

        db.collection("users").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUserProfiles(); // Reload the list after deletion
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageProfileActivity", "Error deleting document: ", e);
                    Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                });
    }
}

