package com.example.cmput301f24mikasa;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//TODO
// FIND WAY TO USE THIS getEvent in other activiites


// MAKE SURE YOU INITIALIZE     private GetEvent getEvent; wherever you need to retrieve an event

public class GetEvent {

    private FirebaseFirestore db;

    public GetEvent(){
        db = FirebaseFirestore.getInstance();
    }

    public interface EventCallback{ // Handle whether or not we are able to retrieve event

        // Referenced https://www.onespan.com/blog/callback-methods-overview by OneSpan Team, 2024-10-27
        void onSuccess(DocumentSnapshot documentSnapshot);
        void onFailure(Exception e);
    }

    public void getEvent(String eventID,Context context, EventCallback eventCallback){
        DocumentReference eventRef = db.collection("event").document(eventID); // retrieve event based on eventID
        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    eventCallback.onSuccess(documentSnapshot);
                    Toast.makeText(context, "Event retrieved successfully", Toast.LENGTH_SHORT).show();
                } else{ // if for whatever reason, the event exists but we are unable to find it.
                    Toast.makeText(context, "Unable to retrieve event", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Event does not exist. ", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
