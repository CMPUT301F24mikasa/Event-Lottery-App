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

public class FacilityProfileActivity extends AppCompatActivity {

    ImageView imgProfilePicture;
    Boolean pictureUploaded;
    Button btnUploadPicture, btnUpdate;
    EditText editFacilityName, editFacilityLocation, editFacilityDesc;
    Uri imageUri;
    FirebaseFirestore db;
    ActivityResultLauncher<Intent> resultLauncher;
    StorageReference storageReference;
    String deviceID;




    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_facility_profile);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        editFacilityDesc = findViewById(R.id.editFacilityDesc);
        editFacilityLocation = findViewById(R.id.editFacilityLocation);
        editFacilityName = findViewById(R.id.editFacilityName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);

        pictureUploaded = false;

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("facility_images");
        loadFacilityFromDeviceID(deviceID);
        registerResult();
        btnUploadPicture.setOnClickListener(view -> pickImage());


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

            if(pictureUploaded || imageUri !=null){
                uploadFacility(facilityName, facilityLocation, facilityDesc);
            } else {
                Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void pickImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    private void loadFacilityFromDeviceID(String deviceID) {
        Query query = db.collection("facility").whereEqualTo("ownerDeviceID", deviceID);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                String facilityName = documentSnapshot.getString("facilityName");
                String facilityLocation = documentSnapshot.getString("facilityLocation");
                String facilityDesc = documentSnapshot.getString("facilityDesc");
                String imageUrl = documentSnapshot.getString("imageURL");

                btnUpdate.setText("Update");

                // Load the image if URL is present
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(imgProfilePicture);
                    imgProfilePicture.setBackground(null);
                    pictureUploaded = true; // Set pictureUploaded to true if image URL exists
                }

                editFacilityDesc.setText(facilityDesc);
                editFacilityName.setText(facilityName);
                editFacilityLocation.setText(facilityLocation);
            } else {
                Toast.makeText(this, "Please create a facility", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading facility data.", Toast.LENGTH_SHORT).show()
        );
    }

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


}

