package uk.ac.tees.donut.squad.location;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Created by Anthony Ward
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, DirectionCallback, GoogleMap.OnMarkerClickListener
{

    //GOOGLE MAP API V2

    private DatabaseReference mDatabase;
    private ChildEventListener mChildEventListener;

    Calendar currentDateTime;

    private SupportMapFragment mapFrag;
    private Button btnRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mLastLocation;
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    //Google directions API key
    private String directionAPIKey = "AIzaSyBPSyzwv_Lr4JyCgKRswRhBRebSi8htqt8";
    private LatLng currentLocation;
    private LatLng destination;
    private double longitude;
    private double latitude;

    private Meetup meetup;
    private Squad squad;

    private String userId;
    private String squadId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        currentDateTime = Calendar.getInstance();




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnRequest = (Button) findViewById(R.id.btn_request_direction);
        btnRequest.setOnClickListener(this);

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
         userId = (String) b.get("uId");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    public void setDestination(double lat, double lng)
    {
        destination = new LatLng(latitude, longitude);
    }

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

        mMap.setOnMarkerClickListener(this);

        addMarkers(mMap);



    }


    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.btn_request_direction)
        {
            requestDirection();
        }
    }

    public void requestDirection()
    {
        if (currentLocation == null || destination == null)
        {
            Toast.makeText(this, "Invalid Destination", Toast.LENGTH_LONG).show();
        }
        else {
            //Generate Directions
            GoogleDirection.withServerKey(directionAPIKey)
                    .from(currentLocation)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }
    public void addMarkers(final GoogleMap map)
    {

        Toast.makeText(this, ""+ currentDateTime, Toast.LENGTH_SHORT).show();


        mChildEventListener = mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Meetup meetup = dataSnapshot.getValue(Meetup.class);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                String startDate = sdf.format(meetup.getStartDateTime() * 1000L);
                String endDate = sdf.format(meetup.getEndDateTime() * 1000L);
                double lat = meetup.getLatitude();
                double lng = meetup.getLongitude();
                String name = meetup.getName();
                String description = meetup.getDescription();
                long strtDateTime = meetup.getStartDateTime();
                long endDateTime = meetup.getEndDateTime();
                LatLng location = new LatLng(lat,lng);
                Marker marker = map.addMarker(new MarkerOptions().position(location).title(name));
                marker.setTag(meetup);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


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


    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        getLocation();

    }

    public void getLocation()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
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

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        if (direction.isOK())
        {
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));

            btnRequest.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t)
    {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mCurrLocationMarker)){
            return false;
        }else {
            Meetup meetup = (Meetup) marker.getTag();
            if(meetup.getEndDateTime() < currentDateTime.getTimeInMillis() /1000L){
                Toast.makeText(this, "Expired", Toast.LENGTH_LONG).show();
                return  false;
            } else if(meetup.getStartDateTime() > currentDateTime.getTimeInMillis() /1000L){
                Toast.makeText(this, "Upcoming", Toast.LENGTH_LONG).show();
                return false;
            } else{
                Toast.makeText(this, "Ongoing", Toast.LENGTH_LONG).show();
                return false;
            }
        }


    }

}


//https://github.com/akexorcist/Android-GoogleDirectionLibrary