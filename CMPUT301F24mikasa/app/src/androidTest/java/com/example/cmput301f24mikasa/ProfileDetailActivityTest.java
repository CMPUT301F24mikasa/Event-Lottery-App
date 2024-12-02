package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

@RunWith(AndroidJUnit4.class)
public class ProfileDetailActivityTest {

    private static final String TEST_DEVICE_ID = "testDeviceId";

    @Rule
    public ActivityScenarioRule<ProfileDetailActivity> activityRule = new ActivityScenarioRule<>(
            new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ProfileDetailActivity.class)
                    .putExtra("deviceId", TEST_DEVICE_ID)
    );

    private CountingIdlingResource idlingResource = new CountingIdlingResource("DATA_LOADER");

    @Before
    public void setUp() {
        // Register the idling resource before starting the async operation
        IdlingRegistry.getInstance().register(idlingResource);

        // Increment to indicate the async task is running
        idlingResource.increment();

        // Add fake user to Firestore
        Task<Void> task = FirestoreHelper.addFakeUser(TEST_DEVICE_ID);

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                // Set the UserProfile once Firestore task is successful
                UserProfile testUserProfile = new UserProfile("Test User", "profilePictureUrl", TEST_DEVICE_ID, "test@example.com", "1234567890");
                UserProfileManager.getInstance().setUserProfile(testUserProfile);

                // Decrement idling resource when the async operation is complete
                idlingResource.decrement();
            }
        });
    }

    @Test
    public void testProfileDetailsDisplayedCorrectly() {
        // Verify that the profile details are displayed correctly
        onView(withId(R.id.user_name_detail)).check(matches(withText("Test User")));
        onView(withId(R.id.user_email_detail)).check(matches(withText("test@example.com")));
        onView(withId(R.id.user_phone_detail)).check(matches(withText("1234567890")));

        onView(withId(R.id.user_name_detail)).check(matches(not(withText(""))));
        onView(withId(R.id.user_email_detail)).check(matches(not(withText(""))));
        onView(withId(R.id.user_phone_detail)).check(matches(not(withText(""))));
    }

    @Test
    public void testDeleteProfileImage() {
        // Perform click on delete button
        onView(withId(R.id.remove_image_button)).perform(click());

        // Delay the check for the "placeholder" tag by 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            onView(withId(R.id.profile_image_detail))
                    .check(matches(withTagValue(is((Object) "placeholder"))));
        }, 2000);
    }

    @After
    public void tearDown() {
        // Delay the deletion of test data by 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseFirestore.getInstance().collection("users").document(TEST_DEVICE_ID).delete();
        }, 2000);

        // Unregister idling resource without deleting test data
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    public static Matcher<View> withTagValue(final Matcher<Object> tagMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (view.getTag() == null) {
                    return false; // No tag set on the view
                }
                return tagMatcher.matches(view.getTag());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("View with tag: ");
                tagMatcher.describeTo(description);
            }
        };
    }
}
