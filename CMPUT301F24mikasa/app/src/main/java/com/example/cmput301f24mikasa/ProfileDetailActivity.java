package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

/**
 * ProfileDetailActivity displays detailed information about the user profile,
 * including the user's name, email, phone number, and profile image.
 * This activity retrieves the user's profile from the UserProfileManager
 * and populates the relevant views in the UI with the user's details.
 */
public class ProfileDetailActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");
    private String deviceId;

    /**
     * Default constructor for ProfileDetailActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ProfileDetailActivity() {
        // Constructor is provided by default
    }

    /**
     * Called when the activity is first created.
     * Initializes the views and populates them with data from the UserProfile.
     * The user profile is retrieved from the UserProfileManager.
     *
     * @param savedInstanceState a bundle containing the activity's previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileImageView = findViewById(R.id.profile_image_detail);
        Button deleteButton = findViewById(R.id.remove_image_button);
        Button backButton = findViewById(R.id.back_button);
        nameTextView = findViewById(R.id.user_name_detail);
        emailTextView = findViewById(R.id.user_email_detail);
        phoneTextView = findViewById(R.id.user_phone_detail);

        deviceId = getDeviceId(this);

        // Retrieve the user profile from UserProfileManager
        UserProfile userProfile = UserProfileManager.getInstance().getUserProfile();

        if (userProfile != null) {
            nameTextView.setText(userProfile.getName());
            emailTextView.setText(userProfile.getGmailAddress());
            phoneTextView.setText(userProfile.getPhoneNumber());

            // Load the profile image if available
            if (userProfile.getProfilePicture() != null) {
                Picasso.get().load(userProfile.getProfilePicture()).into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileDetailActivity.this, ManageProfileActivity.class);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> removeProfileImage());
    }

    /**
     * Returns the unique device ID for the current user's device.
     *
     * @param context The context of the current activity.
     * @return The unique device ID.
     */
    @SuppressLint("HardwareIds")
    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void removeProfileImage() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profilePicture") && documentSnapshot.getString("profilePicture") != null) {
                        StorageReference fileReference = storageReference.child(deviceId + ".jpg");
                        fileReference.delete()
                                .addOnSuccessListener(aVoid -> {
                                    profileImageView.setImageResource(R.drawable.placeholder_image);
                                    db.collection("users").document(deviceId)
                                            .update("profilePicture", null)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(ProfileDetailActivity.this, "Profile image removed", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(ProfileDetailActivity.this, "Failed to remove profile picture URL from Firestore", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileDetailActivity.this, "Failed to remove profile image", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(ProfileDetailActivity.this, "No profile image to remove", Toast.LENGTH_SHORT).show();
                        profileImageView.setImageResource(R.drawable.placeholder_image);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileDetailActivity.this, "Failed to check profile image", Toast.LENGTH_SHORT).show());
    }
}