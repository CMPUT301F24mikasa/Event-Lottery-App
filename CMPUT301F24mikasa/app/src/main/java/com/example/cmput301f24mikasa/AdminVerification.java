package com.example.cmput301f24mikasa;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminVerification {

    /**
     * Checks if the user is an admin by verifying their device ID in the Firestore database.
     *
     * @param context     The Activity context to update UI elements.
     * @param db          Firestore instance.
     * @param deviceId    The device ID of the user.
     * @param buttonAdmin The ImageButton to show or hide based on admin status.
     */
    public static void checkIfAdmin(Context context, ImageButton buttonAdmin) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        buttonAdmin.setVisibility(ImageButton.GONE);


        db.collection("admin").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Device ID matches an admin entry
                            Log.d("AdminVerification", "User is an admin: " + deviceId);
                            buttonAdmin.setVisibility(ImageButton.VISIBLE);
                        } else {
                            Log.d("AdminVerification", "User is not an admin: " + deviceId);
                            buttonAdmin.setVisibility(ImageButton.GONE);
                        }
                    } else {
                        Log.e("AdminVerification", "Error checking admin status", task.getException());
                        Toast.makeText(context, "Error checking admin status. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

