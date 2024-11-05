package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class ManageFacilitiesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<String> facilitiesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_facilities);

        NavigatonActivity.setupBottomNavigation(this);
        
        db = FirebaseFirestore.getInstance();
        facilitiesList = new ArrayList<>();

        ListView lvFacilities = findViewById(R.id.lv_facilities);
        EditText etSearchFacility = findViewById(R.id.et_search_facility);
        Button btnBack = findViewById(R.id.btn_back);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, facilitiesList);
        lvFacilities.setAdapter(adapter);

        loadFacilities();

        // Back button to return to Admin Dashboard
        btnBack.setOnClickListener(v -> finish());

        // View and delete functionality
        lvFacilities.setOnItemClickListener((parent, view, position, id) -> {
            String facilityName = facilitiesList.get(position);
            Intent intent = new Intent(ManageFacilitiesActivity.this, ViewFacilityActivity.class);
            intent.putExtra("FACILITY_NAME", facilityName);
            startActivity(intent);
        });

        lvFacilities.setOnItemLongClickListener((parent, view, position, id) -> {
            String facilityName = facilitiesList.get(position);
            deleteFacility(facilityName);
            return true;
        });

        // Search functionality
        etSearchFacility.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    private void loadFacilities() {
        CollectionReference facilitiesRef = db.collection("facilities");
        facilitiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                facilitiesList.clear();
                assert value != null;
                for (QueryDocumentSnapshot doc : value) {
                    String facilityName = doc.getString("name");
                    facilitiesList.add(facilityName);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteFacility(String facilityName) {
        CollectionReference facilitiesRef = db.collection("facilities");
        facilitiesRef.whereEqualTo("name", facilityName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                doc.getReference().delete();
            }
            facilitiesList.remove(facilityName);
            adapter.notifyDataSetChanged();
        });
    }
}
