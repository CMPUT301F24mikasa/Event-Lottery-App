package com.example.cmput301f24mikasa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * QRScannerActivity is responsible for scanning QR codes using the device's camera.
 * It processes the scanned QR code and retrieves event details from Firebase Firestore,
 * then navigates to the event details screen.
 */
public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private PreviewView previewView;
    private final BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
    private String scannedQRContent; // Variable to hold QR code content
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private long lastScanTime = 0; // Timestamp for the last scan
    private boolean hasScanned = false; // Boolean to indicate if a scan has been processed

    /**
     * Default constructor for QRScannerActivity.
     */
    public QRScannerActivity() {
    }


    /**
     * Called when the activity is created. Initializes the UI components and sets click listeners.
     *
     * @param savedInstanceState a bundle containing the activity's previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        previewView = findViewById(R.id.previewView);

        Button startScanningButton = findViewById(R.id.btnScanQR);
        Button backButton = findViewById(R.id.back_button);

        startScanningButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();

                // Disable the button and grey it out
                startScanningButton.setEnabled(false);
                startScanningButton.setAlpha(0.5f); // Make it look greyed out
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 1001);
            }
        });

        // Back button click listener
        backButton.setOnClickListener(view -> startActivity(new Intent(QRScannerActivity.this, EventsActivity.class)));
    }

    /**
     * Initializes the camera preview and image analysis to scan QR codes.
     */
    void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeImage);

                // Bind camera to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Analyzes each frame captured by the camera for QR codes.
     * Processes the frame and initiates barcode scanning if a QR code is detected.
     *
     * @param imageProxy the image proxy containing the frame data from the camera
     */
    private void analyzeImage(ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class)
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null && System.currentTimeMillis() - lastScanTime >= 1000 && !hasScanned) { // Check if 1 second has passed and scan hasn't been processed
            lastScanTime = System.currentTimeMillis(); // Update the last scan time
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(this::processBarcodeResults)
                    .addOnFailureListener(e -> e.printStackTrace())
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close(); // Release the image if not processing
        }
    }

    /**
     * Processes the scanned barcode results.
     * If a QR code is found, stores the content and proceeds to fetch event details.
     *
     * @param barcodes the list of barcodes detected in the image
     */
    void processBarcodeResults(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            String qrText = barcode.getRawValue();
            if (qrText != null && !hasScanned) {
                scannedQRContent = qrText; // Store the scanned QR code content in a variable
                hasScanned = true;
                Toast.makeText(this, "QR Code Scanned!", Toast.LENGTH_SHORT).show();
                fetchEventDetailsAndNavigate(null);
                break; // Stop processing after finding a QR code
            }
        }
    }

    /**
     * Fetches the event details from Firebase Firestore based on the scanned QR code content.
     * If the event is found and the 'qrCodeHash' field is not null, it navigates to the event details screen.
     * If the 'qrCodeHash' field is null, it shows an alert to the user.
     */
    void fetchEventDetailsAndNavigate(String eventId) {
        // Use the provided eventId, or fall back to the scanned QR content
        String idToUse = (eventId != null) ? eventId : scannedQRContent;

        db.collection("event").document(idToUse).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Event details retrieved successfully
                        String eventIdFromFirestore = documentSnapshot.getId();
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String date = documentSnapshot.getString("date");
                        String price = documentSnapshot.getString("price");
                        String qrCodeHash = documentSnapshot.getString("qrCodeHash");

                        if (qrCodeHash == null) {
                            // qrCodeHash is null, show alert
                            new android.app.AlertDialog.Builder(QRScannerActivity.this)
                                    .setTitle("Event Error")
                                    .setMessage("This event cannot be signed up for because the QR Data has been removed.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            // qrCodeHash is not null, proceed to event details screen
                            Intent intent = new Intent(this, ViewEventActivity.class);
                            intent.putExtra("eventId", eventIdFromFirestore);
                            intent.putExtra("title", title);
                            intent.putExtra("description", description);
                            intent.putExtra("date", date);
                            intent.putExtra("price", price);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Handles the result of the permission request for camera access.
     * If the permission is granted, it starts the camera; otherwise, a toast message is shown.
     *
     * @param requestCode the request code passed in requestPermissions
     * @param permissions the requested permissions
     * @param grantResults the grant results for the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a toast message.
     *
     * @param message the message to be displayed
     */
    // In QRScannerActivity.java
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
