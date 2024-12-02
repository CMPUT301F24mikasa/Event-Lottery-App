package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MapActivity is responsible for displaying a Google Map with markers for locations
 * retrieved from a Firestore document associated with a specific event ID.
 * It uses the Geocoding API to convert location addresses into latitude and longitude coordinates.
 *
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GeocodingApi geocodingApi;
    private String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initializeRetrofit();

        // Retrieve the eventID passed from the previous activity
        eventID = getIntent().getStringExtra("eventID");
        if (eventID != null) {
            fetchAndMarkLocations(eventID);
        } else {
            System.err.println("Error: eventID not found");
        }
    }

    private void initializeRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.opencagedata.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geocodingApi = retrofit.create(GeocodingApi.class);
    }

    /**
     * Called when the map is ready. Sets the initial camera position to the center of the world
     * and zooms out. Fetches and marks locations based on the provided eventID.
     *
     * @param map The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Center the map on the world
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 2));

        // Fetch and mark locations using the eventID
        if (eventID != null) {
            fetchAndMarkLocations(eventID); // Pass eventID to the method
        } else {
            Toast.makeText(this, "Event ID is missing!", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndConvertLocation(String location, OnLocationValidatedListener listener) {
        String apiKey = getResources().getString(R.string.open_cage_api_key);

        // Call the non-static getCoordinates method on the geocodingApi instance
        geocodingApi.getCoordinates(location, apiKey).enqueue(new retrofit2.Callback<GeocodeResponse>() {
            @Override
            public void onResponse(Call<GeocodeResponse> call, retrofit2.Response<GeocodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeocodeResponse geocodeResponse = response.body();
                    if (geocodeResponse.results != null && !geocodeResponse.results.isEmpty()) {
                        GeocodeResponse.Result.Geometry geometry = geocodeResponse.results.get(0).geometry;
                        LatLng latLng = new LatLng(geometry.lat, geometry.lng);
                        listener.onSuccess(latLng);
                    } else {
                        listener.onFailure("No valid location found");
                    }
                } else {
                    listener.onFailure("API error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GeocodeResponse> call, Throwable t) {
                listener.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    private void fetchAndMarkLocations(String eventID) {
        db.collection("event")
                .document(eventID) // Query only the document with this eventID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the LocationList field
                        Map<String, String> locationMap = (Map<String, String>) documentSnapshot.get("LocationList");
                        if (locationMap != null) {
                            for (Map.Entry<String, String> entry : locationMap.entrySet()) {
                                String location = entry.getValue();
                                validateAndConvertLocation(location, new OnLocationValidatedListener() {
                                    @Override
                                    public void onSuccess(LatLng latLng) {
                                        addMarker(latLng, entry.getKey());
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        System.err.println("Error for location: " + location + " - " + errorMessage);
                                    }
                                });
                            }
                        }
                    } else {
                        System.err.println("Error: Event document with ID " + eventID + " does not exist.");
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void addMarker(LatLng latLng, String deviceId) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Listener interface for handling the result of a location validation process.
     * Used for notifying when a location has been successfully validated or if an error occurs.
     */
    public interface OnLocationValidatedListener {
        void onSuccess(LatLng latLng);
        void onFailure(String errorMessage);
    }
}
