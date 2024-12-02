package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity that displays details of a specific facility.
 * The facility information is fetched from Firebase Firestore based on the facility name.
 * It includes the facility's name, description, and location.
 * The user can navigate back to the previous screen using the back button.
 */
public class ViewFacilityActivity extends AppCompatActivity {
    TextView tvFacilityName;
    TextView tvFacilityDescription;
    TextView tvFacilityLocation;
    ImageView ivFacilityImage;
    FirebaseFirestore db;
    Button btnBack;
    Button removeImageButton;

    /**
     * Default constructor for ViewFacilityActivity.
     */
    public ViewFacilityActivity() {
    }

    /**
     * This method initializes the views, retrieves the facility name from the intent,
     * and loads the facility details from Firebase Firestore.
     *
     * @param savedInstanceState The saved instance state bundle from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_facility);

        // Initialize UI elements
        tvFacilityName = findViewById(R.id.tv_facility_name);
        tvFacilityDescription = findViewById(R.id.tv_facility_description);
        tvFacilityLocation = findViewById(R.id.tv_facility_location);
        ivFacilityImage = findViewById(R.id.facility_image);
        btnBack = findViewById(R.id.btn_back); // Back button
        removeImageButton = findViewById(R.id.remove_image_button);

        db = FirebaseFirestore.getInstance();

        // Retrieve the facility name from the intent
        String facilityName = getIntent().getStringExtra("FACILITY_NAME");

        if (facilityName != null) {
            loadFacilityDetails(facilityName);
        }

        // Back button functionality
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ViewFacilityActivity.this, ManageFacilitiesActivity.class);
            startActivity(intent);
        });

        // Set up the remove image button to remove the facility image
        removeImageButton.setOnClickListener(v -> {
            db.collection("facility").whereEqualTo("facilityName", facilityName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        String imageURL = document.getString("imageURL");

                        if (imageURL != null && !imageURL.isEmpty()) {
                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

                            imageRef.delete().addOnSuccessListener(aVoid -> {
                                db.collection("facility").document(documentId)
                                        .update("imageURL", null)
                                        .addOnSuccessListener(aVoid1 -> {
                                            ivFacilityImage.setImageResource(R.drawable.placeholder_image);
                                            Toast.makeText(this, "Image removed successfully.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update Firestore.", Toast.LENGTH_SHORT).show();
                                        });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to remove image from Firebase Storage.", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Toast.makeText(this, "No image to remove.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load facility details for image removal.", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * This method queries the "facility" collection and retrieves the details
     * of the facility matching the given facility name from Firebase Firestore.
     *
     * @param facilityName The name of the facility to load details for.
     */
    void loadFacilityDetails(String facilityName) {
        db.collection("facility").whereEqualTo("facilityName", facilityName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    tvFacilityName.setText(document.getString("facilityName"));
                    tvFacilityDescription.setText(document.getString("facilityDesc"));
                    tvFacilityLocation.setText(document.getString("facilityLocation"));

                    String imageURL = document.getString("imageURL");
                    if (imageURL != null && !imageURL.isEmpty()) {
                        Glide.with(this).load(imageURL).into(ivFacilityImage);
                    } else {
                        ivFacilityImage.setImageResource(R.drawable.placeholder_image);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();  // Handle exceptions and errors
        });
    }
}
