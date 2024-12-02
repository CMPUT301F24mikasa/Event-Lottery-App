package com.example.cmput301f24mikasa;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.ActivityTestRule;

import java.io.File;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class TestCreateEventActivity {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityScenarioRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);


    @Test
    public void testCreateEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        CollectionReference eventRef = db.collection("event");
        StorageReference storageRef = storage.getReference().child("test_images");

        String testTitle = "Test Event";
        String testDate = "12/12/2025";
        String testPrice = "15.00";
        String testDesc = "This is a test event. Please ignore";
        Uri testImageUri = Uri.parse("android.resource://com.example.cmput301f24mikasa/raw/test");

        onView(withId(R.id.editTextTitle)).perform(typeText(testTitle));
        onView(withId(R.id.editTextDate)).perform(replaceText(testDate));
        onView(withId(R.id.editTextPrice)).perform(typeText(testPrice));
        onView(withId(R.id.editTextDesc)).perform(typeText(testDesc));
        onView(withId(R.id.btnUpload)).perform(click());

        StorageReference testImageRef = storageRef.child("test_event_image.jpg");
        testImageRef.putFile(testImageUri).addOnSuccessListener(taskSnapshot -> {
            testImageRef.getDownloadUrl().addOnSuccessListener(imageUri -> {
                HashMap<String, Object> eventData = new HashMap<>();
                eventData.put("title", testTitle);
                eventData.put("startDate", testDate);
                eventData.put("price", testPrice);
                eventData.put("description", testDesc);
                eventData.put("imageURL", imageUri.toString());

                eventRef.add(eventData).addOnSuccessListener(documentReference -> {
                    String eventID = documentReference.getId();

                    eventRef.document(eventID).get().addOnSuccessListener(documentSnapshot -> {
                        assertTrue(documentSnapshot.exists());
                        assertEquals(testTitle, documentSnapshot.getString("title"));
                        assertEquals(testDate, documentSnapshot.getString("startDate"));
                        assertEquals(testPrice, documentSnapshot.getString("price"));
                        assertEquals(testDesc, documentSnapshot.getString("description"));
                        assertEquals(imageUri.toString(), documentSnapshot.getString("imageURL"));

                        eventRef.document(eventID).delete();
                        testImageRef.delete().addOnSuccessListener(aVoid -> {
                            System.out.println("Test image and event deleted successfully.");
                        }).addOnFailureListener(e -> {
                            throw new AssertionError("Failed to delete test image");
                        });
                    }).addOnFailureListener(e -> {
                        throw new AssertionError("Failed to verify event in Firestore");
                    });
                }).addOnFailureListener(e -> {
                    throw new AssertionError("Failed to add event to Firestore");
                });
            }).addOnFailureListener(e -> {
                throw new AssertionError("Failed to get uploaded image URL");
            });
        }).addOnFailureListener(e -> {
            throw new AssertionError("Failed to upload test image");
        });
    }
}
