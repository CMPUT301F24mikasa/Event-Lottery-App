package com.example.cmput301f24mikasa;

/**
 * Singleton class for managing the UserProfile throughout the application.
 * It ensures that only one instance of the UserProfileManager exists and provides
 * methods to get and set the current UserProfile.
 */
public class UserProfileManager {
    private static UserProfileManager instance;
    private UserProfile userProfile;

    /**
     * Private constructor to prevent instantiation from other classes.
     * Ensures that the UserProfileManager can only be created through the getInstance() method.
     */
    private UserProfileManager() {
    }

    /**
     * Returns the singleton instance of the UserProfileManager.
     * If an instance does not already exist, it is created.
     *
     * @return The single instance of UserProfileManager.
     */
    public static synchronized UserProfileManager getInstance() {
        if (instance == null) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    /**
     * Gets the current UserProfile.
     *
     * @return The current UserProfile object.
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * Sets the current UserProfile.
     *
     * @param userProfile The UserProfile object to set as the current user profile.
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
