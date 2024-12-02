package com.example.cmput301f24mikasa;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    public static Task<Void> addFakeUser(String deviceId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> userProfileData = new HashMap<>();
        userProfileData.put("name", "Test User");
        userProfileData.put("profilePicture", "profilePictureUrl");
        userProfileData.put("deviceId", deviceId);
        userProfileData.put("gmailAddress", "test@example.com");
        userProfileData.put("phoneNumber", "1234567890");

        return firestore.collection("users").document(deviceId)
                .set(userProfileData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreHelper", "User profile set up successfully.");
                    } else {
                        Log.e("FirestoreHelper", "Failed to set up user profile: ", task.getException());
                    }
                });
    }

    public static Task<Void> removeFakeUser(String deviceId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        return firestore.collection("users").document(deviceId).delete();
    }
}
