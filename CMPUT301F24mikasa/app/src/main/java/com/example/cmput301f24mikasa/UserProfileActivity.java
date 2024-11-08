package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301f24mikasa.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Activity for managing the user's profile, including updating personal details
 * (name, email, phone number), uploading/removing profile images, and saving
 * the profile to Firebase Firestore and Firebase Storage.
 */
public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picker

    private EditText nameEditText, emailEditText, phoneEditText;
    private ImageView profileImageView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");
    private Uri imageUri; // URI of the selected image

    /**
     * Initializes the activity, setting up UI elements and button click listeners.
     * Loads the user's profile data from Firestore if available.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        profileImageView = findViewById(R.id.profile_image_view);
        Button saveProfileButton = findViewById(R.id.save_profile_button);
        Button uploadImageButton = findViewById(R.id.upload_image_button);

        Button removeImageButton = findViewById(R.id.remove_image_button);

        // Load user data if already exists
        loadUserProfile();

        // Set up Upload Image Button
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Set up Remove Image Button
        removeImageButton.setOnClickListener(v -> removeProfileImage());

        // Set up Save Profile Button
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to home screen or previous activity
                finish(); // Closes the current activity and returns to the previous one
            }
        });
    }

    /**
     * Loads the user's profile data from Firestore and displays it in the UI.
     * If the user's profile image exists, it will be displayed in the ImageView.
     */
    private void loadUserProfile() {
        String deviceId = getDeviceId(this);
        Log.d("UserProfileActivity", "Device ID: " + deviceId);
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            nameEditText.setText(user.getName() != null ? user.getName() : "");
                            emailEditText.setText(user.getGmailAddress() != null ? user.getGmailAddress() : "");
                            phoneEditText.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                            // Load the profile image if exists
                            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                                Picasso.get().load(user.getProfilePicture()).into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.placeholder_image); // Set a default image or clear the ImageView
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }


    /**
     * Saves the user's profile to Firestore and uploads the profile image to Firebase Storage.
     * If no image is selected, a random default image URL is set for the profile.
     */
    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate mandatory fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assign phone number as null if the field is empty
        if (TextUtils.isEmpty(phone)) {
            phone = null;
        }

        // Get unique device ID
        String deviceId = getDeviceId(this);

        // Create a User object with phone being either the entered value or null
        UserProfile user = new UserProfile(name, null, deviceId, email, phone);

        // Initialize eventsJoined array (it's already done in the constructor, but ensure it's there)
        user.setEventsJoined(new ArrayList<>());  // Create an empty list for events the user will join

        // Check if an image is uploaded
        if (imageUri != null) {
            uploadImageAndSaveProfile(user);
        } else {
            // Set default image URL if no image is uploaded
            String defaultImageUrl = getRandomImageUrl();
            user.setProfilePicture(defaultImageUrl);

            // Store User object in Firestore with the default image
            saveUserToFirestore(user);
        }

        // After saving successfully, set result and finish
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Returns a random default image URL from a predefined list.
     *
     * @return A randomly selected default image URL.
     */
    private String getRandomImageUrl() {
        List<String> imageUrls = Arrays.asList(
                "https://cdn.pixabay.com/photo/2024/10/20/14/09/pumpkins-9135128_1280.jpg",
                "https://cdn.pixabay.com/photo/2023/12/21/07/39/christmas-baubles-8461383_1280.jpg",
                "https://cdn.pixabay.com/photo/2024/10/13/07/48/leaves-9116635_1280.png"
        );

        Random random = new Random();
        return imageUrls.get(random.nextInt(imageUrls.size()));
    }

    /**
     * Uploads the selected profile image to Firebase Storage and updates the user's profile in Firestore.
     *
     * @param userProfile The user profile to update with the image URL after upload.
     */
    private void uploadImageAndSaveProfile(UserProfile userProfile) {
        StorageReference fileReference = storageReference.child(userProfile.getDeviceId() + ".jpg");
        UploadTask uploadTask = fileReference.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                userProfile.setProfilePicture(uri.toString());
                saveUserToFirestore(userProfile);
            });
        }).addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves the user's profile data to Firestore.
     *
     * @param userProfile The user profile to save to Firestore.
     */
    private void saveUserToFirestore(UserProfile userProfile) {
        db.collection("users")
                .document(userProfile.getDeviceId())  // Use deviceId as the document ID
                .set(userProfile)  // Store the full UserProfile object
                .addOnSuccessListener(aVoid -> Toast.makeText(UserProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }

    /**
     * Opens a file chooser to allow the user to select an image from their device.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image picker activity. Sets the selected image URI to the ImageView.
     *
     * @param requestCode The request code from the image picker.
     * @param resultCode The result code of the activity.
     * @param data The intent containing the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // Display the selected image
        }
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


    /**
     * Removes the user's current profile image and updates the Firestore profile.
     */
    // Remove Profile Image Feature
    private void removeProfileImage() {
        // Check if there's an image currently loaded but not uploaded
        if (imageUri != null) {
            // Clear the selected image URI and reset the ImageView
            imageUri = null;
            profileImageView.setImageResource(R.drawable.placeholder_image); // Set to default or placeholder image
            Toast.makeText(UserProfileActivity.this, "Selected image removed", Toast.LENGTH_SHORT).show();
            return;
        }

        // If no imageUri, check if there's an existing profile image in Firebase
        String deviceId = getDeviceId(this);
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profilePicture") && documentSnapshot.getString("profilePicture") != null) {
                        // Only proceed to delete if a profile picture URL is set
                        StorageReference fileReference = storageReference.child(deviceId + ".jpg");
                        fileReference.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully deleted image from storage
                                    Toast.makeText(UserProfileActivity.this, "Profile image removed", Toast.LENGTH_SHORT).show();
                                    profileImageView.setImageResource(R.drawable.placeholder_image);

                                    // Remove profile picture URL from Firestore
                                    db.collection("users").document(deviceId)
                                            .update("profilePicture", null)
                                            .addOnSuccessListener(aVoid1 -> Log.d("UserProfileActivity", "Profile picture URL removed from Firestore"))
                                            .addOnFailureListener(e -> Log.e("UserProfileActivity", "Failed to remove profile picture URL from Firestore", e));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserProfileActivity", "Failed to remove profile image", e);
                                    Toast.makeText(UserProfileActivity.this, "Failed to remove profile image", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No profile picture exists in Firestore to delete
                        Toast.makeText(UserProfileActivity.this, "No profile image to remove", Toast.LENGTH_SHORT).show();
                        profileImageView.setImageResource(R.drawable.placeholder_image); // Set a default image if desired
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to check profile image", Toast.LENGTH_SHORT).show());
    }
}
