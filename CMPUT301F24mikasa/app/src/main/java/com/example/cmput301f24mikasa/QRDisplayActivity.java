package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * QRDisplayActivity is responsible for displaying the QR code text that was scanned by the user.
 * It retrieves the QR code text passed via an Intent and displays it in a TextView.
 */
public class QRDisplayActivity extends AppCompatActivity {

    /**
     * Default constructor for QRDisplayActivity.
     */
    public QRDisplayActivity() {
    }

    /**
     * This method initializes the activity's UI and retrieves
     * the scanned QR code data from the Intent and displays it in a TextView.
     *
     * @param savedInstanceState a bundle containing the activity's previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        // Get the scanned data from the intent
        Intent intent = getIntent();
        String qrText = intent.getStringExtra("QR_TEXT");

        // Set the scanned data to the TextView
        TextView qrTextView = findViewById(R.id.qrTextView);
        qrTextView.setText(qrText);
    }
}

