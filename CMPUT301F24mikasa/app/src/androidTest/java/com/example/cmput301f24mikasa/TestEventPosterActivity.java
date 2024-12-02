package com.example.cmput301f24mikasa;

import static androidx.test.espresso.Espresso.onView;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.fail;

import android.content.Intent;


import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestEventPosterActivity {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityScenarioRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    @Test
    public void testEventPosterActivityWithFirestoreData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = "TEST";

        db.collection("event").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String title = documentSnapshot.getString("title");
                String startDate = documentSnapshot.getString("startDate");
                String desc = documentSnapshot.getString("description");
                String price = documentSnapshot.getString("price");
                String imageURL = documentSnapshot.getString("imageURL");

                Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventPosterActivity.class);
                intent.putExtra("eventID", eventId);
                intent.putExtra("title", title);
                intent.putExtra("startDate", startDate);
                intent.putExtra("desc", desc);
                intent.putExtra("price", price);
                intent.putExtra("imageURL", imageURL);

                onView(withId(R.id.txtTitle)).check(matches(withText(title)));
                onView(withId(R.id.txtDate)).check(matches(withText(startDate)));
                onView(withId(R.id.txtPrice)).check(matches(withText("$" + price)));
                onView(withId(R.id.txtDesc)).check(matches(withText(desc)));
                onView(withId(R.id.imgEventImage)).check(matches(isDisplayed()));
                onView(withId(R.id.imgQRCode)).check(matches(isDisplayed()));
            } else {
                fail("Event not found in Firestore.");
            }
        }).addOnFailureListener(e -> fail("Failed to fetch event data from Firestore: " + e.getMessage()));
    }
}

