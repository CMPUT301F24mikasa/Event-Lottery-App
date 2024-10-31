package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc;
    Boolean eventCreated;
    Uri imageUri;
    FirebaseFirestore db;
    StorageReference storageReference;
    ActivityResultLauncher<Intent> resultLauncher;
    CollectionReference eventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_create_event);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        eventCreated = false;
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
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        registerResult();
        btnUpload.setOnClickListener(view -> pickImage());

        btnCreateEvent.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String date = editTextDate.getText().toString();
            String price = editTextPrice.getText().toString();
            String desc = editTextDesc.getText().toString();

            if (title.isEmpty() || date.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                createEvent(title, date, price, desc);
            } else {
                Toast.makeText(CreateEventActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerateQRCode.setOnClickListener(v -> {
            DocumentReference documentReference = eventRef.document();
            String eventID = documentReference.getId();
            Bitmap qrCode = generateQRCodeBitmap(eventID);
            documentReference.update("qrRef", eventID).addOnSuccessListener(aVoid ->
                    Toast.makeText(CreateEventActivity.this, "QR Code successfully generated", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(CreateEventActivity.this, "Sorry, something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgEvent.setImageURI(imageUri);
                        } else {
                            Toast.makeText(CreateEventActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void createEvent(String title, String date, String price, String desc) {
        StorageReference fileRef = storageReference.child(title + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    HashMap<String, Object> eventDetails = new HashMap<>();
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    eventDetails.put("deviceID", deviceId);
                    eventDetails.put("title", title);
                    eventDetails.put("startDate", date);
                    eventDetails.put("price", price);
                    eventDetails.put("description", desc);
                    eventDetails.put("capacity", 1);
                    eventDetails.put("imageURL", uri.toString());
                    eventDetails.put("cancelledEntrants", new ArrayList<>());
                    eventDetails.put("selectedEntrants", new ArrayList<>());

                    eventRef.document(title).set(eventDetails).addOnSuccessListener(aVoid ->
                            Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(CreateEventActivity.this, "Sorry, unable to create event.", Toast.LENGTH_SHORT).show()
                    );
                }).addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this, "Sorry, image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(CreateEventActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        );
    }

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