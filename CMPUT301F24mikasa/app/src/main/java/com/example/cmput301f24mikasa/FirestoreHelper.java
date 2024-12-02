package com.example.cmput301f24mikasa;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for interacting with Firestore to manage user profile operations.
 * Provides methods for adding and removing fake user profiles.
 */
public class FirestoreHelper {

    /**
     * Adds a fake user profile to the Firestore database. The profile includes default 
     * information such as name, profile picture URL, device ID, Gmail address, and phone number.
     *
     * @param deviceId The unique device ID to associate with the user profile.
     * @return A {@link Task} that resolves when the Firestore operation is complete.
     *         If the operation is successful, the task will complete without errors.
     */
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
                });
    }

    
    /**
     * Removes a fake user profile from the Firestore database by deleting the document
     * associated with the given device ID.
     *
     * @param deviceId The unique device ID of the user profile to be removed.
     * @return A {@link Task} that resolves when the Firestore operation is complete.
     *         If the operation is successful, the task will complete without errors.
     */
    public static Task<Void> removeFakeUser(String deviceId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        return firestore.collection("users").document(deviceId).delete();
    }
}
