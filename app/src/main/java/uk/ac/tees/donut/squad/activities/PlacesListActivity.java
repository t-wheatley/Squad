package uk.ac.tees.donut.squad.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.LocPlace;


/**
 * Activity which allows the user to view a list of Places.
 */
public class PlacesListActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    // Firebase
    private DatabaseReference mDatabase;

    // Location
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location userLoc;

    // Loading Overlay
    private RelativeLayout loadingOverlay;
    private TextView loadingText;

    // Activity UI
    private RelativeLayout searchMenu;
    private TextView listText;
    private Button btnDistance;
    private EditText searchBar;

    // RecyclerView
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    // BurgerMenu
    private LinearLayout burgerMenu;
    private FloatingActionButton burgerButton;

    // Variables
    private List<LocPlace> searchList;
    private List<LocPlace> filteredList;
    private PlaceAdapter filteredAdapter;
    private String squadId;
    private int filter;
    private int loadingCount;
    private boolean squad;
    private boolean search;
    private boolean burger = false;

    // Final values
    final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("PlacesListActivity: oncreate method");

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Places...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Initialising UI Elements
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        listText = (TextView) findViewById(R.id.placesList_textView);
        btnDistance = (Button) findViewById(R.id.placesList_btnDistance);
        searchMenu = (RelativeLayout) findViewById(R.id.placesList_searchLayout);
        burgerMenu = (LinearLayout) findViewById(R.id.placesList_burgerMenu);
        burgerButton = (FloatingActionButton) findViewById(R.id.placesList_fab);
        btnDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterDistance();
            }
        });
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

        // Initialising variables
        squad = false;
        filter = 0;

        searchList = new ArrayList<LocPlace>();
        filteredList = new ArrayList<LocPlace>();

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            // Collects the userId passed from the RecyclerView
            squadId = (String) b.get("squadId");
            squad = true;
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

        // Connecting to the GoogleApiClient
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // Initialising the LocationRequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(102);
        mLocationRequest.setInterval(5);

        // Displaying the mAdapter in the recyclerView
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart()
    {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    int getContentViewId()
    {
        return R.layout.activity_places_list;
    }

    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_places;
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
    public void onBackPressed()
    {
        // If burger menu is open
        if (burger == true)
        {
            // Close the burger menu
            fab(burgerButton);
        } else
        {
            // Close the activity
            PlacesListActivity.this.finish();
        }
    }

    /**
     * Method to build a GoogleApiClient if one doesn't already exist.
     */
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

    /**
     * Method to get all of the Places.
     */
    public void getAll()
    {
        // Database reference to get a Squad's Places
        Query allQuery = mDatabase.child("places");

        // Check to see if any Places exist
        checkForEmpty(allQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<LocPlace, PlacesListActivity.PlaceViewHolder>(
                LocPlace.class,
                R.layout.item_list_card,
                PlacesListActivity.PlaceViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                // Populates a viewHolder with the found Place
                populatePlaceViewHolder(viewHolder, model, position);
            }

            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex)
            {
                // When the data is changed call updateFilter()
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

    /**
     * Method to get a Squad's Places.
     */
    public void getSquad(String squadId)
    {
        // Database reference to get a Squad's Places
        Query squadQuery = mDatabase.child("places").orderByChild("squad").equalTo(squadId);

        // Check to see if any Places exist
        checkForEmpty(squadQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<LocPlace, PlacesListActivity.PlaceViewHolder>(
                LocPlace.class,
                R.layout.item_list_card,
                PlacesListActivity.PlaceViewHolder.class,
                squadQuery
        )
        {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
            {
                listText.setVisibility(View.GONE);
                // Populates a viewHolder with the found Place
                populatePlaceViewHolder(viewHolder, model, position);
            }

            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex)
            {
                // When the data is changed call updateFilter()
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

    /**
     * Checks if Places exist, either in a query or an adapter.
     *
     * @param query   The query to check.
     * @param adapter The adapter to check.
     */
    public void checkForEmpty(Query query, PlaceAdapter adapter)
    {
        // If checking a query
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
        } else // If checking an adapter
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

    /**
     * Method to add an observer on the RecyclerView to check if empty on data changes.
     *
     * @param fbAdapter    The FireBaseRecyclerAdapter to be observed.
     * @param placeAdapter The PlaceAdapter to be observed.
     */
    public void adapterObserver(final FirebaseRecyclerAdapter fbAdapter, final PlaceAdapter placeAdapter)
    {
        // If using FirebaseRecyclerAdapter
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

            // Registering the observer
            fbAdapter.registerAdapterDataObserver(mObserver);
        } else if (placeAdapter != null) // If using PlaceAdapter
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

            // Registering the observer
            placeAdapter.registerAdapterDataObserver(mObserver);
        }
    }

    /**
     * Method that populates the viewHolder with the info it needs.
     *
     * @param viewHolder The ViewHolder to be populated.
     * @param model      The Place to be displayed.
     * @param position   The position of the ViewHolder.
     */
    public void populatePlaceViewHolder(final PlacesListActivity.PlaceViewHolder viewHolder, final LocPlace model, int position)
    {
        // Displaying the name
        viewHolder.nameField.setText(model.getName());

        // Displaying the description
        String description = model.getDescription().replace("\n", "");
        viewHolder.descriptionField.setText(description);

        // Number of Meetups at place
        if (model.getMeetups() == null)
        {
            viewHolder.meetupNo.setText("0 Meetups");
        } else
        {
            if (model.getMeetups().size() > 1)
            {
                viewHolder.meetupNo.setText(model.getMeetups().size() + " Meetups");
            } else
            {
                viewHolder.meetupNo.setText("1 Meetup");
            }
        }

        // Calculates the distance to the Place and displays it
        Location placeLocation = new Location("place");
        placeLocation.setLatitude(model.getLocLat());
        placeLocation.setLongitude(model.getLocLong());
        double distance = userLoc.distanceTo(placeLocation) / 1609.344;
        String miles = String.format("%.2f", distance);
        viewHolder.distance.setText(miles + " miles");


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

        // loading the picture
        if(model.getPictures() != null)
        {
            // Get the first picture's url
            HashMap<String, String> pictures = model.getPictures();
            String pictureUrl = pictures.values().toArray()[0].toString();

            // Displaying the needed UI
            viewHolder.imageLayout.setVisibility(View.VISIBLE);
            viewHolder.image.setVisibility(View.VISIBLE);
            viewHolder.imageLoading.setVisibility(View.VISIBLE);

            // Download and display using Glide
            Glide.with(viewHolder.itemView.getContext())
                    .load(pictureUrl)
                    .asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            // Displaying the needed UI
                            viewHolder.imageLayout.setVisibility(View.GONE);
                            viewHolder.image.setVisibility(View.GONE);
                            viewHolder.imageLoading.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            viewHolder.image.setImageDrawable(new BitmapDrawable(getResources(), resource));
                            viewHolder.imageLoading.setVisibility(View.GONE);
                            return true;
                        }
                    })
                    .into(viewHolder.image);
        }

        // onClick
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

    /**
     * Method that searches for a Meetup with a name that contains the searchText.
     *
     * @param searchText The String to be searched for.
     */
    public void search(String searchText)
    {
        // Search mode
        search = true;

        // Clearing the previous searchList
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

            // Creating an adapter using the searchList
            PlaceAdapter searchAdapter = new PlaceAdapter(searchList);

            // Checking for empty and adding an observer
            checkForEmpty(null, searchAdapter);

            // Displaying the new adapter
            mRecyclerView.setAdapter(searchAdapter);
        } else
        {
            search = false;

            if (filter == 0)
            {
                mRecyclerView.setAdapter(mAdapter);
            } else
            {
                mRecyclerView.setAdapter(filteredAdapter);
            }
        }
    }

    /**
     * Method to update a filteredList when the data has changed.
     */
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

    /**
     * Method that creates a filteredList based on the User's distance to the Places.
     */
    public void filterDistance()
    {
        if (mGoogleApiClient.isConnected())
        {
            // Get the User's latest location
            getNewLocation();
        }

        if (userLoc != null)
        {
            // Displaying the loading overlay
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingText.setText("Filtering...");

            // Filtered mode
            filter = 1;

            // List sorted by distance
            filteredList = new ArrayList<>();

            // Filling the list with data retrieved from Firebase
            for (int i = 0; i < mAdapter.getItemCount(); i++)
            {
                filteredList.add((LocPlace) mAdapter.getItem(i));
            }

            // Sorting the list in order of distance
            Collections.sort(filteredList, new Comparator<LocPlace>()
            {
                public int compare(LocPlace p1, LocPlace p2)
                {
                    if(p1.getLocLat() != 0)
                    {
                        if (p1.getLocLong() != 0)
                        {
                            if (p2.getLocLat() != 0)
                            {
                                if (p2.getLocLong() != 0)
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
                                }
                            }
                        }
                    }
                    return 0;
                }
            });

            // List sorted by distance with expired meetups
            filteredAdapter = new PlaceAdapter(filteredList);

            // Checking for empty and adding an observer
            checkForEmpty(null, filteredAdapter);

            // If there is a search term entered
            if (search = true)
            {
                // Call search()
                search(searchBar.getText().toString());
            } else
            {
                // Display the adapter
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

    /**
     * Method to get the User's latest location
     */
    public void getNewLocation()
    {
        // If no location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Request the permission
            ActivityCompat.requestPermissions(PlacesListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        // Store the User's latest location
        userLoc = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        // If no location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Request the permission
            ActivityCompat.requestPermissions(PlacesListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else
        {
            // Get the latest location
            getNewLocation();

            // If cant get a location
            if (userLoc == null)
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
            } else
            {
                // If came from 'View Places' button on Squad
                if (squad)
                {
                    // Get the Squad's Places.
                    getSquad(squadId);
                } else
                {
                    // Get all the Places.
                    getAll();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    /**
     * Method to handle the result of the permission request.
     *
     * @param requestCode  The request code passed to requestPermissions.
     * @param permissions  The requested permissions.
     * @param grantResults The permission granting results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            // If permission granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Get the latest location
                getNewLocation();

                if (userLoc == null)
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
                } else
                {
                    // If came from 'View Places' button on Squad
                    if (squad)
                    {
                        getSquad(squadId);
                    } else
                    {
                        getAll();
                    }
                }
            } else
            { // if permission is not granted
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

    /**
     * Static class to be filled by populateMeetupViewHolder.
     */
    public static class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView image;
        ProgressBar imageLoading;
        TextView nameField;
        TextView descriptionField;
        TextView squadField;
        ImageView icon;
        TextView meetupNo;
        TextView distance;
        LinearLayout imageLayout;

        public PlaceViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.listCard_text1);
            descriptionField = (TextView) v.findViewById(R.id.listCard_text3);
            squadField = (TextView) v.findViewById(R.id.listCard_text2);
            icon = (ImageView) v.findViewById(R.id.icon);
            image = (ImageView) v.findViewById(R.id.listCard_image);
            imageLoading = (ProgressBar) v.findViewById(R.id.listCard_imageProgress);
            meetupNo = (TextView) v.findViewById(R.id.listCard_text4);
            distance = (TextView) v.findViewById(R.id.listCard_text5);
            imageLayout = (LinearLayout) v.findViewById(R.id.listCard_imageLayout);

            icon.setImageResource(R.drawable.ic_meetup_icon);
        }
    }

    /**
     * Class used to display Places in a RecyclerView Adapter, used in filtering.
     */
    public class PlaceAdapter extends RecyclerView.Adapter<PlaceViewHolder>
    {

        private List<LocPlace> placeList;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        public PlaceAdapter(List<LocPlace> placeList)
        {
            this.placeList = placeList;
        }

        /**
         * Method to create a PlaceViewHolder using a layout as a template.
         *
         * @param parent   The ViewGroup the PlaceViewHolder belongs to
         * @param viewType The viewType to be used.
         * @return A PlaceViewHolder to be filled.
         */
        @Override
        public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_list_card, parent, false);

            return new PlaceViewHolder(itemView);
        }

        /**
         * Fills a PlaceViewHolder with the needed info.
         *
         * @param holder   The PlaceViewHolder to be filled.
         * @param position The position of the PlaceViewHolder.
         */
        @Override
        public void onBindViewHolder(final PlaceViewHolder holder, int position)
        {
            // Getting the Place
            final LocPlace place = placeList.get(position);

            // Getting name
            holder.nameField.setText(place.getName());

            // Getting description
            String description = place.getDescription().replace("\n", "");
            holder.descriptionField.setText(description);

            // Number of Meetups at place
            if (place.getMeetups() == null)
            {
                holder.meetupNo.setText("0 Meetups");
            } else
            {
                if (place.getMeetups().size() > 1)
                {
                    holder.meetupNo.setText(place.getMeetups().size() + " Meetups");
                } else
                {
                    holder.meetupNo.setText("1 Meetup");
                }
            }

            // Distance of place
            Location placeLocation = new Location("place");
            placeLocation.setLatitude(place.getLocLat());
            placeLocation.setLongitude(place.getLocLong());
            double distance = userLoc.distanceTo(placeLocation) / 1609.344;
            String miles = String.format("%.2f", distance);
            holder.distance.setText(miles + " miles");

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

            // loading the picture
            if(place.getPictures() != null)
            {
                // Get the first picture's url
                HashMap<String, String> pictures = place.getPictures();
                String pictureUrl = pictures.values().toArray()[0].toString();

                // Displaying the needed UI
                holder.imageLayout.setVisibility(View.VISIBLE);
                holder.image.setVisibility(View.VISIBLE);
                holder.imageLoading.setVisibility(View.VISIBLE);

                // Download and display using Glide
                Glide.with(holder.itemView.getContext())
                        .load(pictureUrl)
                        .asBitmap()
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                // Displaying the needed UI
                                holder.imageLayout.setVisibility(View.GONE);
                                holder.image.setVisibility(View.GONE);
                                holder.imageLoading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.image.setImageDrawable(new BitmapDrawable(getResources(), resource));
                                holder.imageLoading.setVisibility(View.GONE);
                                return true;
                            }
                        })
                        .into(holder.image);
            }

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

    /**
     * Method to send the User to the NewPlaceActivity.
     *
     * @param view The Button that was pressed.
     */
    public void createNew(View view)
    {
        Intent intent = new Intent(this, NewPlaceActivity.class);
        startActivity(intent);
    }

    /**
     * Method to open and close the burger menu when the FloatingActionButton is pressed.
     *
     * @param view The FloatingActionButton that was pressed.
     */
    public void fab(View view)
    {
        if (burger == false)
        {
            searchBar.setVisibility(View.INVISIBLE);
            burgerMenu.setVisibility(View.VISIBLE);
            burgerButton.setImageResource(R.drawable.ic_cross);
            burger = true;
        } else
        {
            burgerMenu.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            burgerButton.setImageResource(R.drawable.ic_burger);
            burger = false;
        }
    }

}
