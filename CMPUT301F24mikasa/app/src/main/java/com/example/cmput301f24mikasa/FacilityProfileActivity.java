package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.bumptech.glide.Glide;


import java.util.HashMap;
import java.util.Map;

/**
 * The FacilityProfileActivity class provides the UI and functionality for managing a facility profile.
 * This activity allows users to upload an image, view, create, and update their facility details,
 * which are stored in Firebase Firestore and Firebase Storage.
 */
public class FacilityProfileActivity extends AppCompatActivity {

    /**
     * Default constructor for FacilityProfileActivity.
     */
    public FacilityProfileActivity() {
    }

    // Initialize UI elements
    ImageView imgProfilePicture;
    Boolean pictureUploaded;
    Button btnUploadPicture, btnUpdate;
    EditText editFacilityName, editFacilityLocation, editFacilityDesc;
    Uri imageUri;

    FirebaseFirestore db;
    ActivityResultLauncher<Intent> resultLauncher;
    StorageReference storageReference;
    String deviceID;
    private boolean isRemovingImage = false;


    /**
     * Initializes the activity, sets up UI components, retrieves the device ID, and loads the facility
     * profile associated with the device if it exists in Firestore.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_profile);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Handle Back button click
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        editFacilityDesc = findViewById(R.id.editFacilityDesc);
        editFacilityLocation = findViewById(R.id.editFacilityLocation);
        editFacilityName = findViewById(R.id.editFacilityName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);

        // Handle Remove image button click
        Button btnRemovePicture = findViewById(R.id.btnRemovePicture);
        btnRemovePicture.setOnClickListener(v -> removeProfileImage());


        pictureUploaded = false;

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("facility_images");
        loadFacilityFromDeviceID(deviceID);
        registerResult();
        btnUploadPicture.setOnClickListener(view -> pickImage());

        // Handle update image button click
        btnUpdate.setOnClickListener(v -> {
            if (!pictureUploaded) {
                Toast.makeText(FacilityProfileActivity.this, "Please upload a picture of your facility.", Toast.LENGTH_SHORT).show();
                return;
            }

            String facilityName = editFacilityName.getText().toString();
            String facilityLocation = editFacilityLocation.getText().toString();
            String facilityDesc = editFacilityDesc.getText().toString();

            if (facilityName.isEmpty()) {
                Toast.makeText(FacilityProfileActivity.this, "Please enter a facility name.", Toast.LENGTH_SHORT).show();
                return;
            }

            if( pictureUploaded || imageUri != null){
                uploadFacility(facilityName, facilityLocation, facilityDesc);
            } else {
                Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    /**
     * Opens the image picker to select a profile picture.
     */
    private void pickImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    /**
     * Loads the facility profile associated with the device ID from Firestore.
     *
     * @param deviceID The unique ID of the device to fetch facility data associated with it.
     */
    private void loadFacilityFromDeviceID(String deviceID){
        Query query = db.collection("facility").whereEqualTo("ownerDeviceID", deviceID);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (!queryDocumentSnapshots.isEmpty()) {


                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                String facilityName = documentSnapshot.getString("facilityName");
                String facilityLocation = documentSnapshot.getString("facilityLocation");
                String facilityDesc = documentSnapshot.getString("facilityDesc");
                String imageUrl = documentSnapshot.getString("imageURL");

                btnUpdate.setText("Update");

                // https://github.com/bumptech/glide, Referenced 2024-10-31
                Glide.with(this).load(imageUrl).into(imgProfilePicture);
                imgProfilePicture.setBackground(null);


                editFacilityDesc.setText(facilityDesc);
                editFacilityName.setText(facilityName);
                editFacilityLocation.setText(facilityLocation);
            } else{
                Toast.makeText(this, "Please create a facility", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Uploads the facility profile details and image to Firebase Firestore and Firebase Storage.
     *
     * @param facilityName     The name of the facility.
     * @param facilityLocation The location of the facility.
     * @param facilityDesc     The description of the facility.
     */
    private void uploadFacility(String facilityName, String facilityLocation, String facilityDesc){
        StorageReference fileReference = storageReference.child(facilityName +".jpg");

        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Map<String, Object> facilityDetails = new HashMap<>();
                        facilityDetails.put("facilityName", facilityName);
                        facilityDetails.put("facilityLocation", facilityLocation);
                        facilityDetails.put("facilityDesc", facilityDesc);
                        facilityDetails.put("imageURL", uri.toString());
                        facilityDetails.put("ownerDeviceID", deviceID); // Store the owner device ID


                        db.collection("facility").document(facilityName).set(facilityDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(FacilityProfileActivity.this, "Facility profile has been updated.", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FacilityProfileActivity.this, "Sorry, facility profile could not be updated. Please try again.", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FacilityProfileActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    /**
     * Registers an activity result for image selection to update the profile picture.
     */
    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK && result.getData() != null){
                            imageUri = result.getData().getData();
                            imgProfilePicture.setImageURI(imageUri);
                            pictureUploaded = true;

                        } else{
                            Toast.makeText(FacilityProfileActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /**
     * Removes the profile image for the facility, either locally or from the database and storage.
     */
    private void removeProfileImage() {
        // Check if the image removal process is already in progress
        if (isRemovingImage) {
            // Notify the user to wait until the ongoing process completes
            Toast.makeText(this, "Please wait, removing image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark the start of the image removal process
        isRemovingImage = true;

        // If there is a local image URI, remove it and reset the profile picture to a placeholder
        if (imageUri != null) {
            imageUri = null; // Clear the local image URI
            imgProfilePicture.setImageResource(R.drawable.placeholder_image); // Set placeholder image
            Toast.makeText(FacilityProfileActivity.this, "Selected image removed.", Toast.LENGTH_SHORT).show();
            isRemovingImage = false; // Mark the removal process as complete
            return;
        }

        // Fetch the facility's record from the database using the device ID
        db.collection("facility").whereEqualTo("ownerDeviceID", deviceID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any facility record exists
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Retrieve the first matching facility document
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        // Get the image URL from the document
                        String imageUrl = documentSnapshot.getString("imageURL");

                        // If an image URL exists, remove the image from Firebase Storage
                        if (imageUrl != null) {
                            StorageReference fileReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            fileReference.delete().addOnSuccessListener(aVoid -> {
                                // Notify the user of successful removal and reset the profile picture
                                Toast.makeText(FacilityProfileActivity.this, "Profile image removed", Toast.LENGTH_SHORT).show();
                                imgProfilePicture.setImageResource(R.drawable.placeholder_image);

                                // Remove the image URL reference from the database
                                documentSnapshot.getReference().update("imageURL", null)
                                        .addOnSuccessListener(aVoid1 ->
                                                Toast.makeText(FacilityProfileActivity.this, "Image reference removed", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> e.printStackTrace()); // Log any errors during database update

                                // Mark the removal process as complete
                                isRemovingImage = false;
                            }).addOnFailureListener(e -> {
                                // Notify the user of failure to remove the image from storage
                                Toast.makeText(FacilityProfileActivity.this, "Failed to remove image from storage", Toast.LENGTH_SHORT).show();
                                isRemovingImage = false; // Mark the removal process as complete
                            });
                        } else {
                            // If no image URL exists, reset the profile picture and notify the user
                            Toast.makeText(FacilityProfileActivity.this, "No profile image to remove", Toast.LENGTH_SHORT).show();
                            imgProfilePicture.setImageResource(R.drawable.placeholder_image);
                            isRemovingImage = false; // Mark the removal process as complete
                        }
                    } else {
                        // Notify the user if the facility record is not found
                        Toast.makeText(FacilityProfileActivity.this, "Facility not found", Toast.LENGTH_SHORT).show();
                        isRemovingImage = false; // Mark the removal process as complete
                    }
                })
                .addOnFailureListener(e -> {
                    // Notify the user if the database query fails
                    Toast.makeText(FacilityProfileActivity.this, "Failed to check profile image", Toast.LENGTH_SHORT).show();
                    isRemovingImage = false; // Mark the removal process as complete
                });
    }

}
