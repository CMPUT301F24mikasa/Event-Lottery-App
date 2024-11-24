package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * ManageFacilitiesActivity is responsible for displaying and managing the list of facilities in the admin interface.
 * It allows the admin to view the list of facilities, each with its name, description, and location.
 * The admin can also navigate to a detailed view of a specific facility.
 */
public class ManageFacilitiesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<String> facilitiesList;
    private FacilityAdapter adapter;

    /**
     * Default constructor for ManageFacilitiesActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public ManageFacilitiesActivity() {
        // Constructor is provided by default
    }


    /**
     * Initializes the activity, sets up the ListView to display the list of facilities,
     * and loads the facility data from Firebase Firestore.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_facilities);

        db = FirebaseFirestore.getInstance();
        facilitiesList = new ArrayList<>();

        ListView lvFacilities = findViewById(R.id.lv_facilities);
        Button btnBack = findViewById(R.id.btn_back);

        adapter = new FacilityAdapter(this, facilitiesList);
        lvFacilities.setAdapter(adapter);

        // Load facilities into the list
        loadFacilities();

        // Back button to return to Admin Dashboard
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ManageFacilitiesActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Set up item click listener for the ListView
        lvFacilities.setOnItemClickListener((parent, view, position, id) -> {
            // Get the facility name at the clicked position
            String facilityName = facilitiesList.get(position);

            // Create an Intent to view the facility details
            Intent intent = new Intent(ManageFacilitiesActivity.this, ViewFacilityActivity.class);

            // Pass the facility name to the ViewFacilityActivity
            intent.putExtra("FACILITY_NAME", facilityName);

            // Start the activity
            startActivity(intent);
        });
    }

    /**
     * Loads the list of facilities from the Firestore "facility" collection and updates the list displayed in the UI.
     * This method listens for changes to the "facility" collection in real-time and updates the list accordingly.
     */
    private void loadFacilities() {
        CollectionReference facilitiesRef = db.collection("facility");
        facilitiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            facilitiesList.clear();
            assert value != null;
            for (QueryDocumentSnapshot doc : value) {
                String name = doc.getString("facilityName");
                String description = doc.getString("facilityDesc");
                String location = doc.getString("facilityLocation");

                // Simple display with name, description, and location
                String displayText = name;
                facilitiesList.add(displayText);
            }
            adapter.notifyDataSetChanged();
        });
    }
}
