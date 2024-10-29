package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventViewActivity extends AppCompatActivity {

    private ListView eventListView;
    private EventArrayAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_manage_events);

        eventListView = findViewById(R.id.event_list_view);
        eventList = new ArrayList<>();

        // Initialize adapter
        adapter = new EventArrayAdapter(this, eventList);
        eventListView.setAdapter(adapter);
    }


}

