package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picker

    private EditText nameEditText, emailEditText, phoneEditText;
    private ImageView profileImageView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");
    private Uri imageUri; // URI of the selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        NavigatonActivity.setupBottomNavigation(this);

        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        profileImageView = findViewById(R.id.profile_image_view);
        Button saveProfileButton = findViewById(R.id.save_profile_button);
        Button uploadImageButton = findViewById(R.id.upload_image_button);

        // Load user data if already exists
        loadUserProfile();

        // Set up Upload Image Button
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Set up Save Profile Button
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

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



    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate mandatory fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show();
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

        // Check if an image is uploaded
        if (imageUri != null) {
            uploadImageAndSaveProfile(user);
        } else {
            // Set default image URL if no image is uploaded
            String defaultImageUrl = getRandomImageUrl(); // Assuming getRandomImageUrl() is implemented
            user.setProfilePicture(defaultImageUrl);

            // Store User object in Firestore with the default image
            saveUserToFirestore(user);
        }
    }

    private String getRandomImageUrl() {
        List<String> imageUrls = Arrays.asList(
                "https://cdn.pixabay.com/photo/2024/10/20/14/09/pumpkins-9135128_1280.jpg",
                "https://cdn.pixabay.com/photo/2024/09/25/13/12/mountains-9123540_1280.jpg",
                "https://cdn.pixabay.com/photo/2024/07/20/15/40/forest-9087654_1280.jpg",
                "https://cdn.pixabay.com/photo/2024/06/15/09/23/cat-9078123_1280.jpg",
                "https://cdn.pixabay.com/photo/2024/03/10/18/30/beach-9026541_1280.jpg"
        );

        Random random = new Random();
        return imageUrls.get(random.nextInt(imageUrls.size()));
    }

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

    private void saveUserToFirestore(UserProfile userProfile) {
        db.collection("users")
                .document(userProfile.getDeviceId())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> Toast.makeText(UserProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // Display the selected image
        }
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
