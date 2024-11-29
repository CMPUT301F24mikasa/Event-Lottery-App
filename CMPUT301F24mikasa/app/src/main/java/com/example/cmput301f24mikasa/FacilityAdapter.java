package com.example.cmput301f24mikasa;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * Custom ArrayAdapter to display a list of facility names, with options to view or delete each facility.
 * This adapter interfaces with Firebase Firestore to handle facility data.
 *
 * @version 1.0
 */
public class FacilityAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> facilityNames;
    private final FirebaseFirestore db;

    /**
     * Constructs a FacilityAdapter with the specified context and list of facility names.
     *
     * @param context       the current context
     * @param facilityNames the list of facility names to be displayed in the adapter
     */
    public FacilityAdapter(Context context, List<String> facilityNames) {
        super(context, 0, facilityNames);
        this.context = context;
        this.facilityNames = facilityNames;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Provides a view for an adapter item at a specified position in the list.
     * This method inflates the layout for each facility item if not already inflated,
     * and populates it with data from the facility name.
     *
     * @param position    the position of the item within the adapter's data set
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent that this view will eventually be attached to
     * @return the View for the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_item, parent, false);
        }

        String facilityDetails = facilityNames.get(position);
        TextView tvFacilityName = convertView.findViewById(R.id.txt_facility_name);
        Button btnViewFacility = convertView.findViewById(R.id.btn_view_facility);
        Button btnDeleteFacility = convertView.findViewById(R.id.btn_delete_facility); // Correct the ID

        tvFacilityName.setText(facilityDetails);

        // View Button - Opens the details page
        btnViewFacility.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewFacilityActivity.class);
            // Only pass the facility name (or other relevant data)
            String facilityName = facilityDetails.split(" - ")[0];  // Extract name
            intent.putExtra("FACILITY_NAME", facilityName);
            context.startActivity(intent);
        });

        // Delete Button - Deletes the facility from Firestore
        btnDeleteFacility.setOnClickListener(v -> deleteFacility(facilityDetails, position));

        return convertView;
    }

    /**
     * Deletes a specified facility from Firebase Firestore based on its name,
     * and updates the list to reflect the removal.
     *
     * @param facilityDetails the full string details of the facility (used to extract the facility name)
     * @param position        the position of the facility in the list
     */
    private void deleteFacility(String facilityDetails, int position) {
        String facilityName = facilityDetails.split(" - ")[0];  // Extract name from the displayed string

        db.collection("facility").whereEqualTo("facilityName", facilityName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                doc.getReference().delete();
            }
            // Update the list and notify adapter
            facilityNames.remove(position);
            notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            // Handle errors
            e.printStackTrace();
        });
    }
}
