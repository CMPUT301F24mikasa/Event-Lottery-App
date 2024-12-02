package com.example.cmput301f24mikasa;

import android.content.Context;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * AdminVerification handles the validation of admin privileges by checking the device ID
 * against stored records in Firebase Firestore. It updates the visibility of admin icon
 * based on the admin status, enabling secure access to admin-only features.
 */

public class AdminVerification {

    /**
     * Checks if the user is an admin by verifying their device ID in the Firestore database.
     *
     * @param context     The Activity context to update UI elements.
     * @param buttonAdmin The ImageButton to show or hide based on admin status.
     * @param deviceId    The device ID to verify admin status.
     * @param firestore   The Firestore instance to use for the check.
     */
    public static void checkIfAdmin(Context context, ImageButton buttonAdmin, String deviceId, FirebaseFirestore firestore) {
        buttonAdmin.setVisibility(ImageButton.GONE);

        firestore.collection("admin").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            buttonAdmin.setVisibility(ImageButton.VISIBLE);
                        } else {
                            buttonAdmin.setVisibility(ImageButton.GONE);
                        }
                    } else {
                        Toast.makeText(context, "Error checking admin status. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
