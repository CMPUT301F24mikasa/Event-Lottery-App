package com.example.cmput301f24mikasa;

/**
 * Represents a notification sent to a user related to an event.
 * Contains information about the notification text, sender, recipient, and associated event details.
 */
public class Notifications {
    private String notificationID;
    private String notificationText;
    private String recipientDeviceId;
    private String senderName;
    private String eventName;
    private String eventDescription;
    private String eventID;

    /**
     * Constructor to create a notification with all relevant event details.
     *
     * @param notificationText the text content of the notification
     * @param recipientDeviceId the device ID of the recipient
     * @param senderName the name of the sender
     * @param eventName the name of the event associated with the notification
     * @param eventDescription the description of the event
     */
    public Notifications(String notificationText, String recipientDeviceId, String senderName, String eventName, String eventDescription) {
        this.notificationText = notificationText;
        this.recipientDeviceId = recipientDeviceId;
        this.senderName = senderName;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
    }

    /**
     * Constructor to create a notification with the ID and event information.
     *
     * @param ID the unique notification ID
     * @param notificationText the text content of the notification
     * @param eventID the ID of the associated event
     */
    public Notifications(String ID, String notificationText, String eventID) {
        this.notificationID=ID;
        this.notificationText = notificationText;
        this.eventID=eventID;
    }

    /**
     * Default constructor for Notifications class.
     */
    public Notifications() {}

    /**
     * Gets the event ID associated with this notification.
     *
     * @return the event ID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the event ID associated with this notification.
     *
     * @param eventID the event ID to set
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Gets the unique notification ID.
     *
     * @return the notification ID
     */
    public String getNotificationID() {
        return notificationID;
    }

    /**
     * Sets the unique notification ID.
     *
     * @param notificationID the notification ID to set
     */
    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    /**
     * Gets the text content of the notification.
     *
     * @return the notification text
     */
    public String getNotificationText() {
        return notificationText;
    }

    /**
     * Sets the text content of the notification.
     *
     * @param notificationText the notification text to set
     */
    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    /**
     * Gets the recipient's device ID for the notification.
     *
     * @return the recipient's device ID
     */
    public String getRecipientDeviceId() {
        return recipientDeviceId;
    }

    /**
     * Sets the recipient's device ID for the notification.
     *
     * @param recipientDeviceId the recipient's device ID to set
     */
    public void setRecipientDeviceId(String recipientDeviceId) {
        this.recipientDeviceId = recipientDeviceId;
    }

    /**
     * Gets the name of the sender of the notification.
     *
     * @return the sender's name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the name of the sender of the notification.
     *
     * @param senderName the sender's name to set
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Gets the name of the event associated with this notification.
     *
     * @return the event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the name of the event associated with this notification.
     *
     * @param eventName the event name to set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the description of the event associated with this notification.
     *
     * @return the event description
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Sets the description of the event associated with this notification.
     *
     * @param eventDescription the event description to set
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * Displays the notification details as a formatted string.
     *
     * @return a string representation of the notification, including sender, event, and message
     */
    // Additional Method: Display Notification Details
    public String displayNotification() {
        return "Notification from: " + senderName + "\nFor event: " + eventName + "\nMessage: " + notificationText;
    }
}