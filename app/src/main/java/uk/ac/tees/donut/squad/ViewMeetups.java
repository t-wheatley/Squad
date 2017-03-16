package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.posts.Meetup;

public class ViewMeetups extends AppCompatActivity
{

    RecyclerView recycler;
    DatabaseReference mDatabase;
    FirebaseRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meetups);
        this.setTitle("Current Meetups");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");

        // Defining RecyclerView
        recycler = (RecyclerView) findViewById(R.id.viewMeetups_recyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Notification for user
        Toast.makeText(this, "Loading meetups...", Toast.LENGTH_SHORT).show();

        // Firebase RecyclerView adapter, finds all meetups and displays them in a RecyclerView
        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupHolder>(Meetup.class, android.R.layout.two_line_list_item, MeetupHolder.class, mDatabase)
        {
            @Override
            protected void populateViewHolder(MeetupHolder meetupViewHolder, final Meetup meetup, int position)
            {
                // Gets the current meetups data and creates a meetupViewHolder
                meetupViewHolder.setName(meetup.getName());
                meetupViewHolder.setInterest(meetup.getInterest());

                // OnClickListener for the item, displays a details page for the meetup
                meetupViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        // Stores the current item's key in a string
                        String mId = meetup.getId();

                        // Sends the id to the details activity
                        Intent detail = new Intent(ViewMeetups.this, MeetupDetail.class);
                        detail.putExtra("meetupId", mId);
                        startActivity(detail);
                    }
                });
            }
        };

        // Display the adapter in the RecyclerView
        recycler.setAdapter(mAdapter);
    }
}
