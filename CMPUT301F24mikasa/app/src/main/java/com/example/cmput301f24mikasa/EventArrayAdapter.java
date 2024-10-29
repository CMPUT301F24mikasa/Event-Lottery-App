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

    public EventArrayAdapter(Context context, List<Event> eventList) {
        super(context, R.layout.activity_event_list_item, eventList);
        this.context = context;
        this.eventList = eventList;
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

        return rowView;
    }
}
