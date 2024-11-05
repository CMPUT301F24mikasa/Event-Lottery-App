package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FacilityProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView imgProfilePicture;
    Boolean pictureUploaded;
    Button btnUploadPicture, btnRemovePicture, btnUpdate;
    EditText editFacilityName, editFacilityLocation, editFacilityDesc;
    Uri imageUri;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_profile);

        NavigatonActivity.setupBottomNavigation(this);

        // Initialize UI components
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        editFacilityDesc = findViewById(R.id.editFacilityDesc);
        editFacilityLocation = findViewById(R.id.editFacilityLocation);
        editFacilityName = findViewById(R.id.editFacilityName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnRemovePicture = findViewById(R.id.btnRemovePicture);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        pictureUploaded = false; // No picture uploaded initially

        // Initialize Firebase and UI components
        db = FirebaseFirestore.getInstance();
        String deviceId = retrieveDeviceId();  // Get the current device ID

        // Query Firestore to get the facility associated with this device
        db.collection("facility")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Load the facility details into the UI
                        Map<String, Object> facilityData = queryDocumentSnapshots.getDocuments().get(0).getData();
                        if (facilityData != null) {
                            editFacilityName.setText((String) facilityData.get("name"));
                            editFacilityLocation.setText((String) facilityData.get("location"));
                            editFacilityDesc.setText((String) facilityData.get("description"));
                            String imageUrl = (String) facilityData.get("imageUrl");
                            if (imageUrl != null) {
                                // Load the image using Picasso
                                Picasso.get().load(imageUrl).into(imgProfilePicture);
                                pictureUploaded = true;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FacilityProfileActivity.this, "Failed to load facility data.", Toast.LENGTH_SHORT).show();
                });


        // Handle upload photo logic
        btnUploadPicture.setOnClickListener(v -> openFileChooser());

        btnRemovePicture.setOnClickListener(v -> {
            if (!pictureUploaded) {
                Toast.makeText(FacilityProfileActivity.this, "No picture to remove.", Toast.LENGTH_SHORT).show();
            } else {
                imgProfilePicture.setImageResource(R.drawable.placeholder_image); // Placeholder image
                imageUri = null;
                pictureUploaded = false;
                Toast.makeText(FacilityProfileActivity.this, "Picture removed.", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            String facilityName = editFacilityName.getText().toString();
            String facilityLocation = editFacilityLocation.getText().toString();
            String facilityDesc = editFacilityDesc.getText().toString();

            // Validate fields
            if (facilityName.isEmpty() || facilityLocation.isEmpty() || facilityDesc.isEmpty()) {
                Toast.makeText(FacilityProfileActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pictureUploaded) {
                Toast.makeText(FacilityProfileActivity.this, "Please upload a picture of your facility.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload picture if available and then save data
            if (imageUri != null) {
                uploadImageAndSaveProfile(facilityName, facilityLocation, facilityDesc);
            } else {
                saveFacilityDetails(facilityName, facilityLocation, facilityDesc, null);
            }
        });
    }

    // Method to open file chooser
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfilePicture.setImageURI(imageUri);
            pictureUploaded = true;
        }
    }

    // Method to upload image to Firebase Storage and save profile details
    private void uploadImageAndSaveProfile(String facilityName, String facilityLocation, String facilityDesc) {
        StorageReference storageRef = storage.getReference("facility_images").child(System.currentTimeMillis() + ".jpg");
        storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveFacilityDetails(facilityName, facilityLocation, facilityDesc, uri.toString());
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(FacilityProfileActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveFacilityDetails(String facilityName, String facilityLocation, String facilityDesc, String imageUrl) {
        String deviceId = retrieveDeviceId();  // Add this line to get the device ID
        Map<String, Object> facilityDetails = new HashMap<>();
        facilityDetails.put("name", facilityName);
        facilityDetails.put("location", facilityLocation);
        facilityDetails.put("description", facilityDesc);
        facilityDetails.put("deviceId", deviceId);  // Save the device ID

        if (imageUrl != null) {
            facilityDetails.put("imageUrl", imageUrl);
        }

        db.collection("facility").add(facilityDetails)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FacilityProfileActivity.this, "Facility profile successfully updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FacilityProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("HardwareIds")
    public String retrieveDeviceId() {
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}
