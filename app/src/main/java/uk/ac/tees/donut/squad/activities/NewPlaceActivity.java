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
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.R;

import uk.ac.tees.donut.squad.location.FetchAddressIntentService;
import uk.ac.tees.donut.squad.location.LocContants;
import uk.ac.tees.donut.squad.posts.AddressPlace;

import uk.ac.tees.donut.squad.posts.LocPlace;
import uk.ac.tees.donut.squad.posts.Meetup;

import uk.ac.tees.donut.squad.posts.Place;

public class NewPlaceActivity extends AppCompatActivity {

    private static final String TAG = "Auth";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    private Spinner spinnerInterest;
    private Button btnSubmit;

    private AddressResultReceiver mResultReceiver;
    private int fetchType;
    protected double latitude;
    protected double longitude;
    protected String addressFull;


    private EditText editName;
    private EditText editDescription;
    private EditText editAddress1;
    private EditText editAddress2;
    private EditText editAddressTC;
    private EditText editAddressC;
    private EditText editAddressPC;

    protected String name;
    protected String description;
    protected String interest;
    protected String a1;
    protected String a2;
    protected String pc;
    protected String tc;
    protected String c;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);
        this.setTitle("New Place");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creating new result reciever and setting the fetch type for geocoder
        mResultReceiver = new AddressResultReceiver(null);
        fetchType = LocContants.USE_ADDRESS_NAME;

        // Links the variables to their layout items.
        editName = (EditText) findViewById(R.id.textEditName);
        spinnerInterest = (Spinner) findViewById(R.id.spinnerInterest);
        editDescription = (EditText) findViewById(R.id.textEditDescription);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);

        editAddress1 = (EditText) findViewById(R.id.textEditAddress1);
        editAddress2 = (EditText) findViewById(R.id.textEditAddress2);
        editAddressTC = (EditText) findViewById(R.id.textEditAddressTC);
        editAddressC = (EditText) findViewById(R.id.textEditAddressCounty);
        editAddressPC = (EditText) findViewById(R.id.textEditAddressPC);

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When pressed calls the submitPlace method
                if(checkEditTexts())
                {
                    submitPlace();
                }
                else
                {
                    Toast.makeText(NewPlaceActivity.this, "Please provide a Name, Squad, " +
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
                    Toast.makeText(NewPlaceActivity.this, "No User", Toast.LENGTH_SHORT).show();

                    new AlertDialog.Builder(NewPlaceActivity.this)
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

    private void submitPlace(){
        // Display loading overlay
        loadingText.setText("Posting your Place...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Gets the strings from the editTexts
         name = editName.getText().toString();
         interest = spinnerInterest.getSelectedItem().toString();
         description = editDescription.getText().toString();

         a1 = editAddress1.getText().toString();
         a2 = editAddress2.getText().toString();
         tc = editAddressTC.getText().toString();
         c = editAddressC.getText().toString();
         pc = editAddressPC.getText().toString();

        addressFull = editAddress1.getText().toString() + " " + editAddress2.getText().toString()
                + " " + editAddressTC.getText().toString() + " " + editAddressC.getText().toString()
                + " " +editAddressPC.getText().toString();



        // Checks if the name field is empty
        if (TextUtils.isEmpty(name))
        {
            // If no string is found an error is output
            editName.setError("Required");
            return;
        }

        // Description is required
        if (TextUtils.isEmpty(description))
        {
            editDescription.setError("Required");
            return;
        }
        geocode();

        // Disable button so there are no multi-posts
        setEditingEnabled(false);



        // Re-enables the editTexts and buttons and finishes the activity
        setEditingEnabled(true);
        finish();
    }

    // Takes a meetup and pushes it to the Firebase Realtime Database (Without extras)
    public void createPlace(String n, String i, String d, String a1, String a2, String tc, String c, String pc, Double lat, Double lon){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            // Creating a new meetup node and getting the key value
            String placeId = mDatabase.child("places").push().getKey();

            // Creating a place object

            Place place = new LocPlace(placeId, n, i, d, user.getUid(), a1, a2, tc, c, pc, latitude, longitude);

            // Pushing the meetup to the "meetups" node using the placeId
            mDatabase.child("places").child(placeId).setValue(place);

            // Send user to their meetup on the MeetupDetailActivity activity
            Intent intent = new Intent(NewPlaceActivity.this, PlaceDetailsActivity.class);
            intent.putExtra("placeId", placeId);
            startActivity(intent);
            finish();
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }


    }

    // Takes a boolean value to either enable or disable the UI elements, this is used to avoid multiple posts
    private void setEditingEnabled(boolean enabled)
    {
        editName.setEnabled(enabled);
        spinnerInterest.setEnabled(enabled);
        editDescription.setEnabled(enabled);
        if (enabled)
        {
            btnSubmit.setVisibility(View.VISIBLE);
        } else
        {
            btnSubmit.setVisibility(View.GONE);
        }
    }

    // Fill's the spinner with all of the interests stored in FireBase
    private void fillSpinner()
    {
        mDatabase.child("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> interests = new ArrayList<String>();

                // Get all the interests
                for (DataSnapshot interestSnapshot: dataSnapshot.getChildren()) {
                    String interest = interestSnapshot.child("name").getValue(String.class);
                    interests.add(interest);
                }

                // Fill the spinner
                ArrayAdapter<String> interestAdapter = new ArrayAdapter<String>(NewPlaceActivity.this, android.R.layout.simple_spinner_item, interests);
                interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInterest.setAdapter(interestAdapter);

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


                        // Calls the createPlace method with the strings entered
                        createPlace(name, interest, description, a1, a2, tc, c, pc, longitude, latitude);


                    }
                });
            }
        }
    }
}
