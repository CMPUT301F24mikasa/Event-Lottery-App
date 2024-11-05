package com.example.cmput301f24mikasa;

public class UserProfileManager {
    private static UserProfileManager instance;
    private UserProfile userProfile;

    private UserProfileManager() {
    }

    public static synchronized UserProfileManager getInstance() {
        if (instance == null) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
