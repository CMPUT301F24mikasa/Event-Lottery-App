package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

/**
 * EventPosterActivity is responsible for generating an event poster based
 * on the details provided by CreateEventActivity through the intent.
 * These details include the title, data, description, price, event image, and
 * QR code.
 */
public class EventPosterActivity extends AppCompatActivity {

    /**
     * Default constructor for EventPosterActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    public EventPosterActivity() {
        // Constructor is provided by default
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

        Button btnBack = findViewById(R.id.btn_back);
        Button btnSaveImage = findViewById(R.id.btnSaveImage);
        btnBack.setOnClickListener(v -> finish());

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtPrice = findViewById(R.id.txtPrice);
        TextView txtDesc = findViewById(R.id.txtDesc);
        ImageView imgEventImage = findViewById(R.id.imgEventImage);
        ImageView imgQRCode = findViewById(R.id.imgQRCode); // New ImageView for QR code

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String startDate = intent.getStringExtra("startDate");
        String desc = intent.getStringExtra("desc");
        String price = intent.getStringExtra("price");
        String imageURL = intent.getStringExtra("imageURL");
        byte[] qrCodeBytes = intent.getByteArrayExtra("qrCodeBytes"); // Retrieve QR code byte array

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

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference eventRef = db.collection("event").document(eventId);
                                eventRef.update("posterURL", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(EventPosterActivity.this, "Poster saved successfully!", Toast.LENGTH_SHORT).show();
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

    public Bitmap savePosterActivityView(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }
}