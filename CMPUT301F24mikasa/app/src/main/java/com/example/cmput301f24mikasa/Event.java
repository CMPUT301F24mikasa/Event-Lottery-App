package com.example.cmput301f24mikasa;

import com.google.firebase.firestore.DocumentReference;

public class Event {
    private String eventID;
    private String title;
    private String description;
    private String startDate;
    private int capacity;
    private String price;
    private DocumentReference posterRef; // Keep as String to match Firestore format
    private String deviceID;

    // Empty constructor required for Firestore serialization
    public Event() {}

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

    // Getters and setters
    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    // Getters and Setters
    public DocumentReference getPosterRef() {
        return posterRef;
    }

    public void setPosterRef(DocumentReference posterRef) {
        this.posterRef = posterRef;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
