package com.example.cmput301f24mikasa;

public class Notifications {
    private String notificationText;
    private String recipientDeviceId;
    private String senderName;
    private String eventName;
    private String eventDescription;

    // Constructor
    public Notifications(String notificationText, String recipientDeviceId, String senderName, String eventName, String eventDescription) {
        this.notificationText = notificationText;
        this.recipientDeviceId = recipientDeviceId;
        this.senderName = senderName;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
    }

    // Getters and Setters
    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getRecipientDeviceId() {
        return recipientDeviceId;
    }

    public void setRecipientDeviceId(String recipientDeviceId) {
        this.recipientDeviceId = recipientDeviceId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    // Additional Method: Display Notification Details
    public String displayNotification() {
        return "Notification from: " + senderName + "\nFor event: " + eventName + "\nMessage: " + notificationText;
    }
}
