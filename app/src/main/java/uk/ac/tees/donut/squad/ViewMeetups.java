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
import uk.ac.tees.donut.squad.users.User;

public class ViewMeetups extends AppCompatActivity
{

    boolean attending = false;
    User user;

    RecyclerView recycler;
    DatabaseReference mDatabase;
    FirebaseRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meetups);
        this.setTitle("Current Meetups");

        Bundle b = getIntent().getExtras();
        if(b != null)
        {
            user = (User) b.getSerializable("USER");
            if (b.containsKey("ATT"))
                attending = b.getBoolean("ATT");
        }


        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");

        // Defining RecyclerView
        recycler = (RecyclerView) findViewById(R.id.viewMeetups_recyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Notification for user
        Toast.makeText(this, "Loading meetups...", Toast.LENGTH_SHORT).show();

        if(attending)
            getAttending();
        else
            getAll();

        // Display the adapter in the RecyclerView
        recycler.setAdapter(mAdapter);
    }

    public void getAll()
    {
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
    }

    public void getAttending()
    {
        // Firebase RecyclerView adapter, finds all meetups and displays them in a RecyclerView
        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupHolder>(Meetup.class, android.R.layout.two_line_list_item, MeetupHolder.class, mDatabase)
        {
            @Override
            protected void populateViewHolder(MeetupHolder meetupViewHolder, final Meetup meetup, int position)
            {
                if(user.myMeetupsContains(meetup.getId()))
                {
                    // Gets the current meetups data and creates a meetupViewHolder
                    meetupViewHolder.setName(meetup.getName());
                    meetupViewHolder.setInterest(meetup.getInterest());

                    // OnClickListener for the item, displays a details page for the meetup
                    meetupViewHolder.mView.setOnClickListener(new View.OnClickListener()
                    {
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
            }
        };
    }
}
