package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    FirebaseFirestore db;
    Button btnBack;

    /**
     * Default constructor for ViewFacilityActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ViewFacilityActivity() {
        // Constructor is provided by default
    }

    /**
     * Called when the activity is created.
     * Initializes the views, retrieves the facility name from the intent,
     * and loads the facility details from Firebase Firestore.
     *
     * @param savedInstanceState The saved instance state bundle from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_facility);

        // Initialize views
        tvFacilityName = findViewById(R.id.tv_facility_name);
        tvFacilityDescription = findViewById(R.id.tv_facility_description);
        tvFacilityLocation = findViewById(R.id.tv_facility_location);
        btnBack = findViewById(R.id.btn_back); // Back button

        db = FirebaseFirestore.getInstance();

        String facilityName = getIntent().getStringExtra("FACILITY_NAME");

        if (facilityName != null) {
            loadFacilityDetails(facilityName);
        }

        // Back button functionality
        btnBack.setOnClickListener(v -> finish());  // This will return to the previous activity
    }

    /**
     * Loads the facility details from Firebase Firestore.
     * This method queries the "facility" collection and retrieves the details
     * of the facility matching the given facility name.
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
                }
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
}
