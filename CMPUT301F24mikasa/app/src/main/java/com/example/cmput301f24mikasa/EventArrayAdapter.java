package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * EventArrayAdapter is a custom ArrayAdapter for displaying a list of Event objects
 * in a ListView, with buttons for viewing, editing, and deleting each event.
 * It enables interaction with each event item in the list through button click listeners.
 *
 * <p>This adapter uses the OnEventClickListener interface to communicate
 * button click events to the activity or fragment that initializes this adapter.
 *
 * @version 1.0
 * @since 2024-11-08
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final List<Event> eventList;
    private final OnEventClickListener listener;


    /**
     * Interface to define the actions for each button in the event item layout.
     * This interface must be implemented by the activity or fragment that uses
     * this adapter to handle button click events.
     */
    public interface OnEventClickListener {
        /**
         * Called when the "View" button is clicked for a specific event.
         *
         * @param event The event associated with the button click.
         */
        void onViewButtonClick(Event event);

        /**
         * Called when the "Delete" button is clicked for a specific event.
         *
         * @param event The event associated with the button click.
         */
        void onDeleteButtonClick(Event event);

        /**
         * Called when the "Edit" button is clicked for a specific event.
         *
         * @param event The event associated with the button click.
         */
        void onEditButtonClick(Event event);
    }

    /**
     * Constructs a new EventArrayAdapter.
     *
     * @param context The current context.
     * @param eventList The list of Event objects to be displayed.
     * @param listener The listener for handling button click events.
     */
    public EventArrayAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        super(context, R.layout.activity_event_list_item, eventList);
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    /**
     * Creates and returns a View for each event item in the list.
     *
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return The View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_event_list_item, parent, false);

        // Event data
        TextView titleTextView = rowView.findViewById(R.id.event_title);

        // Initialize Buttons
        Button viewButton = rowView.findViewById(R.id.view_button);
        Button deleteButton = rowView.findViewById(R.id.delete_button);
        Button editButton = rowView.findViewById(R.id.edit_button);

        // Set event title
        Event event = eventList.get(position);
        titleTextView.setText(event.getTitle());

        // Set view button click listener
        viewButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewButtonClick(event);
            }
        });

        // Set delete button click listener
        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteButtonClick(event);
            }
        });

        // Set edit button click listener
        editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditButtonClick(event);
            }
        });

        return rowView;
    }
}
