package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final List<Event> eventList;
    private final OnEventClickListener listener; // Add listener
    private boolean isAdmin;

    // Define the interface for button clicks
    public interface OnEventClickListener {
        void onViewButtonClick(Event event);
        void onDeleteButtonClick(Event event);
        void onEditButtonClick(Event event);
    }

    // Update constructor to accept listener
    public EventArrayAdapter(Context context, List<Event> eventList, OnEventClickListener listener, boolean isAdmin) {
        super(context, R.layout.activity_event_list_item, eventList);
        this.context = context;
        this.eventList = eventList;
        this.listener = listener; // Store listener reference
        this.isAdmin = isAdmin;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_event_list_item, parent, false);

        // Event data
        TextView titleTextView = rowView.findViewById(R.id.event_title);

        // Buttons
        Button viewButton = rowView.findViewById(R.id.view_button);
        Button deleteButton = rowView.findViewById(R.id.delete_button);
        Button editButton = rowView.findViewById(R.id.edit_button);

        // Set event title
        Event event = eventList.get(position);
        titleTextView.setText(event.getTitle());

        // Set button click listeners
        viewButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewButtonClick(event);
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteButtonClick(event);
            }
        });

        if (isAdmin) {
            editButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditButtonClick(event);
                }
            });
        }
        return rowView;
    }
}
