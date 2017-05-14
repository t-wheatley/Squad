package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import uk.ac.tees.donut.squad.posts.LocPlace;
import uk.ac.tees.donut.squad.posts.Place;
import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Activity which allows the user to create a new Place.
 */
public class NewPlaceActivity extends AppCompatActivity
{
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;

    // Activity UI
    private EditText editName;
    private EditText editDescription;
    private EditText editAddress1;
    private EditText editAddress2;
    private EditText editAddressTC;
    private EditText editAddressC;
    private EditText editAddressPC;
    private Spinner spinnerSquad;
    private Button btnSubmit;

    // Location
    private AddressResultReceiver mResultReceiver;
    private int fetchType;
    protected double latitude;
    protected double longitude;
    protected String addressFull;
    protected String geocodeAddress;

    // Variables
    protected String name;
    protected String description;
    protected String squadId;
    protected String a1;
    protected String a2;
    protected String pc;
    protected String tc;
    protected String c;
    HashMap<String, String> squads;

    // Final values
    private static final String TAG = "Auth";

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

        // UI Elements
        editName = (EditText) findViewById(R.id.textEditName);
        spinnerSquad = (Spinner) findViewById(R.id.spinnerSquad);
        editDescription = (EditText) findViewById(R.id.textEditDescription);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        editAddress1 = (EditText) findViewById(R.id.textEditAddress1);
        editAddress2 = (EditText) findViewById(R.id.textEditAddress2);
        editAddressTC = (EditText) findViewById(R.id.textEditAddressTC);
        editAddressC = (EditText) findViewById(R.id.textEditAddressCounty);
        editAddressPC = (EditText) findViewById(R.id.textEditAddressPC);

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // When pressed calls the submitPlace method
                if (checkEditTexts())
                {
                    submitPlace();
                } else
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

        // AuthStateListener
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(NewPlaceActivity.this, "No User", Toast.LENGTH_SHORT).show();

