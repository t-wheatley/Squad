package uk.ac.tees.donut.squad.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.users.User;

/**
 * Created by q5071488 on 14/03/2017.
 */

public class FirebaseDB
{
    List<Meetup> meetupList;

    // Takes a meetup and pushes it to the Firebase Realtime Database (Without extras)
    public void createMeetup(Meetup meetup)
    {
        // Create a reference to the meetups table of the db
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("meetups");

        // Creating a new user node and getting the key value
        String meetupId = db.push().getKey();

        // Setting the meetups Id
        meetup.setId(meetupId);

        // Pushing the meetup to the "meetups" node using the meetupId
        db.child(meetupId).setValue(meetup);
    }

    //Get all meetups in the database
    public List getMeetups()
    {
        meetupList = new ArrayList<Meetup>();

        // Create a reference to the meetups table of the db
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("meetups");

        db.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot postSnapShot: dataSnapshot.getChildren())
                {
                    Meetup post = postSnapShot.getValue(Meetup.class);
                    Log.w("Firebase", post.getId());
                    meetupList.add(post);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Getting Post failed, log a message
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException());
            }
        });


        Log.w("Firebase", Integer.toString(meetupList.size()));
        return meetupList;
    }

    public Meetup getMeetup(String id)
    {
        // Create a reference to the meetups table of the db
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        //Meetup meetup = new Meetup(db.child("meetups").orderByChild("id").equalTo(id));

        return null;
    }
}
