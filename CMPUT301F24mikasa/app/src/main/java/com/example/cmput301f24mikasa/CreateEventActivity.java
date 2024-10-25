package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    // Assign variables
    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc;

    FirebaseFirestore db;
    private CollectionReference eventRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_create_event);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");


        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to database
                String title = editTextTitle.getText().toString();
                String date = editTextDate.getText().toString();
                String price = editTextPrice.getText().toString();
                String desc = editTextDesc.getText().toString();

                //TODO
                // Test data entry
                // Randomly generate eventID?
                // Find way to input capacity
                // End date?
                // Find way to store QR ref
                HashMap<String, Object> eventDetails = new HashMap<>();
                eventDetails.put("title", title);
                eventDetails.put("startDate", date);
                eventDetails.put("price", price);
                eventDetails.put("description", desc);
                eventDetails.put("capacity", 1); // edit later
                eventDetails.put("eventID", "rfu4h2"); // edit later, maybe randomly generate?
                // do we need an end date?
                eventDetails.put("cancelledEntrants", "/user/entrant");
                eventDetails.put("selectedEntrants", "/user/entrant");
                eventDetails.put("qrRef", "url to qr ref"); // edit later
                eventDetails.put("posterRef", "/media/eventMedia");

                eventRef.add(eventDetails).addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateEventActivity.this, "Event successfully created!", Toast.LENGTH_SHORT).show();
                });
                eventRef.add(eventDetails).addOnFailureListener(documentReference -> {
                    Toast.makeText(CreateEventActivity.this, "Sorry, unable to create event.", Toast.LENGTH_SHORT).show();
                });


            }
        });



    }
}
