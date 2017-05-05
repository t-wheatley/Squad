package uk.ac.tees.donut.squad.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.posts.Place;
import uk.ac.tees.donut.squad.squads.Squad;

public class PlacesListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
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
    EditText searchBar;

    String squadId;
    Boolean squad;

    RelativeLayout searchMenu;
    LinearLayout burgerMenu;
    FloatingActionButton burgerButton;
    //for whenever the burger menu is open or not
    Boolean burger = false;

    List<LocPlace> searchList;
    List<LocPlace> filteredList;
    PlaceAdapter filteredAdapter;
    boolean search;
    int filter;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location userLoc;
    final int LOCATION_PERMISSION_REQUEST_CODE = 1;

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
        btnDistance = (Button) findViewById(R.id.placesList_btnDistance);
        btnDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterDistance();
            }
        });

        searchMenu = (RelativeLayout) findViewById(R.id.placesList_searchLayout);
        burgerMenu = (LinearLayout) findViewById(R.id.placesList_burgerMenu);
        burgerButton = (FloatingActionButton) findViewById(R.id.placesList_fab);

        searchBar = (EditText) findViewById(R.id.placesList_searchBar);
        searchBar.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() == 0)
                {
                    search = false;
                    updateFilter();
                } else
                {
                    search(searchBar.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        squad = false;

        searchList = new ArrayList<LocPlace>();
        filteredList = new ArrayList<LocPlace>();
        filter = 0;

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

        buildGoogleApiClient();
        mGoogleApiClient.connect();


        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(102);
        mLocationRequest.setInterval(5);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart()
    {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        PlacesListActivity.this.finish();
    }

    public void buildGoogleApiClient()
    {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
                switch (type)
                {
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
                switch (type)
                {
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
        if (query != null)
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
        if (fbAdapter != null)
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
        } else if (placeAdapter != null)
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

    public void search(String searchText)
    {
        search = true;

        searchList.clear();

        if (!searchText.isEmpty())
        {
            // If not filter has been applied yet, needs original list
            if (filter == 0)
            {
                filteredList.clear();

                for (int i = 0; i < mAdapter.getItemCount(); i++)
                {
                    filteredList.add((LocPlace) mAdapter.getItem(i));
                }
            }

            searchText = searchText.toLowerCase();

            for (LocPlace place : filteredList)
            {
                if (place.getName().toLowerCase().contains(searchText))
                {
                    searchList.add(place);
                }
            }

            PlaceAdapter searchAdapter = new PlaceAdapter(searchList);

            checkForEmpty(null, searchAdapter);

            mRecyclerView.setAdapter(searchAdapter);
        } else
        {
            if (filter == 0)
            {
                mRecyclerView.setAdapter(mAdapter);
            } else
            {
                mRecyclerView.setAdapter(filteredAdapter);
            }
        }
    }

    public void updateFilter()
    {
        if (filter == 1)
        {
            // Distance filter
            filterDistance();
        } else
        {
            mRecyclerView.setAdapter(mAdapter);
        }

        if (search = true)
        {
            search(searchBar.getText().toString());
        }
    }

    public void filterDistance()
    {
        if (mGoogleApiClient.isConnected())
        {
            getNewLocation();
        }

        if (userLoc != null)
        {
            // Displaying the loading overlay
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingText.setText("Filtering...");

            filter = 1;

            filteredList = new ArrayList<>();

            for (int i = 0; i < mAdapter.getItemCount(); i++)
            {
                filteredList.add((LocPlace) mAdapter.getItem(i));
            }

            Collections.sort(filteredList, new Comparator<LocPlace>()
            {
                public int compare(LocPlace p1, LocPlace p2)
                {
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

            filteredAdapter = new PlaceAdapter(filteredList);

            checkForEmpty(null, filteredAdapter);

            if (search = true)
            {
                search(searchBar.getText().toString());
            } else
            {
                mRecyclerView.setAdapter(filteredAdapter);
            }
        } else
        {
            new AlertDialog.Builder(PlacesListActivity.this)
                    .setTitle("No Location")
                    .setMessage("Sorry we can't access your location right now, make sure you have Location turned on!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public void getNewLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(PlacesListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        userLoc = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(PlacesListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        getNewLocation();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getNewLocation();
            } else { // if permission is not granted
                finish();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {

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

    public void fab(View view)
    {
        if(burger == false)
        {
            searchBar.setVisibility(View.INVISIBLE);
            burgerMenu.setVisibility(View.VISIBLE);
            burgerButton.setImageResource(R.drawable.ic_cross);
            burger = true;
        }
        else
        {
            burgerMenu.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            burgerButton.setImageResource(R.drawable.ic_burger);
            burger = false;
        }
    }

}
