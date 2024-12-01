package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileDetailActivity extends AppCompatActivity {
    static ImageView profileImageView;
    TextView nameTextView;
    TextView emailTextView;
    TextView phoneTextView;
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");
    static String deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileImageView = findViewById(R.id.profile_image_detail);
        nameTextView = findViewById(R.id.user_name_detail);
        emailTextView = findViewById(R.id.user_email_detail);
        phoneTextView = findViewById(R.id.user_phone_detail);

        // Retrieve the user profile and get deviceId from it
        UserProfile userProfile = UserProfileManager.getInstance().getUserProfile();
        if (userProfile != null) {
            deviceId = userProfile.getDeviceId();
            Log.d("ProfileDetailActivity", "deviceId from UserProfile: " + deviceId);

            nameTextView.setText(userProfile.getName());
            emailTextView.setText(userProfile.getGmailAddress());
            phoneTextView.setText(userProfile.getPhoneNumber());

            // Load profile image using Picasso
            if (userProfile.getProfilePicture() != null) {
                Picasso.get().load(userProfile.getProfilePicture()).into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.placeholder_image);
                profileImageView.setTag("placeholder");
            }
        } else {
            // Log error if user profile is null
            Log.e("ProfileDetailActivity", "userProfile is null");
            // Fallback to retrieving deviceId using the intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("deviceId")) {
                deviceId = intent.getStringExtra("deviceId");
                Log.d("ProfileDetailActivity", "deviceId from intent: " + deviceId);
            } else {
                Log.e("ProfileDetailActivity", "No deviceId in UserProfile or intent");
            }
        }

        Button deleteButton = findViewById(R.id.remove_image_button);
        Button backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            Intent new_intent = new Intent(ProfileDetailActivity.this, ManageProfileActivity.class);
            startActivity(new_intent);
        });

        deleteButton.setOnClickListener(v -> removeProfileImage());
    }

    public void removeProfileImage() {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e("ProfileDetailActivity", "deviceId is null or empty, cannot remove profile image");
            Toast.makeText(this, "Cannot remove profile image: Device ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("profilePicture")) {
                String profilePictureUrl = documentSnapshot.getString("profilePicture");
                if (profilePictureUrl != null) {
                    StorageReference fileReference = storageReference.child(deviceId + ".jpg");
                    // Attempt to delete the image from Firebase Storage
                    fileReference.delete().addOnSuccessListener(aVoid -> {
                        updateProfileImageView(true);
                        updateFirestoreProfilePicture();
                    }).addOnFailureListener(e -> {
                        // Even if the image doesn't exist, proceed to update Firestore
                        Log.w("ProfileDetailActivity", "Image file not found in storage. Updating Firestore only.");
                        updateProfileImageView(true);
                        updateFirestoreProfilePicture();
                    });
                } else {
                    Log.w("ProfileDetailActivity", "No profile picture URL in Firestore");
                    updateProfileImageView(false);
                }
            } else {
                Log.w("ProfileDetailActivity", "No profile document or profile picture field in Firestore");
                updateProfileImageView(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileDetailActivity", "Failed to retrieve Firestore document: ", e);
            Toast.makeText(ProfileDetailActivity.this, "Failed to retrieve profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileImageView(boolean profilePictureExists) {
        profileImageView.setImageResource(R.drawable.placeholder_image);
        profileImageView.setTag("placeholder");
        if (profilePictureExists) {
            Toast.makeText(ProfileDetailActivity.this, "Profile image removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ProfileDetailActivity.this, "No profile image to remove", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateFirestoreProfilePicture() {
        db.collection("users").document(deviceId)
                .update("profilePicture", null)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileDetailActivity", "Profile picture URL removed from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileDetailActivity", "Failed to update Firestore: ", e);
                    Toast.makeText(ProfileDetailActivity.this, "Failed to remove profile picture URL from Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
