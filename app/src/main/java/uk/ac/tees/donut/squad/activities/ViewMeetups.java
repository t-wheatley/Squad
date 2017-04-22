package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.users.User;

public class ViewMeetups extends AppCompatActivity
{
    RecyclerView recycler;
    DatabaseReference mDatabase;
    FirebaseRecyclerAdapter mAdapter;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    boolean attending = false;
    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meetups);
        this.setTitle("Current Meetups");

        Bundle b = getIntent().getExtras();
        if(b != null)
        {
            if (b.containsKey("ATT"))
                attending = b.getBoolean("ATT");
        }

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");

        // Defining RecyclerView
        recycler = (RecyclerView) findViewById(R.id.viewMeetups_recyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Load meetups and display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingOverlay.setVisibility(View.VISIBLE);

        if(attending)   //if came from 'Attending Meetups' button on profile...
        {
            loadingText.setText("Loading your Meetups...");
            getAttending();
        }
        else
        {
            loadingText.setText("Loading Meetups...");
            getAll();
        }


        // Display the adapter in the RecyclerView
        recycler.setAdapter(mAdapter);
    }

    public void getAll()
    {
        // Count for hiding the progress bar
        loadingCount = 1;

        // Firebase RecyclerView adapter, finds all meetups and displays them in a RecyclerView
        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupHolder>(Meetup.class, android.R.layout.two_line_list_item, MeetupHolder.class, mDatabase)
        {
            @Override
            protected void populateViewHolder(MeetupHolder meetupViewHolder, final Meetup meetup, int position)
            {
                // Gets the current meetups data and creates a meetupViewHolder
                meetupViewHolder.setName(meetup.getName());
                meetupViewHolder.setInterest(meetup.getSquad());

                // OnClickListener for the item, displays a details page for the meetup
                meetupViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        // Stores the current item's key in a string
                        String mId = meetup.getId();

                        // Sends the id to the details activity
                        Intent detail = new Intent(ViewMeetups.this, MeetupDetailActivity.class);
                        detail.putExtra("meetupId", mId);
                        startActivity(detail);
                    }
                });

                // If loading the last item
                if (mAdapter.getItemCount() == loadingCount)
                {
                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                }

                loadingCount++;
            }


        };
    }

    public void getAttending()
    {
        // Count for hiding the progress bar
        loadingCount = 1;

        // Firebase RecyclerView adapter, finds all meetups and displays them in a RecyclerView
        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupHolder>(Meetup.class, android.R.layout.two_line_list_item, MeetupHolder.class, mDatabase)
        {
            @Override
            protected void populateViewHolder(MeetupHolder meetupViewHolder, final Meetup meetup, int position)
            {
                if(User.myMeetupsContains(meetup.getId()))
                {
                    // Gets the current meetups data and creates a meetupViewHolder
                    meetupViewHolder.setName(meetup.getName());
                    meetupViewHolder.setInterest(meetup.getSquad());

                    // OnClickListener for the item, displays a details page for the meetup
                    meetupViewHolder.mView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            // Stores the current item's key in a string
                            String mId = meetup.getId();

                            // Sends the id to the details activity
                            Intent detail = new Intent(ViewMeetups.this, MeetupDetailActivity.class);
                            detail.putExtra("meetupId", mId);
                            startActivity(detail);
                        }
                    });
                }

                // If loading the last item
                if (mAdapter.getItemCount() == loadingCount)
                {
                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                }

                loadingCount++;
            }
        };
    }

}