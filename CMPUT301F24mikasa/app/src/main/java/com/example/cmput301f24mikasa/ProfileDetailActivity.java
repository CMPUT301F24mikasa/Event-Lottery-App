package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

/**
 * ProfileDetailActivity displays detailed information about the user profile,
 * including the user's name, email, phone number, and profile image.
 * This activity retrieves the user's profile from the UserProfileManager
 * and populates the relevant views in the UI with the user's details.
 */
public class ProfileDetailActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;

    /**
     * Called when the activity is first created.
     * Initializes the views and populates them with data from the UserProfile.
     * The user profile is retrieved from the UserProfileManager.
     *
     * @param savedInstanceState a bundle containing the activity's previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileImageView = findViewById(R.id.profile_image_detail);
        nameTextView = findViewById(R.id.user_name_detail);
        emailTextView = findViewById(R.id.user_email_detail);
        phoneTextView = findViewById(R.id.user_phone_detail);

        // Retrieve the user profile from UserProfileManager
        UserProfile userProfile = UserProfileManager.getInstance().getUserProfile();

        if (userProfile != null) {
            nameTextView.setText(userProfile.getName());
            emailTextView.setText(userProfile.getGmailAddress());
            phoneTextView.setText(userProfile.getPhoneNumber());

            // Load the profile image if available
            if (userProfile.getProfilePicture() != null) {
                Picasso.get().load(userProfile.getProfilePicture()).into(profileImageView);
            }
        }
    }
}