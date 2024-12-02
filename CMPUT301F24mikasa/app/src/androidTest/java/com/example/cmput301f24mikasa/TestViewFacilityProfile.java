package com.example.cmput301f24mikasa;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class TestViewFacilityProfile {

    @Rule
    public ActivityScenarioRule<ViewFacilityActivity> mActivityScenarioRule;

    public TestViewFacilityProfile() throws InterruptedException {
        Intent intent = loadFacilityData();
        mActivityScenarioRule = new ActivityScenarioRule<>(intent);
    }

    // Referenced https://www.baeldung.com/java-countdown-latch by baeldung, 2024-12-01
    private Intent loadFacilityData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Intent[] intent = {null};

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facility").whereEqualTo("facilityName", "Edmonton Community Rec Center").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String facilityName = document.getString("facilityName");
                        String facilityLocation = document.getString("facilityLocation");
                        String facilityDesc = document.getString("facilityDesc");

                        intent[0] = new Intent(ApplicationProvider.getApplicationContext(), ViewFacilityActivity.class);
                        intent[0].putExtra("FACILITY_NAME", facilityName);
                        intent[0].putExtra("FACILITY_LOCATION", facilityLocation);
                        intent[0].putExtra("FACILITY_DESC", facilityDesc);
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        latch.await();
        return intent[0];
    }

    @Test
    public void testLoadFacilityDetails() {
        onView(withId(R.id.tv_facility_name)).check(matches(isDisplayed()));

        onView(withId(R.id.tv_facility_name)).check(matches(withText("Edmonton Community Rec Center")));

        onView(withId(R.id.tv_facility_location)).check(matches(isDisplayed()));

        onView(withId(R.id.tv_facility_location)).check(matches(withText("Edmonton")));

        onView(withId(R.id.tv_facility_description)).check(matches(isDisplayed()));

        onView(withId(R.id.tv_facility_description)).check(matches(withText("This is for a test. Do not delete.")));
    }
}
