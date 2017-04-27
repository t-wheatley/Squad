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
import uk.ac.tees.donut.squad.posts.AddressPlace;

public class PlacesListActivity extends AppCompatActivity
{

    private DatabaseReference mDatabase;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    RelativeLayout loadingOverlay;
    TextView loadingText;
    TextView listText;

    String squadId;
    Boolean squad;
    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Places...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        //initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        listText = (TextView) findViewById(R.id.placesList_textView);

        squad = false;

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            // Collects the userId passed from the RecyclerView
            squadId = (String) b.get("squadId");
            squad = true;
        }

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        // Setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // If came from 'View Places' button on Squad
        if (squad)
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
        // Database reference to get a Squad's Meetups
        Query allQuery = mDatabase.child("places");

        // Check to see if any Meetups exist
        checkForEmpty(allQuery);

        mAdapter = new FirebaseRecyclerAdapter<AddressPlace, PlacesListActivity.PlaceViewHolder>(
                AddressPlace.class,
                R.layout.item_three_text,
                PlacesListActivity.PlaceViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final AddressPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                populatePlaceViewHolder(viewHolder, model, position);
            }
        };
    }

    public void getSquad(String squadId)
    {
        // Database reference to get a Squad's Meetups
        Query squadQuery = mDatabase.child("places").orderByChild("squad").equalTo(squadId);

        // Check to see if any Meetups exist
        checkForEmpty(squadQuery);

        mAdapter = new FirebaseRecyclerAdapter<AddressPlace, PlacesListActivity.PlaceViewHolder>(
                AddressPlace.class,
                R.layout.item_three_text,
                PlacesListActivity.PlaceViewHolder.class,
                squadQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final AddressPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                populatePlaceViewHolder(viewHolder, model, position);
            }
        };
    }

    // Checks if Places in the selected query exist
    public void checkForEmpty(Query query)
    {
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Hide the loading screen
                loadingOverlay.setVisibility(View.GONE);

                // Checks if Places will be found
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

    public void populatePlaceViewHolder(final PlacesListActivity.PlaceViewHolder viewHolder, final AddressPlace model, int position)
    {

        viewHolder.nameField.setText(model.getName());
        viewHolder.addressField.setText(model.fullAddress());

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
                String mId = model.getPlaceId();
                //Sends the id to the details activity
                Intent detail = new Intent(PlacesListActivity.this, PlaceDetailsActivity.class);
                detail.putExtra("placeId", mId);
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

    public static class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView addressField;
        TextView squadField;

        public PlaceViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.text1);
            addressField = (TextView) v.findViewById(R.id.text2);
            squadField = (TextView) v.findViewById(R.id.text3);
        }
    }

}
