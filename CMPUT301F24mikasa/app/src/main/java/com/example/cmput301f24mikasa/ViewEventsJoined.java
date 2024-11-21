package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Activity that displays the details of an event the user has joined.
 * It shows the event title, description, date, price, and image.
 * The user can navigate back to the previous screen using the back button.
 */
public class ViewEventsJoined extends AppCompatActivity {

    /**
     * Default constructor for ViewEventsJoined.
     * This constructor is required for the Android activity lifecycle.
     */
    public ViewEventsJoined() {
        // Constructor is provided by default
    }

    /**
     * Called when the activity is created. Initializes the UI components and populates them
     * with the event details passed from the previous activity through Intent extras.
     *
     * @param savedInstanceState The saved instance state bundle from previous activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_joined_event);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> finish());

        // Initialize views
        TextView evtTitle = findViewById(R.id.evtTitle);
        TextView evtDate = findViewById(R.id.evtDate);
        TextView evtPrice = findViewById(R.id.evtPrice);
        TextView evtDesc = findViewById(R.id.evtDesc);
        ImageView evtImage = findViewById(R.id.imgEvent);

        // Retrieve intent extras
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String startDate = intent.getStringExtra("startDate");
        String desc = intent.getStringExtra("desc");
        String price = intent.getStringExtra("price");
        String imageURL = intent.getStringExtra("imageURL");

        // Set text and image views
        evtTitle.setText(title);
        evtDate.setText(startDate);
        evtDesc.setText(desc);
        evtPrice.setText("$" + price);
        Glide.with(this).load(imageURL).into(evtImage);
    }
}
