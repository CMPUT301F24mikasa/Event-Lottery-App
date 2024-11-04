package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;
import javax.annotation.Nullable;

public class ViewFacilityActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvFacilityName, tvFacilityLocation, tvFacilityDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_facility);

        db = FirebaseFirestore.getInstance();

        tvFacilityName = findViewById(R.id.tv_facility_name);
        tvFacilityLocation = findViewById(R.id.tv_facility_location);
        tvFacilityDescription = findViewById(R.id.tv_facility_description);
        Button btnBack = findViewById(R.id.btn_back);

        // Back button to return to Manage Facilities screen
        btnBack.setOnClickListener(v -> finish());

        // Get the facility name from the intent
        String facilityName = getIntent().getStringExtra("FACILITY_NAME");

        // Load facility details from Firestore
        loadFacilityDetails(facilityName);
    }

    private void loadFacilityDetails(String facilityName) {
        DocumentReference facilityRef = db.collection("facilities").document(facilityName);
        facilityRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    tvFacilityName.setText(snapshot.getString("name"));
                    tvFacilityLocation.setText(snapshot.getString("location"));
                    tvFacilityDescription.setText(snapshot.getString("description"));
                }
            }
        });
    }
}
