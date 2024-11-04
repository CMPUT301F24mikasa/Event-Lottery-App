package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class WaitingListActivity extends AppCompatActivity{
    private ArrayList<UserProfile> dataList;
    private ListView userList;
    private UserProfileArrayAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_waiting_list);

        // Retrieve the event title and eventID passed from OrganizerManageEventsActivity
        Intent intent = getIntent();
        String eventID=intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");
        //checker
        Toast.makeText(this, "Event ID: " + eventID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Event title: " + eventTitle, Toast.LENGTH_SHORT).show();


        // Now you can safely use eventTitle
        TextView headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText("Waiting List For " + eventTitle);

        // Initialize the back button
        Button backButton = findViewById(R.id.back_button_for_waiting);
        backButton.setOnClickListener(view -> finish()); // This will close the current activity and return to the previous one

        /*
         String[] name = { "Edmonton", "Vancouver", "Toronto" };
         //String[] authors = { "AB", "BC", "ON" };
         //String[] genre = { "fc", "fc", "fc" };
         //String[] year = { "2004", "2004", "2004" };
         //String[] status = { "Read", "Read", "Read" };


         dataList = new ArrayList<UserProfile>();
         for (int i = 0; i < name.length; i++) {
         dataList.add(new UserProfile(name[i], "aaa","aa","aaaaa","",false));
         }


         userList = findViewById(R.id.waiting_list_view);
         userAdapter = new UserProfileArrayAdapter(this, dataList);
         userList.setAdapter(userAdapter);
         */

    }
}
