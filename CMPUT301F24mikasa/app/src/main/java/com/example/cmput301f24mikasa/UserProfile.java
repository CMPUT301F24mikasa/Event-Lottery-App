package com.example.cmput301f24mikasa;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a user's profile containing their personal details and the events they have joined.
 * This class is designed to be serialized and stored/retrieved from Firebase Firestore.
 * It includes fields for the user's name, profile picture, device ID, email, phone number,
 * and a list of event IDs that the user has joined.
 */
public class UserProfile implements Serializable {

    private String name;
    private String profilePicture;
    private String deviceId;
    private String gmailAddress;
    private String phoneNumber;
    private String location; 

    // Firestore instance for retrieving profile pictures
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Default constructor required by Firestore for deserialization.
     */
    public UserProfile() {
    }


    /**
     * Constructor with profile picture provided.
     * Initializes a user profile with the specified details.
     *
     * @param name The name of the user.
     * @param profilePicture The URL or path to the user's profile picture.
     * @param deviceId The unique device ID for the user.
     * @param gmailAddress The Gmail address associated with the user.
     * @param phoneNumber The phone number of the user.
     */
    public UserProfile(String name, String profilePicture, String deviceId, String gmailAddress, String phoneNumber) {
        this.name = name;
        this.deviceId = deviceId;
        this.gmailAddress = gmailAddress;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.location = null;
    }


    /**
     * Gets the user's location.
     *
     * @return The user's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the user's location.
     *
     * @param location The user's location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the user's name.
     *
     * @return The name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name The name of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's profile picture.
     *
     * @return The profile picture URL or path of the user.
     */
    public String getProfilePicture() {
        return profilePicture;
    }

    /**
     * Sets the user's profile picture.
     *
     * @param profilePicture The URL or path to the user's profile picture.
     */
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * Gets the user's device ID.
     *
     * @return The unique device ID of the user.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the user's device ID.
     *
     * @param deviceId The unique device ID of the user.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the user's Gmail address.
     *
     * @return The Gmail address of the user.
     */
    public String getGmailAddress() {
        return gmailAddress;
    }

    /**
     * Sets the user's Gmail address.
     *
     * @param gmailAddress The Gmail address of the user.
     */
    public void setGmailAddress(String gmailAddress) {
        this.gmailAddress = gmailAddress;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The phone number of the user.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber The phone number of the user.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
