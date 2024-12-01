package com.example.cmput301f24mikasa;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;

/**
 * EditEventActivity allows for editing an existing event.
 * It allows updating event details (title, date, price, description, waiting list, geo-location)
 * and uploading a new image. Data is fetched from and updated to Firebase Firestore and Storage.
 */
public class EditEventActivity extends AppCompatActivity {
    private ImageView imgEvent;
    private Button btnUpload, btnUpdateEvent;
    private EditText editTextTitle, editTextDate, editTextPrice, editTextDesc, editTextLimitWaitingList;
    private CheckBox checkBoxLimitWaitingList, checkBoxGeoLocation;
    private String eventID;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Uri imageUri;
    private boolean imageChanged = false;
    private TextView txtStepIndex, headerTextView;
    private Button btnGenerateQRCode, btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Handle "Back" button click
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditEventActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });

        initializeUIElements();

        txtStepIndex = findViewById(R.id.txtStepIndex);
        txtStepIndex.setVisibility(View.GONE);
        headerTextView.setText("EDIT EVENT");
        btnGenerateQRCode =findViewById(R.id.btnGenerateQRCode);
        disableButton(btnGenerateQRCode);

        initializeFirebase();

        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null) {
            Toast.makeText(this, "No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            fetchEventData(eventID);
        }

        setupDatePicker();
        registerImagePicker();
        setupTextWatchers();

        btnUpload.setOnClickListener(view -> pickImage());
        btnUpdateEvent.setOnClickListener(v -> {
            if (areFieldsValid()) {
                updateEvent();
            } else {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });

        disableButton(btnUpdateEvent); // Disable the update button initially
    }

    private void initializeUIElements() {
        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpdateEvent = findViewById(R.id.btnCreateEvent);
        btnUpdateEvent.setText("Update Event");
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);
        checkBoxLimitWaitingList = findViewById(R.id.checkBoxLimitWaitingList);
        editTextLimitWaitingList = findViewById(R.id.editTextLimitWaitingList);
        checkBoxGeoLocation = findViewById(R.id.checkBoxGeoLocation);
        headerTextView = findViewById(R.id.headerTextView);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("event_images");
    }

    private void fetchEventData(String eventID) {
        DocumentReference docRef = db.collection("event").document(eventID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Pass the actual DocumentSnapshot, not DocumentReference
                        populateFieldsWithEventData(documentSnapshot);
                        checkAllConditions(); // Recheck conditions after data is loaded
                    } else {
                        Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFieldsWithEventData(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        // Safely retrieve and set the title
        String title = documentSnapshot.getString("title");
        editTextTitle.setText(title != null ? title : "");

        // Safely retrieve and set the start date
        String startDate = documentSnapshot.getString("startDate");
        editTextDate.setText(startDate != null ? startDate : "");

        // Safely retrieve and set the price
        String price = documentSnapshot.getString("price");
        editTextPrice.setText(price != null ? price : "");

        // Safely retrieve and set the description
        String description = documentSnapshot.getString("description");
        editTextDesc.setText(description != null ? description : "");

        // Safely retrieve and set the waiting list limit
        Boolean hasWaitingListLimit = documentSnapshot.getBoolean("hasWaitingListLimit");
        if (hasWaitingListLimit != null && hasWaitingListLimit) {
            checkBoxLimitWaitingList.setChecked(true);
            editTextLimitWaitingList.setVisibility(View.VISIBLE);

            Long waitingListLimit = documentSnapshot.getLong("waitingListLimit");
            if (waitingListLimit != null) {
                editTextLimitWaitingList.setText(String.valueOf(waitingListLimit));
            } else {
                editTextLimitWaitingList.setText(""); // Default empty if not provided
            }
        } else {
            checkBoxLimitWaitingList.setChecked(false);
            editTextLimitWaitingList.setVisibility(View.GONE);
        }

        // Safely retrieve and set geo-location required
        String geoLocationRequired = documentSnapshot.getString("geo-location required");
        checkBoxGeoLocation.setChecked("yes".equalsIgnoreCase(geoLocationRequired));

        // Safely retrieve and set the image
        String imageURL = documentSnapshot.getString("imageURL");
        if (imageURL != null && !imageURL.isEmpty()) {
            Glide.with(this).load(imageURL).into(imgEvent);
        }
    }

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
        if (areFieldsValid()) {
            enableButton(btnUpdateEvent);
        } else {
            disableButton(btnUpdateEvent);
        }
    }

    private boolean areFieldsValid() {
        return !editTextTitle.getText().toString().trim().isEmpty()
                && !editTextDate.getText().toString().trim().isEmpty()
                && !editTextPrice.getText().toString().trim().isEmpty()
                && !editTextDesc.getText().toString().trim().isEmpty();
    }

    private void updateEvent() {
        if (eventID == null) {
            Toast.makeText(this, "Event ID is missing. Cannot update.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTextTitle.getText().toString();
        String date = editTextDate.getText().toString();
        String price = editTextPrice.getText().toString();
        String desc = editTextDesc.getText().toString();
        Boolean hasWaitingListLimit = checkBoxLimitWaitingList.isChecked();
        String waitingListLimitText = editTextLimitWaitingList.getText().toString();
        int waitingListLimit = hasWaitingListLimit && !waitingListLimitText.isEmpty() ? Integer.parseInt(waitingListLimitText) : 10000;
        String geoLocationRequired = checkBoxGeoLocation.isChecked() ? "yes" : "no";

        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", title);
        updatedData.put("startDate", date);
        updatedData.put("price", price);
        updatedData.put("description", desc);
        updatedData.put("hasWaitingListLimit", hasWaitingListLimit);
        updatedData.put("waitingListLimit", waitingListLimit);
        updatedData.put("geo-location required", geoLocationRequired);

        if (imageChanged && imageUri != null) {
            uploadImageAndUpdateEvent(updatedData);
        } else {
            saveEventData(updatedData);
        }
    }

    private void uploadImageAndUpdateEvent(HashMap<String, Object> updatedData) {
        StorageReference fileRef = storageReference.child(eventID + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            updatedData.put("imageURL", uri.toString());
                            saveEventData(updatedData);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to get image URL.", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show());
    }

    private void saveEventData(HashMap<String, Object> updatedData) {
        db.collection("event").document(eventID).update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating event. Please try again.", Toast.LENGTH_SHORT).show());
    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    // Referenced from https://youtu.be/nOtlFl1aUCw?si=bUEVHRjnQpoJzAe7 by CodingZest, 2024-11-29
    private void registerImagePicker() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgEvent.setImageURI(imageUri);
                        imageChanged = true;
                        checkAllConditions();
                    }
                }
        );
    }

    // Referenced https://www.geeksforgeeks.org/datepickerdialog-in-android/ by gupta_shrinath, 2024-11-30
    private void setupDatePicker() {
        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                String formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                editTextDate.setText(formattedDate);
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void disableButton(Button button) {
        button.setEnabled(false);
        button.setBackgroundColor(Color.GRAY);
    }

    private void enableButton(Button button) {
        button.setEnabled(true);
        button.setBackgroundColor(Color.parseColor("#0D47A1"));
    }
}