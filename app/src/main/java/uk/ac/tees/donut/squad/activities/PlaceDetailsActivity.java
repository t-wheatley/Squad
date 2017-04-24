package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import uk.ac.tees.donut.squad.posts.AddressPlace;

public class PlaceDetailsActivity extends AppCompatActivity {

    String placeId;

    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    AddressPlace place;

    TextView placeName;
    TextView description;
    TextView noPic;
    TextView address;
    TextView squad;
    Button mapBtn;
    Button directionsBtn;

    ImageSwitcher gallery;
    RelativeLayout galleryLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Place...");
        loadingOverlay.setVisibility(View.VISIBLE);

        //getting UI Elements
        placeName = (TextView) findViewById(R.id.placeNameText);
        description = (TextView) findViewById(R.id.descriptionText);
        noPic = (TextView) findViewById(R.id.noPic);
        address = (TextView) findViewById(R.id.address);
        squad = (TextView) findViewById(R.id.squadText);

        mapBtn = (Button) findViewById(R.id.mapButton);
        mapBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openMapLocation();
            }
        });

        directionsBtn = (Button) findViewById(R.id.directionsButton);
        directionsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openMapDirections();
            }
        });

        gallery = (ImageSwitcher) findViewById(R.id.placeGallery);
        galleryLayout = (RelativeLayout) findViewById(R.id.galleryLayout);

        //if there are no pictures
        boolean pics = false; //TEMPORARY TILL WE CAN ATTEMPT AT LOADING PICS
        if(pics)
        {
            //keeps the noPic text, and changes the height of the layout so it's not too big
            ViewGroup.LayoutParams params = galleryLayout.getLayoutParams();
            params.height = 35;
            galleryLayout.setLayoutParams(params);
        }
        else
            //gets rid of the noPic text
            noPic.setVisibility(View.GONE);


        //gets extras passd from last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(b != null)
        {
            placeId = (String) b.get("placeId");
            this.setTitle("Place Details");
        }
        else
        {
            new AlertDialog.Builder(PlaceDetailsActivity.this)
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

        mDatabase = FirebaseDatabase.getInstance().getReference("places");

        loadPlace();

    }

    public void loadPlace()
    {
        // Reads the data from the placeId in Firebase
        mDatabase.child(placeId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Place class
                place = dataSnapshot.getValue(AddressPlace.class);

                // Displays the found place's attributes
                placeName.setText(place.getName());
                description.setText(place.getDescription());
                address.setText(place.fullAddress());
                squad.setText(place.getInterest());

                /*
                // If user is the host
                if(firebaseUser.getUid().equals(place.getUser()))
                {
                    editMode();
                }
                */

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
        Toast.makeText(PlaceDetailsActivity.this, "Nothing here yet", Toast.LENGTH_LONG).show();
    }

    private void openMapDirections()
    {
        Toast.makeText(PlaceDetailsActivity.this, "Nothing here yet", Toast.LENGTH_LONG).show();
    }
}
