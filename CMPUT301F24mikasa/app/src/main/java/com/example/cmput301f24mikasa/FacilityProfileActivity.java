package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FacilityProfileActivity extends AppCompatActivity {

    ImageView imgProfilePicture;
    Boolean pictureUploaded;
    Button btnUploadPicture, btnRemovePicture, btnUpdate;
    EditText editFacilityName, editFacilityLocation, editFacilityDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_facility_profile);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Corrected findViewById calls
        editFacilityDesc = findViewById(R.id.editFacilityDesc);
        editFacilityLocation = findViewById(R.id.editFacilityLocation);
        editFacilityName = findViewById(R.id.editFacilityName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnRemovePicture = findViewById(R.id.btnRemovePicture);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);

        pictureUploaded = false;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Handle upload photo logic
        btnUploadPicture.setOnClickListener(v -> {
            pictureUploaded = true;
        });

        btnRemovePicture.setOnClickListener(v -> {
            if (!pictureUploaded) {
                Toast.makeText(FacilityProfileActivity.this, "No picture to remove.", Toast.LENGTH_SHORT).show();
            } else {
                // Handle remove picture logic
            }
        });

        btnUpdate.setOnClickListener(v -> {
            if (!pictureUploaded) {
                Toast.makeText(FacilityProfileActivity.this, "Please upload a picture of your facility.", Toast.LENGTH_SHORT).show();
                return;
            }

            String facilityName = editFacilityName.getText().toString();
            String facilityLocation = editFacilityLocation.getText().toString();
            String facilityDesc = editFacilityDesc.getText().toString();

            Map<String, Object> facilityDetails = new HashMap<>();
            facilityDetails.put("name", facilityName);
            facilityDetails.put("facilityLocation", facilityLocation);
            facilityDetails.put("facilityDesc", facilityDesc);

            db.collection("facility").add(facilityDetails).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(FacilityProfileActivity.this, "Facility profile successfully updated.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FacilityProfileActivity.this, "Failed to update profile changes", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
