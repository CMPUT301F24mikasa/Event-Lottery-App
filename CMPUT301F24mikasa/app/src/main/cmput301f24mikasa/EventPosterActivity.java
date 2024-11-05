package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class EventPosterActivity extends AppCompatActivity {

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
        btnBack.setOnClickListener(v -> finish());

        // Initialize views
        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtPrice = findViewById(R.id.txtPrice);
        TextView txtDesc = findViewById(R.id.txtDesc);
        ImageView imgEventImage = findViewById(R.id.imgEventImage);
        ImageView imgQRCode = findViewById(R.id.imgQRCode); // New ImageView for QR code

        // Retrieve intent extras
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String startDate = intent.getStringExtra("startDate");
        String desc = intent.getStringExtra("desc");
        String price = intent.getStringExtra("price");
        String imageURL = intent.getStringExtra("imageURL");
        byte[] qrCodeBytes = intent.getByteArrayExtra("qrCodeBytes"); // Retrieve QR code byte array

        // Set text and image views
        txtTitle.setText(title);
        txtDate.setText(startDate);
        txtDesc.setText(desc);
        txtPrice.setText("$" + price);
        Glide.with(this).load(imageURL).into(imgEventImage);

        // Convert the QR code byte array to a Bitmap and display it
        if (qrCodeBytes != null) {
            Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(qrCodeBytes, 0, qrCodeBytes.length);
            imgQRCode.setImageBitmap(qrCodeBitmap);
        }
    }
}