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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

import java.util.ArrayList;

import uk.ac.tees.donut.squad.R;

/**
 * Created by Anthony Ward
 */

public class PlaceMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, DirectionCallback {

    //GOOGLE MAP API V2

    private SupportMapFragment mapFrag;
    private TextView showDistance;
    private TextView showDuration;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mLastLocation;
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    //Google Direction API key
    private String directionAPIKey = "AIzaSyBPSyzwv_Lr4JyCgKRswRhBRebSi8htqt8";
    private LatLng currentLocation;
    private LatLng destination;
    private double longitude;
    private double latitude;
    private String placeName;
    private String placeDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_maps);

        showDistance = (TextView) findViewById(R.id.textDistance);
        showDistance.setVisibility(View.INVISIBLE);

        showDuration = (TextView) findViewById(R.id.textDuration);
        showDuration.setVisibility(View.INVISIBLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);




    }

    /**
     * Mehtod sets the target destination for navigating
     *
     * @param lat desired latitude
     * @param lng desired longitude
     */
    public void setDestination(double lat, double lng){
        destination = new LatLng(lat, lng);
    }


    /**
     * Method called when map is ready
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //gets extras passd from last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        //Sets longitude, latitude name and details from previous activity
        if(b != null)
        {
            longitude = (Double) b.get("longitude");
            latitude = (Double)  b.get("latitude");
            placeName = (String) b.get("placeName");
            placeDetails = (String) b.get("placeDescription");

            this.setTitle("Place Details");
        }
        //Create error dialog if unable to get place
        else
        {
            new AlertDialog.Builder(PlaceMapsActivity.this)
                    .setTitle("Error")
                    .setMessage("The place went missing somewhere, please try again")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        //Set target destination
        setDestination(latitude, longitude);

        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //Add marker to destination
        mMap.addMarker(new MarkerOptions().position(destination).snippet(placeDetails).title(placeName));

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,13));



    }


    /**
     * on click listener method
     *
     * @param v
     */
    public void onClick(View v){
        int id = v.getId();
    }


    /**
     * Method called when activity is not active
     */
    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    /**
     * Mehtod to build the API client for google location services
     */
    protected synchronized void buildGoogleApiClient() {
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Mehtod calls get location when connected to google API client
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();

    }

    /**
     * Method gets users current location
     */
    public void getLocation(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * method called when connection to google API client is suspended
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * Method called when users location is changed, adds a marker to the users current position
     *
     * @param location users location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
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

        //Set direction perameters's once user's location is set
        if (currentLocation == null || destination == null){
            Toast.makeText(this, "Invalid Destination", Toast.LENGTH_LONG).show();
        }
        else {
            //Generate Directions
            GoogleDirection.withServerKey(directionAPIKey)
                    .from(currentLocation)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .unit(Unit.IMPERIAL)
                    .execute(this);
        }



        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /**
     * Method called to check the current location permissions
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Dialog informs the user of no location permission and asks for permission
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(PlaceMapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
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
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

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
    public void onDirectionSuccess(Direction direction, String rawBody) {
        // Check that direction is ok
        if (direction.isOK()) {
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();

            //Get information about leg for directions
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);

            Info distanceInfo = leg.getDistance();
            Info durationInfo = leg.getDuration();

            String distance = distanceInfo.getText();
            String duration = durationInfo.getText();

            //Set text boxes to show direction distance and driving time
            showDistance.setText("Distance: " + distance);
            showDuration.setText("Driving time: " + duration);

            showDistance.setVisibility(View.VISIBLE);
            showDuration.setVisibility(View.VISIBLE);
            //Draw polyline
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));

        }
    }

    /**
     * Method called when getting direction fails
     *
     * @param t
     */
    @Override
    public void onDirectionFailure(Throwable t) {

    }
}
