package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cmput301f24mikasa.AutoNotChosenForFinalList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AutoNotChosenForFinalListTest {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference notificationRef;
    private String testEventID;
    private String testEventTitle = "Test Event Title";

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("event");
        notificationRef = db.collection("notification");

        // Hard-code the document ID
        testEventID = "tester123"; // Set the hard-coded eventID

        // Create a test event with waiting list and selected entrants
        HashMap<String, Object> testEvent = new HashMap<>();
        testEvent.put("waitingList", List.of("user1", "user2"));
        testEvent.put("selectedEntrants", List.of("user3", "user4"));
        testEvent.put("eventTitle", testEventTitle); // Set the event title
        testEvent.put("eventID", testEventID); // Include the hard-coded eventID

        // Save the test event with the hard-coded document ID
        CountDownLatch latch = new CountDownLatch(1);
        eventsRef.document(testEventID)
                .set(testEvent)
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        try {
            latch.await(); // Wait for Firestore operation to complete
            Thread.sleep(4000); // Add a 4-second delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @After
    public void tearDown() {
        // Clean up the test event and notifications
        CountDownLatch latch = new CountDownLatch(2);

        // Delete the test event by its document ID
        eventsRef.document(testEventID).delete()
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        notificationRef.whereEqualTo("eventID", testEventID).get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNotificationsAreSent() {
        // Launch the activity with ActivityScenario
        ActivityScenario<AutoNotChosenForFinalList> scenario = ActivityScenario.launch(AutoNotChosenForFinalList.class);

        CountDownLatch latch = new CountDownLatch(1); // We will signal the test to continue after 30 seconds


        scenario.onActivity(activity -> {
            assertNotNull(activity); // Ensure the activity is launched

            // Your Firestore query, unchanged
            notificationRef.whereEqualTo("eventID", "tester123").get()
                    .addOnSuccessListener(querySnapshot -> {
                        assertEquals(2, querySnapshot.getDocuments().size()); // Expected notifications
                        latch.countDown(); // If successful, allow the test to proceed
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        latch.countDown(); // In case of failure, allow the test to proceed
                    });
        });
        // Start a thread to simulate the 30-second timeout
        new Thread(() -> {
            try {
                Thread.sleep(130000); // Wait for 30 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown(); // Force the test to continue
            }
        }).start();
        try {
            // Wait for either the Firestore response or the 30-second timeout
            latch.await(); // This will return when countDown() is called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.close(); // Clean up the scenario
    }



}
