package uk.ac.tees.donut.squad.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;

/**
 * Created by Anthony Ward
 */

public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, DirectionCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener
{

    //GOOGLE MAP API V2

    private DatabaseReference mDatabase;
    private ChildEventListener mChildEventListener;

    LinearLayout burgerMenu;
    FloatingActionButton burgerButton;
    //for whenever burger menu is open or not
    boolean burger = false;

    Calendar currentDateTime;
    int filter;

    private SupportMapFragment mapFrag;
    private Button btnRequest;
    private Button btnOngoing;
    private Button btnAll;
    private Button btnUpcoming;
    private Button btnExpired;
    private Button btnClear;
    private Button btnShowMeetup;
    private TextView showDistance;
    private TextView showDuration;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mLastLocation;
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private LinearLayout detailsBar;

    //Google directions API key
    private String directionAPIKey = "AIzaSyBPSyzwv_Lr4JyCgKRswRhBRebSi8htqt8";
    private LatLng currentLocation;
    private LatLng destination;
    private String meetupID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get the current Date and Time
        currentDateTime = Calendar.getInstance();

        //Create database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Set's map base filter to show all meetups
        filter = 1;

        //Create UI elements
        showDistance = (TextView) findViewById(R.id.textDistance);
        showDistance.setVisibility(View.GONE);

        showDuration = (TextView) findViewById(R.id.textDuration);
        showDuration.setVisibility(View.GONE);

        burgerMenu = (LinearLayout) findViewById(R.id.map_burgerMenu);
        burgerButton = (FloatingActionButton) findViewById(R.id.map_fab);

        btnClear = (Button) findViewById(R.id.btn_Clear);
        btnClear.setOnClickListener(this);
        btnClear.setVisibility(View.GONE);
        btnRequest = (Button) findViewById(R.id.btn_request_direction);
        btnRequest.setOnClickListener(this);
        btnRequest.setVisibility(View.GONE);

        detailsBar = (LinearLayout) findViewById(R.id.detailsBar);

        btnShowMeetup = (Button) findViewById(R.id.btn_show_meetup);
        btnShowMeetup.setOnClickListener(this);
        btnShowMeetup.setVisibility(View.GONE);

        btnAll = (Button) findViewById(R.id.btn_filter_all);
        btnAll.setOnClickListener(this);

        btnUpcoming = (Button) findViewById(R.id.btn_filter_upcoming);
        btnUpcoming.setOnClickListener(this);

        btnExpired = (Button) findViewById(R.id.btn_filter_expired);
        btnExpired.setOnClickListener(this);

        btnOngoing = (Button) findViewById(R.id.btn_filter_ongoing);
        btnOngoing.setOnClickListener(this);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }


    /**
     * Called when map is ready
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else
            {
                //Request Location Permission
                checkLocationPermission();
            }
        } else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        addMarkers(mMap);


    }

    /**
     * On click listener method for UI elements
     *
     * @param v
     */
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.btn_request_direction)
        {
            requestDirection();
            btnRequest.setVisibility(View.GONE);
        }
        if (id == R.id.btn_filter_ongoing)
        {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().title("Current Position").position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            filter = 2;
            addMarkers(mMap);
        }
        if (id == R.id.btn_filter_all)
        {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().title("Current Position").position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            filter = 1;
            addMarkers(mMap);
        }
        if (id == R.id.btn_filter_upcoming)
        {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().title("Current Position").position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            filter = 3;
            addMarkers(mMap);
        }
        if (id == R.id.btn_filter_expired)
        {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().title("Current Position").position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            filter = 4;
            addMarkers(mMap);
        }
        if (id == R.id.btn_show_meetup)
        {
            Intent detail = new Intent(MapActivity.this, MeetupDetailActivity.class);
            detail.putExtra("meetupId", meetupID);
            startActivity(detail);
        }
        if (id == R.id.btn_Clear)
        {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().title("Current Position").position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            addMarkers(mMap);
            btnShowMeetup.setVisibility(View.GONE);
            btnRequest.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
            showDuration.setVisibility(View.GONE);
            showDistance.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        }
    }

    /**
     * Method called when back button is pressed, if burger menu is active closes the menu else ends activity
     */
    @Override
    public void onBackPressed()
    {
        if (burger == true)
        {
            fab(burgerButton);
        } else
        {
            MapActivity.this.finish();
        }
    }

    /**
     * Method called when burger button is pressed, controls whether butger menu is displayed or not
     */
    public void fab(View view)
    {
        if (burger == false)
        {
            burgerMenu.setVisibility(View.VISIBLE);
            burgerButton.setImageResource(R.drawable.ic_cross);
            burger = true;
        } else
        {
            burgerMenu.setVisibility(View.GONE);
            burgerButton.setImageResource(R.drawable.ic_burger);
            burger = false;
        }
    }

    /**
     * Method creates request for directions from currentLocation to destination
     */
    public void requestDirection()
    {
        //Checks to ensure users current location and desired destination are set
        if (currentLocation == null || destination == null)
        {
            Toast.makeText(this, "Invalid Destination", Toast.LENGTH_LONG).show();
        } else
        {
            //Generate Directions
            GoogleDirection.withServerKey(directionAPIKey)
                    .from(currentLocation)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .unit(Unit.IMPERIAL)
                    .execute(this);
        }
    }

    /**
     * Method to add markers at meetup locations
     *
     * @param map Google map
     */
    public void addMarkers(final GoogleMap map)
    {
        //Create child listener for meetups within firebase
        mChildEventListener = mDatabase.child("meetups").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                //Create meetup object for each meetup with in firebase
                Meetup meetup = dataSnapshot.getValue(Meetup.class);
                double lat = meetup.getLatitude();
                double lng = meetup.getLongitude();
                String name = meetup.getName();
                String description = meetup.getDescription();
                long strtDateTime = meetup.getStartDateTime();
                long endDateTime = meetup.getEndDateTime();
                LatLng location = new LatLng(lat, lng);

                //If statements checks what filter is active before adding markers
                if (filter == 2)
                {
                    if (currentDateTime.getTimeInMillis() / 1000L > strtDateTime && currentDateTime.getTimeInMillis() / 1000L < endDateTime)
                    {
                        Marker marker = map.addMarker(new MarkerOptions().position(location).title(name).snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        marker.setTag(meetup);
                    }
                } else if (filter == 3)
                {
                    if (currentDateTime.getTimeInMillis() / 1000L < strtDateTime)
                    {
                        Marker marker = map.addMarker(new MarkerOptions().position(location).title(name).snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        marker.setTag(meetup);
                    }
                } else if (filter == 4)
                {
                    if (currentDateTime.getTimeInMillis() / 1000L > endDateTime)
                    {
                        Marker marker = map.addMarker(new MarkerOptions().position(location).title(name).snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        marker.setTag(meetup);
                    }
                } else
                {
                    Marker marker = map.addMarker(new MarkerOptions().position(location).title(name).snippet(description));
                    marker.setTag(meetup);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }


    /**
     * Method called when activity is not active
     */
    @Override
    public void onPause()
    {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * Mehtod gets content view ID
     *
     * @return xml file related to activity
     */
    @Override
    int getContentViewId()
    {
        return R.layout.activity_map;
    }

    /**
     * method gets navigation bat
     *
     * @return navigation bar
     */
    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_map;
    }


    /**
     * Mehtod to build the API client for google location services
     */
    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    /**
     * Method called if unable to connect to google API client
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }


    /**
     * Mehtod calls get location when connected to google API client
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        getLocation();

    }


    /**
     * Method gets users current location
     */
    public void getLocation()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //Checks if app has permission to access users location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * method called when connection to google API client is suspended
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i)
    {
    }

    /**
     * Method called when users location is changed, adds a marker to the users current position
     *
     * @param location users location
     */
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;

        //Removes any pre-existing markers
        if (mCurrLocationMarker != null)
        {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocation = latLng;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /**
     * Method called to check the current location permissions
     */
    private void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {

              //Dialog informs the user of no location permission and asks for permission
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else
            {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    /**
     * Mehtod called when requesting permission to use location
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                    {

                        if (mGoogleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else
                {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }


    /**
     * Mehtod called if direction result is successful, creates a polyline between current location and destination
     *
     * @param direction direction details passed in from getDirections
     * @param rawBody
     */
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        // Check that direction is ok
        if (direction.isOK())
        {
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);

            //Get information about leg for directions
            Info distanceInfo = leg.getDistance();
            Info durationInfo = leg.getDuration();
            String distance = distanceInfo.getText();
            String duration = durationInfo.getText();

            //Set text boxes to show direction distance and driving time
            showDistance.setText("Distance: " + distance);
            showDuration.setText("Driving time: " + duration);

            //Make text boxes visible
            showDistance.setVisibility(View.VISIBLE);
            showDuration.setVisibility(View.VISIBLE);

            btnClear.setVisibility(View.VISIBLE);

            //Clear map of previous polylines
            mMap.clear();
            //add marker to current position
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Postion").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            //Add meetup markers again
            addMarkers(mMap);
            //Move camera to destination
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 14));
            //Draw polyline
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
            //set request button to invisible
            btnRequest.setVisibility(View.GONE);
        }
    }

    /**
     * Method called when getting direction fails
     *
     * @param t
     */
    @Override
    public void onDirectionFailure(Throwable t)
    {

    }

    /**
     * Method called when map marker is selected by user
     *
     * @param marker marker selected by user
     * @return ruturn false when complete
     */
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        //Checks if the current marker selected is current location
        if (marker.getPosition().equals(currentLocation))
        {
            return false;
        } else
        {
            //Get meetup object passed in from marker
            Meetup meetup = (Meetup) marker.getTag();
            meetupID = meetup.getId();
            detailsBar.setVisibility(View.VISIBLE);

            //Set interactive buttons to visible
            btnShowMeetup.setVisibility(View.VISIBLE);
            btnRequest.setVisibility(View.VISIBLE);
            //Set direction desrination to current markers position
            destination = marker.getPosition();
            //If filter is set to one display meesage showing if meetup has expired or not
            if (filter == 1)
            {
                if (meetup.getEndDateTime() < currentDateTime.getTimeInMillis() / 1000L)
                {
                    Toast.makeText(this, "Expired", Toast.LENGTH_LONG).show();
                    return false;
                } else if (meetup.getStartDateTime() > currentDateTime.getTimeInMillis() / 1000L)
                {
                    Toast.makeText(this, "Upcoming", Toast.LENGTH_LONG).show();
                    return false;
                } else
                {
                    Toast.makeText(this, "Ongoing", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else
            {
                return false;
            }
        }


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Checks if the current marker selected is current location
        if (marker.getPosition().equals(currentLocation))
        {
            return;
        } else{
            Intent detail = new Intent(MapActivity.this, MeetupDetailActivity.class);
            detail.putExtra("meetupId", meetupID);
            startActivity(detail);
        }
    }
}
