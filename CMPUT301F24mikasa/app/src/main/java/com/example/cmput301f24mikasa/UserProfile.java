package com.example.cmput301f24mikasa;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;  // Optional
    private String profilePictureUrl;  // Can be null if no profile picture
    private boolean isOnWaitingList;
    private boolean notificationsEnabled;
    private String notificationPreference;  // "opt-in" or "opt-out"
    private String deviceIdentifier;  // To be identified by device
    private boolean hasGeolocationWarning;  // Warning for geolocation-based waitlist

    // Constructors
    //public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    //}

    public UserProfile(String userId, String name, String email, String phoneNumber, String profilePictureUrl, boolean notificationsEnabled) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureUrl = profilePictureUrl;
        this.notificationsEnabled = notificationsEnabled;
        this.isOnWaitingList = false; // Default: not on waiting list
        this.notificationPreference = "opt-in";  // Default
        this.deviceIdentifier = null;  // Default: not set
        this.hasGeolocationWarning = false;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public boolean isOnWaitingList() {
        return isOnWaitingList;
    }

    public void setOnWaitingList(boolean isOnWaitingList) {
        this.isOnWaitingList = isOnWaitingList;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(String notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public boolean hasGeolocationWarning() {
        return hasGeolocationWarning;
    }

    public void setHasGeolocationWarning(boolean hasGeolocationWarning) {
        this.hasGeolocationWarning = hasGeolocationWarning;
    }

    // Method to remove profile picture
    public void removeProfilePicture() {
        this.profilePictureUrl = null;  // Removes the profile picture
    }
}
