package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * The CreateEventActivity class is responsible for creating an event,
 * uploading a poster, generating a QR Code that links to the unique eventID,
 * then uploading the information to Firebase Firestore and Storage.
 *
 * CreateEventActivity allows the user to input event details, upload an event image,
 * generate a QR code, and optionally set a waiting list limit.
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
    CheckBox checkBoxLimitWaitingList;
    Boolean hasWaitingListLimit;
    Integer waitingListLimit;

    /**
     * Default constructor for CreateEventActivity.
     * Required for Android activity lifecycle and initialization.
     */
    public CreateEventActivity() {
        // Constructor is provided by default
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    /**
     * onCreate creates the CreateEventActivity.
     * It sets up the UI elements, event listeners, and connects to Firebase Firestore and Storage.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());


        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreatePoster = findViewById(R.id.btnCreatePoster);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);
        checkBoxLimitWaitingList = findViewById(R.id.checkBoxLimitWaitingList);
        editTextLimitWaitingList = findViewById(R.id.editTextLimitWaitingList);

        checkBoxLimitWaitingList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextLimitWaitingList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextLimitWaitingList.setText("");
        });

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        registerResult();

        btnUpload.setOnClickListener(view -> pickImage());

        btnCreateEvent.setOnClickListener(v -> {
            if (eventCreated) {
                Toast.makeText(CreateEventActivity.this, "Event has already been created.", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = editTextTitle.getText().toString();
            String date = editTextDate.getText().toString();
            String price = editTextPrice.getText().toString();
            String desc = editTextDesc.getText().toString();

            if (title.isEmpty() || date.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!DATE_PATTERN.matcher(date).matches()) {
                Toast.makeText(CreateEventActivity.this, "Please enter the date in MM/DD/YYYY format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                createEvent(title, date, price, desc);
            } else {
                Toast.makeText(CreateEventActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerateQRCode.setOnClickListener(v -> {
            if (eventCreated) {  // Ensure event is created before generating QR code
                qrCodeGenerated = true;
                Toast.makeText(CreateEventActivity.this, "QR Code successfully generated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateEventActivity.this, "Please create the event first.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreatePoster.setOnClickListener(v -> {
            if (eventCreated && qrCodeGenerated) {
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
            } else {
                Toast.makeText(CreateEventActivity.this, "Please ensure event is created and QR code is generated.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes an intent that allows the user to select an image from their camera roll.
     * After an image is chosen, its Firebase URL is stored for later reference.
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    /**
     * Registers result of pickImage intent.
     * registerResult allows CreateEventActivity to handle the image
     * provided by the user, such as displaying it in the relevant ImageView.
     */
    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgEvent.setImageURI(imageUri);
                    } else {
                        Toast.makeText(CreateEventActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Creates a new unique event by uploading details to Firebase Firestore and Storage.
     * @param title Title of the event.
     * @param date Start date of the event.
     * @param price Price of the event.
     * @param desc Description of the event.
     */
    private void createEvent(String title, String date, String price, String desc) {
        DocumentReference documentReference = eventRef.document();
        eventID = documentReference.getId();

        StorageReference fileRef = storageReference.child(eventID + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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
                    eventDetails.put("cancelledEntrants", new ArrayList<>());
                    eventDetails.put("selectedEntrants", new ArrayList<>());
                    eventDetails.put("hasWaitingListLimit", hasWaitingListLimit);
                    eventDetails.put("waitingListLimit", waitingListLimit);
                    eventDetails.put("waitingList", new ArrayList<>()); // Add empty WaitingList array


                    // Nikita's code:
                    eventDetails.put("finalEntrants", new ArrayList<>());
                    eventDetails.put("alreadySampled", "0");
                    eventDetails.put("finalListCreated", "0");
                    //--------


                    documentReference.set(eventDetails).addOnSuccessListener(aVoid -> {
                        eventCreated = true;
                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e ->
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

    // https://github.com/zxing/zxing, Downloaded 2024-11-01, by zxing
    /**
     * Generates a QR code bitmap from provided content using ZXing library.
     * The generated QR code is then displayed in the relevant EventPosterActivity
     * @param content Content encoded as a string for easy handling
     * @return QR Code Bitmap of the generated QR code on successful generation
     * Otherwise it returns null if generation fails.
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