                    new AlertDialog.Builder(NewPlaceActivity.this)
                            .setTitle("Sign-in Error")
                            .setMessage("You do not appear to be signed in, please try again.")
                            .setPositiveButton("Back", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
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
    public void onStart()
    {
        super.onStart();
        // Connecting the AuthStateListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
        {
            // Removing the AuthStateListener
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Method to get the Latitude and Longitude of the location.
     */
    private void geocode()
    {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocContants.RECEIVER, mResultReceiver);
        intent.putExtra(LocContants.FETCH_TYPE_EXTRA, fetchType);
        intent.putExtra(LocContants.LOCATION_NAME_DATA_EXTRA, addressFull);

        Log.e(TAG, "Starting Service");
        startService(intent);
    }

    /**
     * Method called on submit button press, validates the proposed Place.
     */
    private void submitPlace()
    {
        // Display loading overlay
        loadingText.setText("Posting your Place...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Gets the strings from the editTexts
        name = editName.getText().toString().trim();
        description = editDescription.getText().toString().trim();

        a1 = editAddress1.getText().toString().trim();
        a2 = editAddress2.getText().toString().trim();
        tc = editAddressTC.getText().toString().trim();
        c = editAddressC.getText().toString().trim();
        pc = editAddressPC.getText().toString().trim();

        addressFull = editAddress1.getText().toString().trim() + " " +
                editAddress2.getText().toString().trim() + " " +
                editAddressTC.getText().toString().trim() + " " +
                editAddressC.getText().toString().trim() + " " +
                editAddressPC.getText().toString().trim();

        squadId = squads.get(spinnerSquad.getSelectedItem().toString().trim());

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

        // Get the latitude and longitude
        geocode();

        // Disable button so there are no multi-posts
        setEditingEnabled(false);

        // Re-enables the editTexts and buttons and finishes the activity
        setEditingEnabled(true);
    }

    /**
     * Method to post a Place to the Firebase Realtime Database.
     *
     * @param n        The name of the Place.
     * @param d        The description of the Place.
     * @param s        The Squad of the Place.
     * @param a1       The address line 1 of the Place.
     * @param a2       The address line 2 of the Place.
     * @param tc       The town/city of the Place.
     * @param c        The county of the Place.
     * @param pc       The postcode of the Place.
     * @param lat      The latitude of the Place.
     * @param lon      The longitude of the Place.
     * @param fullAddy The full address of the Place.
     */
    public void createPlace(String n, String d, String s, String a1, String a2, String tc, String c, String pc, Double lat, Double lon, String fullAddy)
    {

        // Gets the currently signed-in User
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            // User is signed in
            // Creating a new meetup node and getting the key value
            String placeId = mDatabase.child("places").push().getKey();

            // Creating a place object
            Place place = new LocPlace(placeId, n, d, s, user.getUid(), a1, a2, tc, c, pc, lon, lat);

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

    /**
     * Method to enable or disable the UI elements, this is used to avoid multiple posts.
     *
     * @param enabled boolean to determine if editable.
     */
    private void setEditingEnabled(boolean enabled)
    {
        editName.setEnabled(enabled);
        spinnerSquad.setEnabled(enabled);
        editDescription.setEnabled(enabled);
        if (enabled)
        {
            btnSubmit.setVisibility(View.VISIBLE);
        } else
        {
            btnSubmit.setVisibility(View.GONE);
        }
    }

    /**
     * Method to fill the spinner with all of the Squads stored in FireBase
     */
    private void fillSpinner()
    {
        mDatabase.child("squads").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                squads = new HashMap<String, String>();

                // Get all the squads
                for (DataSnapshot squadsSnapshot : dataSnapshot.getChildren())
                {
                    Squad squad = squadsSnapshot.getValue(Squad.class);
                    squads.put(squad.getName(), squad.getId());
                }

                final List<String> squadNames = new ArrayList<String>();
                for (String name : squads.keySet())
                {
                    squadNames.add(name);
                }

                // Fill the spinner
                ArrayAdapter<String> squadAdapter = new ArrayAdapter<String>(NewPlaceActivity.this, android.R.layout.simple_spinner_item, squadNames);
                squadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSquad.setAdapter(squadAdapter);

                // Hide the loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    /**
     * Method that validates the text edits.
     *
     * @return boolean if valid.
     */
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
        if (editAddress1.getText().toString().trim().length() > 0)
        {
            return true;
        } else if (editAddress2.getText().toString().trim().length() > 0)
        {
            return true;
        } else if (editAddressTC.getText().toString().trim().length() > 0)
        {
            return true;
        } else if (editAddressC.getText().toString().trim().length() > 0)
        {
            return true;
        } else if (editAddressPC.getText().toString().trim().length() > 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * Method to display an AlertDialog to validate address.
     */
    public void CreateAlertDialog()
    {
        new AlertDialog.Builder(NewPlaceActivity.this)
                .setTitle("Confirm Address")
                .setMessage("" + geocodeAddress + "\n" + "Is this the correct address?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Confirm Address", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // Calls the createPlace method with the strings entered
                        createPlace(name, description, squadId, a1, a2, tc, c, pc, longitude, latitude, geocodeAddress);
                        finish();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        loadingOverlay.setVisibility(View.INVISIBLE);
                        return;
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {

                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        loadingOverlay.setVisibility(View.INVISIBLE);
                        return;
                    }
                })
                .create()
                .show();

    }

    /**
     * Inner Class to receive address for geocoder.
     */
    public class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler)
        {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData)
        {
            if (resultCode == LocContants.SUCCESS_RESULT)
            {
                final Address address = resultData.getParcelable(LocContants.RESULT_ADDRESS);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        latitude = address.getLatitude();
                        longitude = address.getLongitude();
                        geocodeAddress = resultData.getString(LocContants.RESULT_DATA_KEY);

                        int addressLength = address.getMaxAddressLineIndex();


                        switch (addressLength)
                        {

                            default:
                                a1 = address.getAddressLine(0);
                                break;
                            case 1:
                                a1 = address.getAddressLine(0);
                                break;
                            case 2:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1);
                                break;
                            case 3:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1);
                                pc = address.getAddressLine(2);
                                break;
                            case 4:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1);
                                tc = address.getAddressLine(2);
                                pc = address.getAddressLine(3);
                                break;
                            case 5:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1);
                                tc = address.getAddressLine(2);
                                c = address.getAddressLine(3);
                                pc = address.getAddressLine(4);
                                break;
                            case 6:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1) + " " + address.getAddressLine(2);
                                tc = address.getAddressLine(4);
                                c = address.getAddressLine(3);
                                pc = address.getAddressLine(5);
                                break;
                        }

                        CreateAlertDialog();


                    }
                });
            } else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        loadingOverlay.setVisibility(View.INVISIBLE);
                        Toast.makeText(NewPlaceActivity.this, "Invalid Address, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}