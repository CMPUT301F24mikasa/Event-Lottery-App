package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminEventDetailsActivity extends AppCompatActivity {

    private ImageView eventImagePoster;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventPrice;
    private TextView eventDescription;
    private Button removeImageButton;
    private Button backButton;
    private Button removeQRButton;

    private FirebaseFirestore db;
    private String imageUrl;
    private String qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_details);

        db = FirebaseFirestore.getInstance();

        // Initialize the views
        eventImagePoster = findViewById(R.id.imgEvent);
        eventTitle = findViewById(R.id.evtTitle);
        eventDate = findViewById(R.id.evtDate);
        eventPrice = findViewById(R.id.evtPrice);
        eventDescription = findViewById(R.id.evtDesc);
        removeImageButton = findViewById(R.id.remove_button);
        backButton = findViewById(R.id.button_back);
        removeQRButton = findViewById(R.id.remove_QR_button);

        // Retrieve event ID passed via intent
        String eventId = getIntent().getStringExtra("eventId");

        // Retrieve event data from Firestore using the event ID
        if (eventId != null) {
            DocumentReference eventRef = db.collection("event").document(eventId);

            // Fetch the event data from Firestore
            eventRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String startDate = documentSnapshot.getString("startDate");
                        String price = documentSnapshot.getString("price");
                        String description = documentSnapshot.getString("description");
                        imageUrl = documentSnapshot.getString("imageURL");
                        qrCode = documentSnapshot.getString("qrCodeHash");

                        // Populate the views with the retrieved data
                        eventTitle.setText(title);
                        eventDate.setText(startDate);
                        eventPrice.setText(price);
                        eventDescription.setText(description);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(AdminEventDetailsActivity.this).load(imageUrl).into(eventImagePoster);
                        } else {
                            eventImagePoster.setImageResource(R.drawable.placeholder_image);
                        }
                    }
                }
            });
        }

        // Handle removing event poster click
        removeImageButton.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

                imageRef.delete().addOnSuccessListener(aVoid -> {
                    db.collection("event").document(eventId)
                            .update("imageURL", null)
                            .addOnSuccessListener(aVoid1 -> {
                                eventImagePoster.setImageResource(R.drawable.placeholder_image);
                                Toast.makeText(AdminEventDetailsActivity.this, "Image removed successfully", Toast.LENGTH_SHORT).show();

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("eventId", eventId);
                                setResult(RESULT_OK, resultIntent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AdminEventDetailsActivity.this, "Failed to update Firestore", Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(AdminEventDetailsActivity.this, "Failed to delete image from Storage", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(AdminEventDetailsActivity.this, "No image to remove", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle remove QR code button click
        removeQRButton.setOnClickListener(v -> {
            if (qrCode != null && !qrCode.isEmpty()) {
                db.collection("event").document(eventId)
                        .update("qrCodeHash", null, "qrCodeURL", null)
                        .addOnSuccessListener(aVoid -> {
                            qrCode = null;
                            Toast.makeText(AdminEventDetailsActivity.this, "QR Code removed successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminEventDetailsActivity.this, "Failed to update Firestore", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(AdminEventDetailsActivity.this, "No QR code to remove", Toast.LENGTH_SHORT).show();
            }
        });


        // Handle "Back" button click
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEventDetailsActivity.this, AdminManageEventsActivity.class);
            startActivity(intent);
        });
    }
}

