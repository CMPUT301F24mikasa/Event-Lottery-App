package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {
    // Assign variables
    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc;
    Boolean eventCreated;

    ActivityResultLauncher<Intent> resultLauncher;

    FirebaseFirestore db;
    private CollectionReference eventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_create_event);
        eventCreated = false;

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        registerResult();
        btnUpload.setOnClickListener(view -> pickImage());

        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        DocumentReference documentReference = eventRef.document(); // EventID
        String eventID = documentReference.getId();

        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to database
                String title = editTextTitle.getText().toString();
                String date = editTextDate.getText().toString();
                String price = editTextPrice.getText().toString();
                String desc = editTextDesc.getText().toString();

                if (title.isEmpty()||date.isEmpty()||price.isEmpty()||desc.isEmpty()){
                    Toast.makeText(CreateEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                try {
                    int eventYear = Integer.parseInt(date);
                    if (eventYear < 1000 || eventYear > currentYear) { 
                        Toast.makeText(CreateEventActivity.this, "Please enter a valid 4-digit year (<= " + currentYear + ")", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(CreateEventActivity.this, "Please enter a valid numeric year", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double eventPrice = Double.parseDouble(price);
                    if (eventPrice < 0) {
                        Toast.makeText(CreateEventActivity.this, "Price must be a positive number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(CreateEventActivity.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO
                // Test data entry
                // Find way to input capacity, maybe on facility?
                // End date?
                HashMap<String, Object> eventDetails = new HashMap<>();
                eventDetails.put("title", title);
                eventDetails.put("startDate", date);
                eventDetails.put("price", price);
                eventDetails.put("description", desc);
                eventDetails.put("capacity", 1); // edit later
                eventDetails.put("eventID", eventID);
                eventDetails.put("cancelledEntrants", new ArrayList<>());
                eventDetails.put("selectedEntrants", new ArrayList<>());
                eventDetails.put("posterRef", "/media/eventMedia");
                eventDetails.put("qrRef", ""); // update later


                documentReference.set(eventDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateEventActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                        eventCreated = true;

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateEventActivity.this, "Unable to create event. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventID = documentReference.getId();
                Bitmap qrCode = generateQRCodeBitmap(eventID); // QR Code references eventID
                //TODO
                // Ensure that qrRef can actually store QR Code
                documentReference.update("qrRef", qrCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateEventActivity.this, "QR Code successfully generated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateEventActivity.this, "Sorry, something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void pickImage(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Uri imageUri = result.getData().getData();
                            imgEvent.setImageURI(imageUri);
                        } catch (Exception e) {
                            Toast.makeText(CreateEventActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    // Code referenced from https://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application by Stefano, Downloaded 2024-10-26
    public Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}

