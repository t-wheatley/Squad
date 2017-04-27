package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;

public class MeetupsListActivity extends AppCompatActivity
{

    private DatabaseReference mDatabase;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    String userId;
    String squadId;
    Boolean host;
    Boolean member;
    Boolean squad;

    RelativeLayout loadingOverlay;
    TextView loadingText;
    TextView listText;

    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetups_list);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Meetups...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.meetupsList_recyclerView);
        listText = (TextView) findViewById(R.id.meetupsList_textView);

        member = false;
        host = false;
        squad = false;

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            if (b.get("squadId") != null)
            {
                // Squad mode
                squadId = (String) b.get("squadId");
                squad = true;
            } else if (b.get("userId") != null)
            {
                // Collects the userId passed from the RecyclerView
                userId = (String) b.get("userId");
                if (b.get("host") != null)
                {
                    if ((Boolean) b.get("host"))
                    {
                        // Host mode
                        host = true;
                    }
                } else
                {
                    // Member mode
                    member = true;
                }
            }
        }

        // Base database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        // Setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // If came from 'View Meetups' button on profile
        if (host)
        {
            getHosted(userId);
        } else if (member)
        {
            getUsers(userId);
        } else if (squad)
        {
            getSquad(squadId);
        } else
        {
            getAll();
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    public void getAll()
    {
        // Database reference to get all Meetups
        Query allQuery = mDatabase.child("meetups");

        // Check to see if any Meetups exist
        checkForEmpty(allQuery);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_three_text,
                MeetupViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                populateMeetupViewHolder(viewHolder, model, position);
            }
        };

    }

    public void getUsers(String userId)
    {
        // Database reference to get a User's Meetups
        Query userQuery = mDatabase.child("meetups").orderByChild("users/" + userId).equalTo(true);

        // Check to see if any Meetups exist
        checkForEmpty(userQuery);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_three_text,
                MeetupViewHolder.class,
                userQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                populateMeetupViewHolder(viewHolder, model, position);
            }
        };

    }

    public void getHosted(String userId)
    {
        // Database reference to get a Host's Meetups
        Query hostQuery = mDatabase.child("meetups").orderByChild("host").equalTo(userId);

        // Check to see if any Meetups exist
        checkForEmpty(hostQuery);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_three_text,
                MeetupViewHolder.class,
                hostQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                populateMeetupViewHolder(viewHolder, model, position);
            }
        };


    }

    public void getSquad(String squadId)
    {
        // Database reference to get a Squad's Meetups
        Query squadQuery = mDatabase.child("meetups").orderByChild("squad").equalTo(squadId);

        // Check to see if any Meetups exist
        checkForEmpty(squadQuery);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_three_text,
                MeetupViewHolder.class,
                squadQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                populateMeetupViewHolder(viewHolder, model, position);
            }
        };
    }

    // Checks if Meetups in the selected query exist
    public void checkForEmpty(Query query)
    {
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Hide the loading screen
                loadingOverlay.setVisibility(View.GONE);

                // Checks if Meetups will be found
                if (dataSnapshot.hasChildren())
                {
                    listText.setVisibility(View.GONE);
                } else
                {
                    listText.setVisibility(View.VISIBLE);
                }

                // Add an Observer to the RecyclerView
                adapterObserver();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    // An observer on the RecyclerView to check if empty on changes
    public void adapterObserver()
    {
        mObserver = new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                if (mAdapter.getItemCount() == 0)
                {
                    listText.setVisibility(View.VISIBLE);
                } else
                {
                    listText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount)
            {
                if (mAdapter.getItemCount() == 0)
                {
                    listText.setVisibility(View.VISIBLE);
                } else
                {
                    listText.setVisibility(View.GONE);
                }
            }
        };
        mAdapter.registerAdapterDataObserver(mObserver);
    }


    public void populateMeetupViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
    {

        viewHolder.nameField.setText(model.getName());

        String description = model.getDescription().replace("\n", "");
        String elipsis = "";
        if (description.length() > 54)
            elipsis = "...";

        String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

        viewHolder.descriptionfield.setText(shortDesc);

        // Get Squad name from id
        mDatabase.child("squads").child(model.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                viewHolder.squadField.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        viewHolder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Stores the current item's key in a string
                String mId = model.getId();

                //Sends the id to the details activity
                Intent detail = new Intent(MeetupsListActivity.this, MeetupDetailActivity.class);
                detail.putExtra("meetupId", mId);
                startActivity(detail);
            }
        });

        // If loading the last item or empty
        /*
        if ((mAdapter.getItemCount() == loadingCount))
        {
            // Hide the loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }
        loadingCount++;
        */
    }

    public static class MeetupViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView descriptionfield;
        TextView squadField;

        public MeetupViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.text1);
            descriptionfield = (TextView) v.findViewById(R.id.text2);
            squadField = (TextView) v.findViewById(R.id.text3);
        }
    }
}
