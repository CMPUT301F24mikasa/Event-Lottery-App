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

    /**
     * Default constructor for WaitingListActivity.
     * This constructor is required for the Android activity lifecycle.
     */
    private String name;
    private String profilePicture;
    private String deviceId;
    private String gmailAddress;
    private String phoneNumber;
    private ArrayList<String> eventsJoined; 
    private String location; 

    // Firestore instance for retrieving profile pictures
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * No-argument constructor required by Firestore for deserialization.
     * Initializes the eventsJoined field to an empty ArrayList.
     */
    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public UserProfile() {
        this.eventsJoined = new ArrayList<>();
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
    // Constructor with profile picture provided
    public UserProfile(String name, String profilePicture, String deviceId, String gmailAddress, String phoneNumber) {
        this.name = name;
        this.deviceId = deviceId;
        this.gmailAddress = gmailAddress;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.location = null;
        //this.eventsJoined = new ArrayList<>();
    }

    // Getters and Setters
    /**
     * Gets the user's name.
     *
     * @return The name of the user.
     */

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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

    /**
     * Gets the list of event IDs that the user has joined.
     *
     * @return The list of event IDs.
     */
    public ArrayList<String> getEventsJoined() {
        return eventsJoined;
    }

    /**
     * Sets the list of event IDs that the user has joined.
     *
     * @param eventsJoined The list of event IDs to set.
     */
    public void setEventsJoined(ArrayList<String> eventsJoined) {
        this.eventsJoined = eventsJoined;
    }
}
