package com.example.cmput301f24mikasa;

import android.content.Intent;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class QRScannerActivityTest {

    private static final String TEST_EVENT_ID = "testEvent123"; // Event ID used for testing
    private CountingIdlingResource idlingResource = new CountingIdlingResource("DATA_LOADER");

    @Rule
    public ActivityScenarioRule<QRScannerActivity> activityRule = new ActivityScenarioRule<>(QRScannerActivity.class);

    @Before
    public void setUp() {
        // Register the idling resource before starting the async operation
        IdlingRegistry.getInstance().register(idlingResource);

        // Increment to indicate the async task is running
        idlingResource.increment();

        // Manually add the fake event to Firestore for the test
        addFakeEventToFirestore();
    }

    // Add a fake event to Firestore
    private void addFakeEventToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> event = new HashMap<>();
        event.put("title", "Test Event");
        event.put("description", "This is a test event for QR scanning");
        event.put("price", "10");
        event.put("qrCodeHash", "testEvent123");

        db.collection("event").document(TEST_EVENT_ID)  // Event ID for this test
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    // Event successfully written
                    idlingResource.decrement();
                })
                .addOnFailureListener(e -> {
                    // Handle failure (in this case, we're skipping failure handling in this test)
                    idlingResource.decrement();
                });
    }

    @Test
    public void testQRCodeScanAndEventNavigation() throws InterruptedException {
        // Launch the activity using the ActivityScenarioRule
        activityRule.getScenario().onActivity(activity -> {
            // Directly invoke the fetchEventDetailsAndNavigate method
            activity.fetchEventDetailsAndNavigate(TEST_EVENT_ID);
        });

        // Delay to ensure Firestore has time to write the event data
        Thread.sleep(2000); // First delay (2 seconds)

        // Now that event details are fetched, we check if the next screen has the correct event data
        onView(withId(R.id.txtEventTitle)).check(matches(withText("Test Event")));
        onView(withId(R.id.txtEventDescription)).check(matches(withText("This is a test event for QR scanning")));
        onView(withId(R.id.txtEventPrice)).check(matches(withText("10")));

        // Delay to ensure the UI updates after fetching event details
        Thread.sleep(2000); // Second delay (2 seconds)
    }

    @After
    public void tearDown() {
        // Unregister the idling resource after tests are done
        IdlingRegistry.getInstance().unregister(idlingResource);
    }
}
