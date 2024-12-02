package com.example.cmput301f24mikasa;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * EventPosterActivity is responsible for generating an event poster based
 * on the details provided by CreateEventActivity through the intent.
 * These details include the title, data, description, price, event image, and
 * QR code.
 */
public class EventPosterActivity extends AppCompatActivity {

    /**
     * Default constructor for EventPosterActivity.
     */
    public EventPosterActivity() {
    }

    /**
     * onCreate creates the EventPosterActivity, it retrieves data
     * from the intent passed by CreateEventActivity, including the the title, data,
     * description, price, event image, and QR code.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_poster);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSaveImage = findViewById(R.id.btnSaveImage);

        // Handle "Back" button click
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EventPosterActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        // Initialize UI Elements
        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtPrice = findViewById(R.id.txtPrice);
        TextView txtDesc = findViewById(R.id.txtDesc);
        ImageView imgEventImage = findViewById(R.id.imgEventImage);
        ImageView imgQRCode = findViewById(R.id.imgQRCode);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String startDate = intent.getStringExtra("startDate");
        String desc = intent.getStringExtra("desc");
        String price = intent.getStringExtra("price");
        String imageURL = intent.getStringExtra("imageURL");

        // Retrieve QR code byte array
        byte[] qrCodeBytes = intent.getByteArrayExtra("qrCodeBytes");

        txtTitle.setText(title);
        txtDate.setText(startDate);
        txtDesc.setText(desc);
        txtPrice.setText("$" + price);
        Glide.with(this).load(imageURL).into(imgEventImage);

        if (qrCodeBytes != null) {
            Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(qrCodeBytes, 0, qrCodeBytes.length);
            imgQRCode.setImageBitmap(qrCodeBitmap);
        }

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnBack.setVisibility(View.INVISIBLE);
                btnSaveImage.setVisibility(View.INVISIBLE);

                Bitmap bitmap = savePosterActivityView(findViewById(R.id.main));

                btnBack.setVisibility(View.VISIBLE);
                btnSaveImage.setVisibility(View.VISIBLE);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                String eventId = intent.getStringExtra("eventID");
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference posterRef = storageRef.child("event_posters/" + eventId + ".png");

                posterRef.putBytes(imageData)
                        .addOnSuccessListener(taskSnapshot -> {
                            posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();

                                saveImageToGallery(bitmap, eventId);


                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference eventRef = db.collection("event").document(eventId);
                                eventRef.update("posterURL", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(EventPosterActivity.this, "Poster saved successfully to Firestore.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(EventPosterActivity.this, "Failed to save poster URL to Firestore.", Toast.LENGTH_SHORT).show();
                                        });
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EventPosterActivity.this, "Failed to upload poster image.", Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    // Referenced https://stackoverflow.com/questions/36624756/how-to-save-bitmap-to-android-gallery by Bao Lei, 2024-11-28
    // Referenced https://github.com/LHM777/Scoped-Storage-Android-11-java-example-Save-bitmap-in-Android-using-MediaStore by LHM777, 2024-11-28
    // Referenced https://www.youtube.com/watch?v=tYQ8AO58Aj0&ab_channel=GenericApps by Generic Apps, 2024-11-28
    private void saveImageToGallery(Bitmap bitmap, String eventID) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "event_poster_" + eventID + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EventPosters");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(this, "Event poster has been saved to gallery!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to write image file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to save poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to create MediaStore entry for poster.", Toast.LENGTH_SHORT).show();
        }
    }

    // Referenced https://dev.to/pranavpandey/android-create-bitmap-from-a-view-3lck by Pranav Pandey, 2024-11-30
    /**
     * Captures the current state of the provided view as a bitmap.
     *
     * @param view The view to capture as an image.
     * @return A bitmap representing the captured view.
     */
    public Bitmap savePosterActivityView(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }
}