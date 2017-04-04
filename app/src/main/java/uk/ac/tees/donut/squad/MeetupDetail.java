package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.users.User;

public class MeetupDetail extends AppCompatActivity
{
    DatabaseReference mDatabase;

    TextView nameDisplay;
    TextView interestDisplay;
    TextView descriptionDisplay;
    String meetupId;

    Button attendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_detail);

        // Declaring editTexts
        nameDisplay = (TextView) findViewById(R.id.meetupDetail_textEditName);
        interestDisplay = (TextView) findViewById(R.id.meetupDetail_textEditInterest);
        descriptionDisplay = (TextView) findViewById(R.id.meetupDetail_textEditDescription);

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
            this.setTitle("Meetup Details");
        }

        //getting attend Button
        attendBtn = (Button) findViewById(R.id.attendBtn);

        if(User.myMeetupsContains(meetupId))
            attendBtn.setText("Unattend Meetup");
            
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

    public void attend(View view)
    {
        if(User.myMeetupsContains(meetupId))
        {
            User.removeMeetup(meetupId);
            attendBtn.setText("Attend Button");
        }
        else
        {
            User.addMeetup(meetupId);
            attendBtn.setText("Unattend Button");
        }
    }
}
