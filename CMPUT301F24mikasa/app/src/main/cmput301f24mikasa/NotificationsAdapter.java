package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class NotificationsAdapter extends ArrayAdapter<Notifications> {
    private final Context context;
    private final List<Notifications> notificationsList;

    public NotificationsAdapter(Context context, List<Notifications> notificationsList) {
        super(context, R.layout.activity_notification_item, notificationsList);
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Inflate the view if not already done
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_notification_item, parent, false);
        }

        // Get the current notification
        Notifications notification = notificationsList.get(position);

        // Find the TextView in the layout and set the notification text
        TextView notificationText = convertView.findViewById(R.id.notification_text);
        notificationText.setText(notification.getNotificationText());

        return convertView;
    }

}
