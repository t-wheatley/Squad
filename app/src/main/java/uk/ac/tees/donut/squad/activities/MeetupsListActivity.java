package uk.ac.tees.donut.squad.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;

public class MeetupsListActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    private DatabaseReference mDatabase;
    private FirebaseStorage firebaseStorage;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    String userId;
    String squadId;
    String placeId;
    boolean host;
    boolean member;
    boolean squad;
    boolean place;

    LinearLayout burgerMenu;
    FloatingActionButton burgerButton;
    //for whenever burger menu is open or not
    boolean burger = false;

    boolean past = false;
    Button pastButton;

    RelativeLayout loadingOverlay;
    TextView loadingText;
    TextView listText;
    Button btnDistance;
    Button btnStartTime;
    EditText searchBar;

    List<Meetup> searchList;
    List<Meetup> filteredList;
    List<Meetup> filteredListExpired;
    MeetupAdapter filteredAdapter;
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

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Meetups...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        burgerMenu = (LinearLayout) findViewById(R.id.meetupsList_burgerMenu);
        burgerButton = (FloatingActionButton) findViewById(R.id.meetupsList_fab);

        pastButton = (Button) findViewById(R.id.meetupsList_showPast);

        // Initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.meetupsList_recyclerView);
        listText = (TextView) findViewById(R.id.meetupsList_textView);
        btnDistance = (Button) findViewById(R.id.meetupsList_btnDistance);
        btnDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterDistance();
            }
        });
        btnStartTime = (Button) findViewById(R.id.meetupsList_btnStartTime);
        btnStartTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                filterStart();
            }
        });
        searchBar = (EditText) findViewById(R.id.meetupsList_searchBar);
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

        member = false;
        host = false;
        squad = false;
        place = false;
        search = false;
        past = true;
        filter = 0;

        searchList = new ArrayList<Meetup>();
        filteredList = new ArrayList<Meetup>();

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
            } else if(b.get("placeId") != null)
            {
                // Place mode
                placeId = (String) b.get("placeId");
                place = true;
            }
        }

        // Base database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

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
        } else if(place)
        {
            getPlace(placeId);
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
    int getContentViewId() {
        return R.layout.activity_meetups_list;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.menu_meetups;
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
        if(burger == true)
        {
            fab(burgerButton);
        } else
        {
            MeetupsListActivity.this.finish();
        }
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
        // Database reference to get all Meetups
        Query allQuery = mDatabase.child("meetups");

        // Check to see if any Meetups exist
        checkForEmpty(allQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_list_card,
                MeetupViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                model.updateStatus();
                populateMeetupViewHolder(viewHolder, model, position);
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

    public void getUsers(String userId)
    {
        // Database reference to get a User's Meetups
        Query userQuery = mDatabase.child("meetups").orderByChild("users/" + userId).equalTo(true);

        // Check to see if any Meetups exist
        checkForEmpty(userQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_list_card,
                MeetupViewHolder.class,
                userQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                model.updateStatus();
                populateMeetupViewHolder(viewHolder, model, position);
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

    public void getHosted(String userId)
    {
        // Database reference to get a Host's Meetups
        Query hostQuery = mDatabase.child("meetups").orderByChild("host").equalTo(userId);

        // Check to see if any Meetups exist
        checkForEmpty(hostQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_list_card,
                MeetupViewHolder.class,
                hostQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                model.updateStatus();
                populateMeetupViewHolder(viewHolder, model, position);
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
        // Database reference to get a Squad's Meetups
        Query squadQuery = mDatabase.child("meetups").orderByChild("squad").equalTo(squadId);

        // Check to see if any Meetups exist
        checkForEmpty(squadQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_list_card,
                MeetupViewHolder.class,
                squadQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                model.updateStatus();
                populateMeetupViewHolder(viewHolder, model, position);
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

    public void getPlace(String placeId)
    {
        // Database reference to get all Meetups
        Query allQuery = mDatabase.child("meetups").orderByChild("place").equalTo(placeId);

        // Check to see if any Meetups exist
        checkForEmpty(allQuery, null);

        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_list_card,
                MeetupViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
            {
                model.updateStatus();
                populateMeetupViewHolder(viewHolder, model, position);
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

    // Checks if Meetups in the selected query exist
    public void checkForEmpty(Query query, MeetupAdapter adapter)
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

                    // Checks if Meetups will be found
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

            // Checks if Meetups exist
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
    public void adapterObserver(final FirebaseRecyclerAdapter fbAdapter, final MeetupAdapter meetupAdapter)
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
        } else if (meetupAdapter != null)
        {
            mObserver = new RecyclerView.AdapterDataObserver()
            {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount)
                {
                    if (meetupAdapter.getItemCount() == 0)
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
                    if (meetupAdapter.getItemCount() == 0)
                    {
                        listText.setVisibility(View.VISIBLE);
                    } else
                    {
                        listText.setVisibility(View.GONE);
                    }
                }
            };

            meetupAdapter.registerAdapterDataObserver(mObserver);
        } else
        {
            // something wrong
        }
    }

    public void populateMeetupViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position)
    {
        // Displaying the name
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

        // Displaying the number of attendees
        if (model.getUsers() != null)
        {
            int attendees = model.getUsers().size();

            if (attendees > 1)
            {
                viewHolder.attendingField.setText(attendees + " attendees");
            } else
            {
                viewHolder.attendingField.setText("1 attendee");
            }
        } else
        {
            viewHolder.attendingField.setText("0 attendees");
        }

        // Getting status
        model.updateStatus();
        int status = model.gimmeStatus();
        if (status == 0)
            viewHolder.statusField.setText("Upcoming");
        else if (status == 1)
            viewHolder.statusField.setText("Ongoing");
        else if (status == 2)
            viewHolder.statusField.setText("Expired");
        else
            viewHolder.statusField.setText("Deleted");


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

        // Diplaying the picture
        StorageReference meetupStorage = firebaseStorage.getReference().child("meetups/" + model.getId() + ".jpg");

        // If the meetup has an image display it
        meetupStorage.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap meetupImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.image.setImageBitmap(meetupImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                viewHolder.image.setVisibility(View.GONE);
            }
        });

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
                    filteredList.add((Meetup) mAdapter.getItem(i));
                }
            }

            searchText = searchText.toLowerCase();

            for (Meetup meetup : filteredList)
            {
                if (meetup.getName().toLowerCase().contains(searchText))
                {
                    searchList.add(meetup);
                }
            }

            MeetupAdapter searchAdapter = new MeetupAdapter(searchList);

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
        } else if (filter == 2)
        {
            // Start date
            filterStart();
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

            // List sorted by distance
            filteredList = new ArrayList<>();


            // Filling the list with data retrieved from Firebase
            for (int i = 0; i < mAdapter.getItemCount(); i++)
            {
                filteredList.add((Meetup) mAdapter.getItem(i));
            }

            // Sorting the list in order of distance
            Collections.sort(filteredList, new Comparator<Meetup>()
            {
                public int compare(Meetup m1, Meetup m2)
                {
                    Location meetupLoc1 = new Location("meetup1");
                    meetupLoc1.setLatitude(m1.getLatitude());
                    meetupLoc1.setLongitude(m1.getLongitude());

                    Location meetupLoc2 = new Location("meetup2");
                    meetupLoc2.setLatitude(m2.getLatitude());
                    meetupLoc2.setLongitude(m2.getLongitude());

                    double distance1 = userLoc.distanceTo(meetupLoc1);
                    double distance2 = userLoc.distanceTo(meetupLoc2);

                    if (distance1 < distance2) return -1;
                    if (distance1 > distance2) return 1;
                    return 0;

                }
            });

            // List sorted by distance with expired meetups
            filteredListExpired = new ArrayList<>(filteredList);

            // Removing expired from filtered list
            for (Iterator<Meetup> iterator = filteredList.iterator(); iterator.hasNext(); )
            {
                Meetup meetup = iterator.next();
                if (meetup.gimmeStatus() == 2)
                {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                }
            }

            // If user wants expired
            if (past == true)
            {
                // Display expired
                filteredAdapter = new MeetupAdapter(filteredListExpired);
            } else
            {
                // Hide expired
                filteredAdapter = new MeetupAdapter(filteredList);
            }

            checkForEmpty(null, filteredAdapter);

            // If there is a search term entered
            if (search == true)
            {
                // call search()
                search(searchBar.getText().toString());
            } else
            {
                // display the adapter
                mRecyclerView.setAdapter(filteredAdapter);
            }

        } else
        {
            new AlertDialog.Builder(MeetupsListActivity.this)
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

    public void filterStart()
    {
        // Displaying the loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("Filtering...");

        filter = 2;

        // List sorted by start time
        filteredList = new ArrayList<>();

        // Filling the list with data retrieved from Firebase
        for (int i = 0; i < mAdapter.getItemCount(); i++)
        {
            filteredList.add((Meetup) mAdapter.getItem(i));
        }

        // Sorting the list in order of start time
        Collections.sort(filteredList, new Comparator<Meetup>()
        {
            public int compare(Meetup m1, Meetup m2)
            {
                if (m1.getStartDateTime() < m2.getStartDateTime()) return -1;
                if (m1.getStartDateTime() > m2.getStartDateTime()) return 1;
                return 0;
            }
        });

        // List sorted by start time with expired meetups
        filteredListExpired = new ArrayList<>(filteredList);

        // Removing expired from filtered list
        for (Iterator<Meetup> iterator = filteredList.iterator(); iterator.hasNext(); )
        {
            Meetup meetup = iterator.next();
            if (meetup.gimmeStatus() == 2)
            {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }

        // If user wants expired
        if (past == true)
        {
            // Display expired
            filteredAdapter = new MeetupAdapter(filteredListExpired);
        } else
        {
            // Hide expired
            filteredAdapter = new MeetupAdapter(filteredList);
        }

        checkForEmpty(null, filteredAdapter);

        // If there is a search term entered
        if (search == true)
        {
            // call search()
            search(searchBar.getText().toString());
        } else
        {
            // display the adapter
            mRecyclerView.setAdapter(filteredAdapter);
        }
    }

    public void getNewLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MeetupsListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
            ActivityCompat.requestPermissions(MeetupsListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else
        {
            getNewLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getNewLocation();
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

    public static class MeetupViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView image;
        TextView nameField;
        TextView descriptionfield;
        TextView squadField;
        TextView attendingField;
        TextView statusField;
        RelativeLayout layout;


        public MeetupViewHolder(View v)
        {
            super(v);
            mView = v;
            image = (ImageView) v.findViewById(R.id.listCard_image);
            nameField = (TextView) v.findViewById(R.id.listCard_text1);
            descriptionfield = (TextView) v.findViewById(R.id.listCard_text3);
            squadField = (TextView) v.findViewById(R.id.listCard_text2);
            attendingField = (TextView) v.findViewById(R.id.listCard_text4);
            statusField = (TextView) v.findViewById(R.id.listCard_text5);
            layout = (RelativeLayout) v.findViewById(R.id.layout);

            //squadField.setText("loading...");
        }
    }

    public class MeetupAdapter extends RecyclerView.Adapter<MeetupViewHolder>
    {

        private List<Meetup> meetupList;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        public MeetupAdapter(List<Meetup> meetupList)
        {
            this.meetupList = meetupList;
        }

        @Override
        public MeetupViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_list_card, parent, false);

            return new MeetupViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MeetupViewHolder holder, int position)
        {
            final Meetup meetup = meetupList.get(position);
            // Getting name
            holder.nameField.setText(meetup.getName());

            // Getting description
            String description = meetup.getDescription().replace("\n", "");
            String elipsis = "";
            if (description.length() > 54)
                elipsis = "...";

            final String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

            holder.descriptionfield.setText(shortDesc);

            // Get Squad name from id
            mDatabase.child("squads").child(meetup.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
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

            // Displaying the number of attendees
            if (meetup.getUsers() != null)
            {
                int attendees = meetup.getUsers().size();

                if (attendees > 1)
                {
                    holder.attendingField.setText(attendees + " attendees");
                } else
                {
                    holder.attendingField.setText("1 attendee");
                }
            } else
            {
                holder.attendingField.setText("0 attendees");
            }

            // Getting status
            meetup.updateStatus();
            int status = meetup.gimmeStatus();
            if (status == 0)
                holder.statusField.setText("Upcoming");
            else if (status == 1)
                holder.statusField.setText("Ongoing");
            else if (status == 2)
                holder.statusField.setText("Expired");
            else
                holder.statusField.setText("Deleted");


            // OnClick
            holder.mView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Stores the current item's key in a string
                    String mId = meetup.getId();

                    //Sends the id to the details activity
                    Intent detail = new Intent(MeetupsListActivity.this, MeetupDetailActivity.class);
                    detail.putExtra("meetupId", mId);
                    startActivity(detail);
                }
            });

            // Diplaying the picture
            StorageReference meetupStorage = firebaseStorage.getReference().child("meetups/" + meetup.getId() + ".jpg");

            // If the meetup has an image display it
            meetupStorage.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap meetupImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.image.setVisibility(View.VISIBLE);
                    holder.image.setImageBitmap(meetupImage);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    holder.image.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return meetupList.size();
        }
    }

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

    public void createNew(View view)
    {
        Intent intent = new Intent(this, NewMeetupActivity.class);
        startActivity(intent);
    }

    public void showPast(View view)
    {
        if (filter == 0)
        {
            if(past == true)
            {
                // Hide expired
                past = false;
                pastButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tick, 0);

                filteredList = new ArrayList<>();

                // Filling the list with data retrieved from Firebase
                for (int i = 0; i < mAdapter.getItemCount(); i++)
                {
                    filteredList.add((Meetup) mAdapter.getItem(i));
                }

                // Removing expired from filtered list
                for (Iterator<Meetup> iterator = filteredList.iterator(); iterator.hasNext(); )
                {
                    Meetup meetup = iterator.next();
                    if (meetup.gimmeStatus() == 2)
                    {
                        // Remove the current element from the iterator and the list.
                        iterator.remove();
                    }
                }

                filteredAdapter = new MeetupAdapter(filteredList);
                mRecyclerView.setAdapter(filteredAdapter);
            } else
            {
                past = true;
                pastButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cross, 0);

                mRecyclerView.setAdapter(mAdapter);
            }
        } else
        {
            if (past == false)
            {
                // Display expired
                past = true;
                pastButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tick, 0);

                filteredAdapter = new MeetupAdapter(filteredListExpired);
                mRecyclerView.setAdapter(filteredAdapter);

            } else
            {
                // Hide expired
                past = false;
                pastButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cross, 0);

                filteredAdapter = new MeetupAdapter(filteredList);
                mRecyclerView.setAdapter(filteredAdapter);
            }
        }
    }
}


