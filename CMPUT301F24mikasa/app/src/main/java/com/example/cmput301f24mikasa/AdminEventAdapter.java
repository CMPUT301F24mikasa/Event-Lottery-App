package com.example.cmput301f24mikasa;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * AdminEventAdapter is a custom ArrayAdapter for displaying a list of Event objects
 * in a ListView, with buttons for viewing and deleting each event.
 * It enables interaction with each event item in the list through button click listeners,
 * without the edit button.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventList;


    public AdminEventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
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

        if (event.getImageURL() != null) {
            Glide.with(context)
                    .load(event.getImageURL())
                    .into(holder.eventImageView);
        } else {
            holder.eventImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventID());
            ((AdminManageEventsActivity) context).startActivityForResult(intent, 1);
        });

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("event").document(event.getEventID())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        eventList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, eventList.size());
                        Toast.makeText(context, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

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
