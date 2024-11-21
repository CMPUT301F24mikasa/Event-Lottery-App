package com.example.cmput301f24mikasa;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * AdminEventAdapter is a custom ArrayAdapter for displaying a list of Event objects
 * in a ListView, with buttons for viewing and deleting each event.
 * It enables interaction with each event item in the list through button click listeners,
 * without the edit button.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private ImageView eventImage;
    private Context context;
    private List<Event> eventList;
    private OnEventClickListener onEventClickListener;

    public interface OnEventClickListener {
        void onViewButtonClick(Event event);
        void onDeleteButtonClick(Event event);
    }

    public AdminEventAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = listener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_admin_event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventNameTextView.setText(event.getTitle());

        if (event.getPosterRef() != null) {
            Glide.with(context)
                    .load(event.getPosterRef())
                    .into(holder.eventImageView);
        } else {
            holder.eventImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.viewButton.setOnClickListener(v -> onEventClickListener.onViewButtonClick(event));
        holder.deleteButton.setOnClickListener(v -> onEventClickListener.onDeleteButtonClick(event));
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class to hold the views for each event item
    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        ImageView eventImageView;
        Button viewButton, deleteButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            eventImageView = itemView.findViewById(R.id.event_image);
            viewButton = itemView.findViewById(R.id.view_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

