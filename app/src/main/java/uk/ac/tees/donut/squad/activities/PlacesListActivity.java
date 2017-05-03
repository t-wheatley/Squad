package uk.ac.tees.donut.squad.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.LocPlace;

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
    Button btnDistance;

    String squadId;
    Boolean squad;

    int filter;
    int loadingCount;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION;

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
        btnDistance = (Button) findViewById(R.id.placesList_btnDistance);
        btnDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterDistance();
            }
        });

        filter = 0;
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
        // Database reference to get a Squad's Places
        Query allQuery = mDatabase.child("places");

        // Check to see if any Places exist
        checkForEmpty(allQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<LocPlace, PlacesListActivity.PlaceViewHolder>(
                LocPlace.class,
                R.layout.item_three_text,
                PlacesListActivity.PlaceViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                populatePlaceViewHolder(viewHolder, model, position);
            }

            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex)
            {
                switch (type) {
                    case ADDED:
                        notifyItemInserted(index);
                        updateFilter();
                        break;
                    case CHANGED:
                        notifyItemChanged(index);
                        updateFilter();
                        break;
                    case REMOVED:
                        notifyItemRemoved(index);
                        updateFilter();
                        break;
                    case MOVED:
                        notifyItemMoved(oldIndex, index);
                        updateFilter();
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        };
    }

    public void getSquad(String squadId)
    {
        // Database reference to get a Squad's Places
        Query squadQuery = mDatabase.child("places").orderByChild("squad").equalTo(squadId);

        // Check to see if any Places exist
        checkForEmpty(squadQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<LocPlace, PlacesListActivity.PlaceViewHolder>(
                LocPlace.class,
                R.layout.item_three_text,
                PlacesListActivity.PlaceViewHolder.class,
                squadQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                populatePlaceViewHolder(viewHolder, model, position);
            }

            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex)
            {
                switch (type) {
                    case ADDED:
                        notifyItemInserted(index);
                        updateFilter();
                        break;
                    case CHANGED:
                        notifyItemChanged(index);
                        updateFilter();
                        break;
                    case REMOVED:
                        notifyItemRemoved(index);
                        updateFilter();
                        break;
                    case MOVED:
                        notifyItemMoved(oldIndex, index);
                        updateFilter();
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        };
    }

    // Checks if Places in the selected query exist
    public void checkForEmpty(Query query, PlaceAdapter adapter)
    {
        if(query != null)
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
                    adapterObserver(mAdapter, null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        } else
        {
            // Hide the loading screen
            loadingOverlay.setVisibility(View.GONE);

            // Checks if Places exist
            if (adapter.getItemCount() != 0)
            {
                listText.setVisibility(View.GONE);
            } else
            {
                listText.setVisibility(View.VISIBLE);
            }

            // Add an Observer to the RecyclerView
            adapterObserver(null, adapter);
        }
    }

    // An observer on the RecyclerView to check if empty on changes
    public void adapterObserver(final FirebaseRecyclerAdapter fbAdapter, final PlaceAdapter placeAdapter)
    {
        if(fbAdapter != null)
        {
            mObserver = new RecyclerView.AdapterDataObserver()
            {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount)
                {
                    if (fbAdapter.getItemCount() == 0)
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
                    if (fbAdapter.getItemCount() == 0)
                    {
                        listText.setVisibility(View.VISIBLE);
                    } else
                    {
                        listText.setVisibility(View.GONE);


                    }
                }
            };

            fbAdapter.registerAdapterDataObserver(mObserver);
        } else if(placeAdapter != null)
        {
            mObserver = new RecyclerView.AdapterDataObserver()
            {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount)
                {
                    if (placeAdapter.getItemCount() == 0)
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
                    if (placeAdapter.getItemCount() == 0)
                    {
                        listText.setVisibility(View.VISIBLE);
                    } else
                    {
                        listText.setVisibility(View.GONE);
                    }
                }
            };

            placeAdapter.registerAdapterDataObserver(mObserver);
        } else
        {
            // something wrong
        }
    }

    public void populatePlaceViewHolder(final PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
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

    public void updateFilter()
    {
        if(filter == 1)
        {
            // Distance filter
            filterDistance();
        }
    }

    public void filterDistance()
    {
        // Displaying the loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("Filtering...");

        filter = 1;

        List<LocPlace> newList = new ArrayList<>();

        for(int i = 0; i < mAdapter.getItemCount(); i++)
        {
            newList.add((LocPlace) mAdapter.getItem(i));
        }

        Collections.sort(newList, new Comparator<LocPlace>()
        {
            public int compare(LocPlace p1, LocPlace p2)
            {
                if ( ContextCompat.checkSelfPermission( PlacesListActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                    ActivityCompat.requestPermissions( PlacesListActivity.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                            MY_PERMISSION_ACCESS_COURSE_LOCATION );
                }


                Location userLoc = null;
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    userLoc = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }


                Location place1 = new Location("place1");
                place1.setLatitude(p1.getLocLat());
                place1.setLongitude(p1.getLocLong());

                Location place2 = new Location("place2");
                place2.setLatitude(p2.getLocLat());
                place2.setLongitude(p2.getLocLong());


                double distance1 = userLoc.distanceTo(place1);
                double distance2 = userLoc.distanceTo(place2);

                if (distance1 < distance2) return -1;
                if (distance1 > distance2) return 1;
                return 0;
            }
        });

        PlaceAdapter distance = new PlaceAdapter(newList);

        checkForEmpty(null, distance);

        mRecyclerView.setAdapter(distance);
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

    public class PlaceAdapter extends RecyclerView.Adapter<PlaceViewHolder>
    {

        private List<LocPlace> placeList;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        public PlaceAdapter(List<LocPlace> placeList)
        {
            this.placeList = placeList;
        }

        @Override
        public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_three_text, parent, false);

            return new PlaceViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PlaceViewHolder holder, int position)
        {
            final LocPlace place = placeList.get(position);
            holder.nameField.setText(place.getName());
            holder.addressField.setText(place.fullAddress());


            // Get Squad name from id
            mDatabase.child("squads").child(place.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    holder.squadField.setText(dataSnapshot.child("name").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            // OnClick
            holder.mView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Stores the current item's key in a string
                    String mId = place.getPlaceId();

                    //Sends the id to the details activity
                    Intent detail = new Intent(PlacesListActivity.this, PlaceDetailsActivity.class);
                    detail.putExtra("placeId", mId);
                    startActivity(detail);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return placeList.size();
        }
    }

}
