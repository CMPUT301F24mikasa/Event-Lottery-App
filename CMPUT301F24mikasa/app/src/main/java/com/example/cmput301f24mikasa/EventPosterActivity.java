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

/**
 * EventPosterActivity is responsible for generating an event poster based
 * on the details provided by CreateEventActivity through the intent.
 * These details include the title, data, description, price, event image, and
 * QR code.
 */
public class EventPosterActivity extends AppCompatActivity {

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
    }
}