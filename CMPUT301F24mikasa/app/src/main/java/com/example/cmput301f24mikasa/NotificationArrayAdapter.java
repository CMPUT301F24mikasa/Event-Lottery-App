package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * ArrayAdapter to display notifications in a ListView.
 * This adapter binds the notification data to the ListView for rendering.
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notifications> {
    private final Context context;
    private final List<Notifications> notificationList;

    /**
     * Constructor for the NotificationArrayAdapter.
     * Initializes the adapter with the context and the list of notifications to display.
     *
     * @param context the context where the adapter is used (usually an activity)
     * @param notificationList the list of notifications to display
     */
    public NotificationArrayAdapter(Context context, List<Notifications> notificationList) {
        super(context, R.layout.activity_notification_item, notificationList);
        this.context = context;
        this.notificationList = notificationList;
    }

    /**
     * Gets the view for each item in the list of notifications.
     * Inflates a new view or reuses an existing one and binds the notification text to the TextView.
     *
     * @param position the position of the item within the data set
     * @param convertView the recycled view to reuse (if any)
     * @param parent the parent view group
     * @return the view for the notification item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.activity_notification_item, parent, false);
        }

        TextView notificationTextView = rowView.findViewById(R.id.notification_text);
        Notifications notification = notificationList.get(position);
        notificationTextView.setText(notification.getNotificationText());

        return rowView;
    }
}
