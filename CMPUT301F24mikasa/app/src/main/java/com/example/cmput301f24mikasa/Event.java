package com.example.cmput301f24mikasa;

import com.google.firebase.firestore.DocumentReference;

/**
 * Represents an event in the application. This class includes the details of an event
 * such as title, description, start date, capacity, price, and associated poster and device IDs.
 * It provides an empty constructor required for Firestore serialization, and a parameterized
 * constructor for creating an event with specific details.
 *
 * <p>Fields in this class are designed to map to Firestore document fields for easy
 * data storage and retrieval.
 *
 * @version 1.0
 * @since 2024-11-08
 */
public class Event {
    private String eventID;
    private String title;
    private String description;
    private String startDate;
    private int capacity;
    private String price;
    private DocumentReference posterRef; // Keep as String to match Firestore format
    private String deviceID;

    /**
     * Empty constructor required for Firestore serialization.
     */

    // Empty constructor required for Firestore serialization
    public Event() {}

    /**
     * Constructs an Event object with specified details.
     *
     * @param eventID      Unique identifier for the event.
     * @param title        Title of the event.
     * @param description  Description of the event.
     * @param startDate    Start date of the event.
     * @param capacity     Maximum capacity of attendees.
     * @param price        Price of the event.
     * @param posterRef    Reference to the event's poster document in Firestore.
     * @param deviceID     Device ID of the event creator.
     */

    // Constructor
    public Event(String eventID, String title, String description, String startDate, int capacity, String price, DocumentReference posterRef, String deviceID) {
        this.eventID = eventID;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.capacity = capacity;
        this.price = price;
        this.posterRef = posterRef;
        this.deviceID = deviceID;
    }

    /**
     * Gets the event ID.
     *
     * @return the unique event ID.
     */

    // Getters and setters
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the event ID.
     *
     * @param eventID the unique event ID to set.
     */

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Gets the title of the event.
     *
     * @return the event title.
     */

    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     *
     * @param title the event title to set.
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of the event.
     *
     * @return the event description.
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the event description to set.
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the start date of the event.
     *
     * @return the start date of the event.
     */

    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the event.
     *
     * @param startDate the start date to set.
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the maximum capacity of attendees for the event.
     *
     * @return the event capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum capacity of attendees for the event.
     *
     * @param capacity the capacity to set.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the price of the event.
     *
     * @return the event price.
     */
    public String getPrice() {
        return price;
    }

    /**
     * Sets the price of the event.
     *
     * @param price the price to set.
     */
    public void setPrice(String price) {
        this.price = price;
    }


    /**
     * Gets the poster reference for the event in Firestore.
     *
     * @return the Firestore document reference to the poster.
     */
    public DocumentReference getPosterRef() {
        return posterRef;
    }


    /**
     * Sets the poster reference for the event in Firestore.
     *
     * @param posterRef the Firestore document reference to set for the poster.
     */
    public void setPosterRef(DocumentReference posterRef) {
        this.posterRef = posterRef;
    }

    /**
     * Gets the device ID of the event creator.
     *
     * @return the device ID of the event creator.
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets the device ID of the event creator.
     *
     * @param deviceID the device ID to set.
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}