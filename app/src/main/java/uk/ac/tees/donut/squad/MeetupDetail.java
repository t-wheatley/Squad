package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.posts.Meetup;

public class MeetupDetail extends AppCompatActivity
{
    DatabaseReference mDatabase;

    EditText nameDisplay;
    EditText interestDisplay;
    EditText descriptionDisplay;
    String meetupId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_detail);

        // Declaring editTexts
        nameDisplay = (EditText) findViewById(R.id.meetupDetail_textEditName);
        interestDisplay = (EditText) findViewById(R.id.meetupDetail_textEditInterest);
        descriptionDisplay = (EditText) findViewById(R.id.meetupDetail_textEditDescription);

        // Disabling the editTexts
        nameDisplay.setEnabled(false);
        interestDisplay.setEnabled(false);
        descriptionDisplay.setEnabled(false);

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        if(b != null)
        {
            // Collects the meetupId passed from the RecyclerView
            meetupId = (String) b.get("meetupId");
            this.setTitle(meetupId);
        }
        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");

        // Reads the data from the meetupId in Firebase
        mDatabase.child(meetupId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Meetup class
                Meetup meetup = dataSnapshot.getValue(Meetup.class);

                // Displays the found meetup's attributes
                nameDisplay.setText(meetup.getName());
                interestDisplay.setText(meetup.getInterest());
                descriptionDisplay.setText(meetup.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
