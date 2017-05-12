package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.location.PlaceMapsActivity;
import uk.ac.tees.donut.squad.posts.AddressPlace;
import uk.ac.tees.donut.squad.posts.LocPlace;

public class PlaceDetailsActivity extends AppCompatActivity
{

    String placeId;

    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    LocPlace place;

    TextView placeName;
    TextView description;
    TextView noPic;
    TextView address;
    TextView squad;
    Button mapBtn;
    Button meetupsBtn;

    double latitude;
    double longitude;

    ImageSwitcher gallery;
    RelativeLayout galleryLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Place...");
        loadingOverlay.setVisibility(View.VISIBLE);

        //getting UI Elements
        placeName = (TextView) findViewById(R.id.placeDetails_placeName);
        description = (TextView) findViewById(R.id.placeDetails_description);
        noPic = (TextView) findViewById(R.id.noPic);
        address = (TextView) findViewById(R.id.placeDetails_address);
        squad = (TextView) findViewById(R.id.placeDetails_squadName);

        mapBtn = (Button) findViewById(R.id.mapButton);
        mapBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openMapLocation();
            }
        });

        meetupsBtn = (Button) findViewById(R.id.meetupsButton);
        meetupsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                viewMeetups();
            }
        });

        gallery = (ImageSwitcher) findViewById(R.id.placeDetails_gallery);

        //if there are no pictures
        boolean noPics = true; //TEMPORARY TILL WE CAN ATTEMPT AT LOADING PICS
        if (noPics)
        {
            //keeps the noPic text, and changes the height of the layout so it's not too big
            gallery.setVisibility(View.GONE);
        } else
        {
            //gets rid of the noPic text
            noPic.setVisibility(View.GONE);
            gallery.setVisibility(View.VISIBLE);
        }




        //gets extras passd from last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (b != null)
        {
            placeId = (String) b.get("placeId");
            this.setTitle("Place Details");
        } else
        {
            new AlertDialog.Builder(PlaceDetailsActivity.this)
                    .setTitle("Error")
                    .setMessage("The place went missing somewhere, please try again")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Starts the loading chain
        // loadMeetup -> loadSquad
        loadPlace();
    }

    public void loadPlace()
    {
        // Reads the data from the placeId in Firebase
        mDatabase.child("places").child(placeId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Place class
                place = dataSnapshot.getValue(LocPlace.class);

                // Displays the found place's attributes
                placeName.setText(place.getName());
                description.setText(place.getDescription());
                address.setText(place.fullAddress());
                longitude = place.getLocLong();
                latitude = place.getLocLat();


                loadSquad();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void loadSquad()
    {
        // Setting the loading text
        loadingText.setText("Getting the Place's Squad...");


        // Get Squad name from id
        mDatabase.child("squads").child(place.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                squad.setText(dataSnapshot.child("name").getValue(String.class));

                // Hiding loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void openMapLocation()
    {

        Intent newDetail = new Intent(PlaceDetailsActivity.this, PlaceMapsActivity.class);
        newDetail.putExtra("latitude", latitude);
        newDetail.putExtra("longitude", longitude);
        newDetail.putExtra("placeName",placeName.getText().toString());
        newDetail.putExtra("placeDescription", description.getText().toString());
        startActivity(newDetail);
    }

    public void viewSquad(View view)
    {
        //Sends the id to the details activity
        Intent detail = new Intent(PlaceDetailsActivity.this, SquadDetailActivity.class);
        detail.putExtra("squadId", place.getSquad());
        startActivity(detail);
    }

    public void viewMeetups()
    {
        // Loads the MeetupsList activity displaying the Meetups that are at this place
        Intent intent = new Intent(this, MeetupsListActivity.class);
        intent.putExtra("placeId", placeId);
        startActivity(intent);
    }
}
