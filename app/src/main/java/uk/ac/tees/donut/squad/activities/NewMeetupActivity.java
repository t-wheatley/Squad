package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;

import android.location.Address;
import android.os.Handler;
import android.os.ResultReceiver;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.location.FetchAddressIntentService;
import uk.ac.tees.donut.squad.location.LocContants;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.squads.Squad;

public class NewMeetupActivity extends AppCompatActivity
{
    private static final String TAG = "Auth";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    private AddressResultReceiver mResultReceiver;
    private int fetchType;
    protected double latitude;
    protected double longitude;
    protected String addressFull;

    String name, description, squadId;
    HashMap<String, String> squads;

    private EditText editName;
    private EditText editAddress1;
    private EditText editAddress2;
    private EditText editAddressTC;
    private EditText editAddressC;
    private EditText editAddressPC;
    private Spinner spinnerSquad;
    private EditText editDescription;
    private Button btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meetup);
        this.setTitle("New Meetup");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creating new result reciever and setting the fetch type for geocoder
        mResultReceiver = new AddressResultReceiver(null);
        fetchType = LocContants.USE_ADDRESS_NAME;

        // Links the variables to their layout items.
        editAddress1 = (EditText) findViewById(R.id.newMeetup_textEditAddress1);
        editAddress2 =(EditText) findViewById(R.id.newMeetup_textEditAddress2);
        editAddressTC = (EditText) findViewById(R.id.newMeetup_textEditAddressTC);
        editAddressC = (EditText) findViewById(R.id.newMeetup_textEditAddressCounty);
        editAddressPC = (EditText) findViewById(R.id.newMeetup_textEditAddressPC);

        editName = (EditText) findViewById(R.id.newMeetup_textEditName);
        spinnerSquad = (Spinner) findViewById(R.id.newMeetup_spinnerSquad);
        editDescription = (EditText) findViewById(R.id.newMeetup_textEditDescription);
        btnSubmit = (Button) findViewById(R.id.newMeetup_buttonSubmit);


        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If at least one location field is filled
                if(checkEditTexts())
                {
                    submitMeetup();
                }
                else
                {
                    Toast.makeText(NewMeetupActivity.this, "Please provide a Name, Squad, " +
                            "Description and Location"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load interests and display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading...");
        loadingOverlay.setVisibility(View.VISIBLE);
        fillSpinner();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    new AlertDialog.Builder(NewMeetupActivity.this)
                            .setTitle("Sign-in Error")
                            .setMessage("You do not appear to be signed in, please try again.")
                            .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void geocode(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocContants.RECEIVER, mResultReceiver);
        intent.putExtra(LocContants.FETCH_TYPE_EXTRA, fetchType);
        intent.putExtra(LocContants.LOCATION_NAME_DATA_EXTRA, addressFull);

        Log.e(TAG, "Starting Service");
        startService(intent);
    }

    private void submitMeetup()
    {
        // Display loading overlay
        loadingText.setText("Posting your Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Gets the strings from the editTexts
        name = editName.getText().toString();
        description = editDescription.getText().toString();
        squadId = squads.get(spinnerSquad.getSelectedItem().toString().trim());
        addressFull = editAddress1.getText().toString() + " " + editAddress2.getText().toString()
                + " " + editAddressTC.getText().toString() + " " + editAddressC.getText().toString()
                + " " +editAddressPC.getText().toString();

        geocode();
    }

    // Takes a meetup and pushes it to the Firebase Realtime Database (Without extras)
    public void createMeetup(String name,  String desc, String squadId)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            // User is signed in
            // Creating a new meetup node and getting the key value
            String meetupId = mDatabase.child("meetups").push().getKey();

            // Creating a meetup object
            Meetup meetup = new Meetup(meetupId, name, desc, squadId, user.getUid(), longitude, latitude);

            // Pushing the meetup to the "meetups" node using the meetupId
            mDatabase.child("meetups").child(meetupId).setValue(meetup);

            // Adding the Meetup to the user's hosted
            mDatabase.child("users").child(user.getUid()).child("hosting").child(meetupId).setValue(true);

            // Send user to their meetup on the MeetupDetailActivity activity
            Intent intent = new Intent(NewMeetupActivity.this, MeetupDetailActivity.class);
            intent.putExtra("meetupId", meetupId);
            startActivity(intent);
            finish();

        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }


    }

    // Fill's the spinner with all of the interests stored in FireBase
    private void fillSpinner()
    {
        mDatabase.child("squads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                squads = new HashMap<String, String>();

                // Get all the squads
                for (DataSnapshot squadsSnapshot: dataSnapshot.getChildren()) {
                    Squad squad = squadsSnapshot.getValue(Squad.class);
                    squads.put(squad.getName(), squad.getId());
                }

                final List<String> squadNames = new ArrayList<String>();
                for (String name : squads.keySet())
                {
                    squadNames.add(name);
                }

                // Fill the spinner
                ArrayAdapter<String> interestAdapter = new ArrayAdapter<String>(NewMeetupActivity.this, android.R.layout.simple_spinner_item, squadNames);
                interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSquad.setAdapter(interestAdapter);

                // Hide the loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Checks at least one of the location fields has a value and name + desc have a value
    public boolean checkEditTexts()
    {
        // Checks if the name field is empty
        if (editName.getText().toString().trim().length() == 0)
        {
            editName.setError("Required");
            return false;
        }

        // Checks if the desc field is empty
        if (editDescription.getText().toString().trim().length() == 0)
        {
            editDescription.setError("Required");
            return false;
        }

        // Checks a location field has been entered
        if(editAddress1.getText().toString().trim().length() > 0)
        {
            return true;
        } else if(editAddress2.getText().toString().trim().length() > 0)
        {
            return true;
        } else if(editAddressTC.getText().toString().trim().length() > 0)
        {
            return true;
        } else if(editAddressC.getText().toString().trim().length() > 0)
        {
            return true;
        } else if(editAddressPC.getText().toString().trim().length() > 0)
        {
            return true;
        } else
        {
            return false;
        }
    }


    //Inner Class to receive address for geocoder
    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }


        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == LocContants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(LocContants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        latitude = address.getLatitude();
                        longitude= address.getLongitude();

                        // Calls the createMeetup method with the strings entered
                        createMeetup(name, description, squadId);

                    }
                });
            }
        }
    }
}