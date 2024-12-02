package com.example.cmput301f24mikasa;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * CreateEventActivity handles event creation by collecting details like title, date, price, and an image.
 * Integrates with Firebase Firestore and Storage for saving event data and supports QR code
 * generation and upload.
 */
public class CreateEventActivity extends AppCompatActivity {
    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent, btnCreatePoster;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc, editTextLimitWaitingList;
    Boolean eventCreated = false;
    Boolean qrCodeGenerated = false;
    Uri imageUri;
    FirebaseFirestore db;
    StorageReference storageReference;
    ActivityResultLauncher<Intent> resultLauncher;
    CollectionReference eventRef;
    String eventID;
    CheckBox checkBoxLimitWaitingList, checkBoxGeoLocation;
    Boolean hasWaitingListLimit;
    Integer waitingListLimit;
    TextView txtStepIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize UI elements
        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreatePoster = findViewById(R.id.btnCreatePoster);
        txtStepIndex = findViewById(R.id.txtStepIndex);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);
        checkBoxLimitWaitingList = findViewById(R.id.checkBoxLimitWaitingList);
        editTextLimitWaitingList = findViewById(R.id.editTextLimitWaitingList);
        checkBoxGeoLocation = findViewById(R.id.checkBoxGeoLocation);

        // Handle "Back" button click
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(CreateEventActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        // Disable non-applicable buttons initially
        disableButton(btnCreateEvent);
        disableButton(btnGenerateQRCode);
        disableButton(btnCreatePoster);

        // Setup Firebase
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        // Disable keyboard input for date and setup DatePickerDialog
        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Checkbox listener for waiting list limit
        checkBoxLimitWaitingList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextLimitWaitingList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextLimitWaitingList.setText("");
        });

        // Register image picker result
        registerResult();

        // Setup text watchers for field validation
        setupTextWatchers();

        // Set button click listeners
        btnUpload.setOnClickListener(view -> pickImage());

        // Click listener for create event button
        btnCreateEvent.setOnClickListener(v -> {
            if (eventCreated) {
                Toast.makeText(CreateEventActivity.this, "Event has already been created.", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = editTextTitle.getText().toString();
            String date = editTextDate.getText().toString();
            String price = editTextPrice.getText().toString();
            String desc = editTextDesc.getText().toString();

            disableButton(btnCreateEvent);

            createEvent(title, date, price, desc);
        });

        // Click listener for generate QR code button
        btnGenerateQRCode.setOnClickListener(v -> {
            if (!eventCreated) {
                Toast.makeText(CreateEventActivity.this, "Please create the event first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (eventID == null || eventID.isEmpty()) {
                Toast.makeText(this, "Event ID is missing. Unable to generate QR Code.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap qrCodeBitmap = generateQRCodeBitmap(eventID);
            if (qrCodeBitmap == null) {
                Toast.makeText(this, "Failed to generate QR Code. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadQRCodeToStorage(qrCodeBitmap, eventID);
        });

        // Click listener for Create poster button
        btnCreatePoster.setOnClickListener(v -> {
            if (!qrCodeGenerated) {
                Toast.makeText(CreateEventActivity.this, "Please generate the QR Code first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap qrCodeBitmap = generateQRCodeBitmap(eventID);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] qrCodeBytes = stream.toByteArray();

            Intent intent = new Intent(CreateEventActivity.this, EventPosterActivity.class);
            intent.putExtra("eventID", eventID);
            intent.putExtra("title", editTextTitle.getText().toString());
            intent.putExtra("startDate", editTextDate.getText().toString());
            intent.putExtra("desc", editTextDesc.getText().toString());
            intent.putExtra("price", editTextPrice.getText().toString());
            intent.putExtra("imageURL", imageUri.toString());
            intent.putExtra("qrCodeBytes", qrCodeBytes);

            startActivity(intent);
        });
    }

    private void disableButton(Button button) {
        button.setEnabled(false);
        button.setBackgroundColor(Color.parseColor("#808080"));
    }

    private void enableButton(Button button) {
        button.setEnabled(true);
        button.setBackgroundColor(Color.parseColor("#0D47A1"));
    }

    // Referenced https://www.geeksforgeeks.org/datepickerdialog-in-android/ by gupta_shrinath, 2024-11-30
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
            String formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
            editTextDate.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgEvent.setImageURI(imageUri);
                        checkAllConditions(); // Re-check conditions after image upload
                    }
                }
        );
    }

    // Referenced https://www.geeksforgeeks.org/how-to-implement-textwatcher-in-android/ by adityamshidlyali, 2024-11-30
    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllConditions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextTitle.addTextChangedListener(textWatcher);
        editTextDate.addTextChangedListener(textWatcher);
        editTextPrice.addTextChangedListener(textWatcher);
        editTextDesc.addTextChangedListener(textWatcher);
    }

    private void checkAllConditions() {
        String title = editTextTitle.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();

        if (!title.isEmpty() && !date.isEmpty() && !price.isEmpty() && !desc.isEmpty() && imageUri != null) {
            enableButton(btnCreateEvent);
            txtStepIndex.setText("Step 2 of 4: Create Event"); // Update step
        } else {
            disableButton(btnCreateEvent);
            txtStepIndex.setText("Step 1 of 4: Upload Image & Fill in Fields"); // Reset to step 1
        }
    }

    private void createEvent(String title, String date, String price, String desc) {
        DocumentReference documentReference = eventRef.document();
        eventID = documentReference.getId();

        StorageReference fileRef = storageReference.child(eventID + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            waitingListLimit = 10000;
            hasWaitingListLimit = checkBoxLimitWaitingList.isChecked();
            if (hasWaitingListLimit) {
                String limitText = editTextLimitWaitingList.getText().toString();
                if (!limitText.isEmpty()) {
                    waitingListLimit = Integer.parseInt(limitText);
                }
            }

            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                HashMap<String, Object> eventDetails = new HashMap<>();
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                eventDetails.put("deviceID", deviceId);
                eventDetails.put("title", title);
                eventDetails.put("startDate", date);
                eventDetails.put("price", price);
                eventDetails.put("description", desc);
                eventDetails.put("capacity", 1);
                eventDetails.put("eventID", eventID);
                eventDetails.put("imageURL", uri.toString());
                eventDetails.put("hasWaitingListLimit", hasWaitingListLimit);
                eventDetails.put("waitingListLimit", waitingListLimit);
                eventDetails.put("alreadySampled", "0");
                eventDetails.put("chosenAmount", 0);
                eventDetails.put("finalListCreated", "0");
                eventDetails.put("waitingList", new ArrayList<>());
                eventDetails.put("selectedEntrants", new ArrayList<>());
                eventDetails.put("cancelledEntrants", new ArrayList<>());

                if (checkBoxGeoLocation.isChecked()) {
                    eventDetails.put("geo-location required", "yes");
                } else {
                    eventDetails.put("geo-location required", "no");
                }

                documentReference.set(eventDetails).addOnSuccessListener(aVoid -> {
                    eventCreated = true;
                    txtStepIndex.setText("Step 3 of 4: Generate QR Code");
                    enableButton(btnGenerateQRCode);
                    Toast.makeText(CreateEventActivity.this, "Event created successfully.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Unable to create event. Please try again.", Toast.LENGTH_SHORT).show();
                    enableButton(btnCreateEvent);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(CreateEventActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                enableButton(btnCreateEvent);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(CreateEventActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            enableButton(btnCreateEvent);
        });
    }

    // Referenced https://stackoverflow.com/questions/40885860/how-to-save-bitmap-to-firebase by Haythem BEN AICHA, 2024-11-30
    // Referenced https://stackoverflow.com/questions/56699632/how-to-upload-file-bitmap-to-cloud-firestore by Deˣ, 2024-11-30
    private void uploadQRCodeToStorage(Bitmap qrCodeBitmap, String eventID) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] qrCodeBytes = stream.toByteArray();

        StorageReference qrCodeRef = storageReference.child("qr_codes/" + eventID + "_qr_code.png");
        qrCodeRef.putBytes(qrCodeBytes).addOnSuccessListener(taskSnapshot -> {
            qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                eventRef.document(eventID).update("qrCodeURL", uri.toString())
                        .addOnSuccessListener(aVoid -> {
                            qrCodeGenerated = true;
                            txtStepIndex.setText("Step 4 of 4: Create Poster");
                            enableButton(btnCreatePoster);
                            Toast.makeText(CreateEventActivity.this, "QR Code uploaded and URL saved successfully.", Toast.LENGTH_SHORT).show();

                            saveQRCodeHash(eventID);

                        })
                        .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Failed to save QR Code URL.", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Failed to get QR Code URL.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Failed to upload QR Code.", Toast.LENGTH_SHORT).show());
    }


    private void saveQRCodeHash(String eventID) {
        HashMap<String, Object> qrCodeData = new HashMap<>();
        qrCodeData.put("qrCodeHash", eventID);

        eventRef.document(eventID)
                .update(qrCodeData)
                .addOnSuccessListener(aVoid -> Toast.makeText(CreateEventActivity.this, "QR Code hash saved successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Failed to save QR Code hash. Please try again.", Toast.LENGTH_SHORT).show());
    }


    // Referenced https://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application by Stefano, 2024-10-31
    /**
     * Generates a QR code
     *
     * @param content The content to be encoded in the QR code.
     * @return A Bitmap containing the generated QR code, or null if there was an error during generation.
     */
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
