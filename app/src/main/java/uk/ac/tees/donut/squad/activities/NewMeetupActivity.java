package uk.ac.tees.donut.squad.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.location.FetchAddressIntentService;
import uk.ac.tees.donut.squad.location.LocContants;
import uk.ac.tees.donut.squad.posts.LocPlace;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.posts.Place;
import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Activity which allows the user to create a new Meetup.
 */
public class NewMeetupActivity extends AppCompatActivity
{
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    // Loading Overlay
    private RelativeLayout loadingOverlay;
    private TextView loadingText;

    // Activity UI
    private LinearLayout newAddressLayout;
    private EditText editName;
    private EditText editAddress1;
    private EditText editAddress2;
    private EditText editAddressTC;
    private EditText editAddressC;
    private EditText editAddressPC;
    private Spinner spinnerSquad;
    private Spinner spinnerPlace;
    private EditText editDescription;
    private FloatingActionButton btnSubmit;
    private Button btnFromDate;
    private Button btnFromTime;
    private Button btnUntilDate;
    private Button btnUntilTime;
    private Button btnNewAddress;
    private Button btnFromPlace;
    private CardView placeCard;

    // Location
    private AddressResultReceiver mResultReceiver;
    private int fetchType;
    protected double latitude;
    protected double longitude;
    protected String addressFull;
    protected String geocodeAddress;

    // Variables
    private String name, description, squadId;
    private HashMap<String, String> squads;
    private HashMap<String, String> places;
    private Calendar fromDateTime;
    private Calendar untilDateTime;
    private Calendar currentDateTime;
    protected String a1, a2, pc, tc, county;
    private int currentYear, currentMonth, currentDay, currentHour, currentMinute;
    private boolean spinnerAddress;

    // Final values
    private static final String TAG = "Auth";

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

        // UI Elements
        newAddressLayout = (LinearLayout) findViewById(R.id.newMeetup_manualAddress);
        editAddress1 = (EditText) findViewById(R.id.newMeetup_textEditAddress1);
        editAddress2 = (EditText) findViewById(R.id.newMeetup_textEditAddress2);
        editAddressTC = (EditText) findViewById(R.id.newMeetup_textEditAddressTC);
        editAddressC = (EditText) findViewById(R.id.newMeetup_textEditAddressCounty);
        editAddressPC = (EditText) findViewById(R.id.newMeetup_textEditAddressPC);
        editName = (EditText) findViewById(R.id.newMeetup_textEditName);
        spinnerSquad = (Spinner) findViewById(R.id.newMeetup_spinnerSquad);
        placeCard = (CardView) findViewById(R.id.newMeetup_existingPlace);
        spinnerPlace = (Spinner) findViewById(R.id.newMeetup_spinnerPlace);
        editDescription = (EditText) findViewById(R.id.newMeetup_textEditDescription);
        btnSubmit = (FloatingActionButton) findViewById(R.id.newMeetup_buttonSubmit);
        btnFromDate = (Button) findViewById(R.id.newMeetup_buttonFromDate);
        btnFromTime = (Button) findViewById(R.id.newMeetup_buttonFromTime);
        btnUntilDate = (Button) findViewById(R.id.newMeetup_buttonUntilDate);
        btnUntilTime = (Button) findViewById(R.id.newMeetup_buttonUntilTime);
        btnNewAddress = (Button) findViewById(R.id.newMeetup_buttonNewAddress);
        btnFromPlace = (Button) findViewById(R.id.newMeetup_buttonFromPlace);

        a1= "";
        a2 = "";
        tc = "";
        county ="";
        pc = "";

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // If at least one location field is filled
                if (spinnerAddress)
                {
                    if (fromDateTime == null)
                    {
                        Toast.makeText(NewMeetupActivity.this, "Please provide a start date and time"
                                , Toast.LENGTH_SHORT).show();
                    } else if (untilDateTime == null)
                    {
                        Toast.makeText(NewMeetupActivity.this, "Please provide an end date and time"
                                , Toast.LENGTH_SHORT).show();
                    } else
                    {
                        submitMeetup();
                    }
                } else
                {

                    if (checkEditTexts())
                    {
                        if (fromDateTime == null)
                        {
                            Toast.makeText(NewMeetupActivity.this, "Please provide a start date and time"
                                    , Toast.LENGTH_SHORT).show();
                        } else if (untilDateTime == null)
                        {
                            Toast.makeText(NewMeetupActivity.this, "Please provide an end date and time"
                                    , Toast.LENGTH_SHORT).show();
                        } else
                        {
                            submitMeetup();
                        }
                    } else
                    {
                        Toast.makeText(NewMeetupActivity.this, "Please provide a Name, Squad, " +
                                        "Description and Location"
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnFromDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Launch Date Picker Dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewMeetupActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth)
                            {
                                btnFromDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                fromDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                fromDateTime.set(Calendar.MONTH, monthOfYear);
                                fromDateTime.set(Calendar.YEAR, year);
                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();
            }
        });

        btnFromTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewMeetupActivity.this,
                        new TimePickerDialog.OnTimeSetListener()
                        {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute)
                            {
                                if (minute < 10)
                                {
                                    btnFromTime.setText(hourOfDay + ":0" + minute);
                                } else
                                {
                                    btnFromTime.setText(hourOfDay + ":" + minute);
                                }
                                fromDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                fromDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, currentHour, currentMinute, true);
                timePickerDialog.show();
            }
        });

        btnUntilDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Launch Date Picker Dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewMeetupActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth)
                            {
                                btnUntilDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                untilDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                untilDateTime.set(Calendar.MONTH, monthOfYear);
                                untilDateTime.set(Calendar.YEAR, year);

                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();
            }
        });

        btnUntilTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewMeetupActivity.this,
                        new TimePickerDialog.OnTimeSetListener()
                        {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute)
                            {
                                if (minute < 10)
                                {
                                    btnUntilTime.setText(hourOfDay + ":0" + minute);
                                } else
                                {
                                    btnUntilTime.setText(hourOfDay + ":" + minute);
                                }
                                untilDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                untilDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, currentHour, currentMinute, true);
                timePickerDialog.show();
            }
        });

        btnNewAddress.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                newAddress();
            }
        });

        btnFromPlace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fromPlace();
            }
        });

        spinnerSquad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (spinnerAddress)
                {
                    fillPlaceSpinner(squads.get(spinnerSquad.getSelectedItem().toString().trim()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        // Defaults
        spinnerAddress = false;
        fromDateTime = Calendar.getInstance();
        untilDateTime = Calendar.getInstance();

        // Load Squads and display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading...");
        loadingOverlay.setVisibility(View.VISIBLE);
        fillSquadSpinner();
        displayCurrentDateTime();

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

                    new AlertDialog.Builder(NewMeetupActivity.this)
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
                            .setCancelable(false)
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
     * Method called on submit button press, validates the proposed meetup.
     */
    private void submitMeetup()
    {
        // Display loading overlay
        loadingText.setText("Posting your Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Gets the strings from the editTexts
        name = editName.getText().toString().trim();
        description = editDescription.getText().toString().trim();
        squadId = squads.get(spinnerSquad.getSelectedItem().toString().trim());

        // Start and End time validation
        if (fromDateTime.getTimeInMillis() == currentDateTime.getTimeInMillis())
        {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(NewMeetupActivity.this, "Please change the Meetup's Start from the " +
                    "default", Toast.LENGTH_SHORT).show();
        } else if (untilDateTime.getTimeInMillis() == currentDateTime.getTimeInMillis())
        {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(NewMeetupActivity.this, "Please change the Meetup's End from the " +
                    "default", Toast.LENGTH_SHORT).show();
        } else if (fromDateTime.getTimeInMillis() < currentDateTime.getTimeInMillis())
        {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(NewMeetupActivity.this, "A Meetup can not start in the past",
                    Toast.LENGTH_SHORT).show();
        } else if (untilDateTime.getTimeInMillis() < currentDateTime.getTimeInMillis())
        {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(NewMeetupActivity.this, "A Meetup can not end in the past",
                    Toast.LENGTH_SHORT).show();
        } else if (untilDateTime.getTimeInMillis() < fromDateTime.getTimeInMillis())
        {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(NewMeetupActivity.this, "A Meetup can not end before it starts",
                    Toast.LENGTH_SHORT).show();
        } else if (spinnerAddress)
        {
            // If using a Place instead of an address
            String placeId = places.get(spinnerPlace.getSelectedItem().toString().trim());
            mDatabase.child("places").child(placeId).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    // Gets the data from Firebase and stores it in a LocPlace class
                    LocPlace firebasePlace = dataSnapshot.getValue(LocPlace.class);

                    // Gets the latitude and longitude from the Place
                    longitude = firebasePlace.getLocLong();
                    latitude = firebasePlace.getLocLat();

                    if(firebasePlace.getAddress1() != null)
                    {
                        a1 = firebasePlace.getAddress1();
                        a2 = firebasePlace.getAddress2();
                        tc = firebasePlace.getTownCity();
                        county = firebasePlace.getCounty();
                        pc = firebasePlace.getPostCode();
                    }


                    if (longitude != 0 && latitude != 0)
                    {

                        createMeetup(name, description, squadId);
                    } else
                    {
                        Toast.makeText(NewMeetupActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    // No place found
                    Toast.makeText(NewMeetupActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else
        {
            // New lat and long needed
            addressFull = editAddress1.getText().toString() + " " + editAddress2.getText().toString()
                    + " " + editAddressTC.getText().toString() + " " + editAddressC.getText().toString()
                    + " " + editAddressPC.getText().toString();

            geocode();
        }
    }

    /**
     * Method to post a Meetup to the Firebase Realtime Database.
     *
     * @param name    The name for the Meetup.
     * @param desc    The description for the Meetup.
     * @param squadId The Squad for the Meetup.
     */
    public void createMeetup(String name, String desc, String squadId)
    {
        // Gets the currently signed-in User
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            // User is signed in
            // Creating a new meetup node and getting the key value
            String meetupId = mDatabase.child("meetups").push().getKey();

            // Getting the DateTimes as long
            long fromUnix = fromDateTime.getTimeInMillis() / 1000L;
            long untilUnix = untilDateTime.getTimeInMillis() / 1000L;

            // Creating a meetup object
            Meetup meetup = new Meetup(meetupId, name, desc, squadId, user.getUid(), fromUnix, untilUnix, longitude, latitude, a1, a2, tc, county,pc);

            // If at a existing place
            if (spinnerAddress)
            {
                String placeId = places.get(spinnerPlace.getSelectedItem().toString().trim());
                meetup.setPlace(placeId);
                mDatabase.child("places").child(placeId).child("meetups").child(meetupId).setValue(true);
            }

            // Pushing the meetup to the "meetups" node using the meetupId
            mDatabase.child("meetups").child(meetupId).setValue(meetup);

            // Adding the Meetup to the user's hosted
            mDatabase.child("users").child(user.getUid()).child("hosting").child(meetupId).setValue(true);

            // Pushing the place to the squad it belongs to
            mDatabase.child("squads").child(squadId).child("meetups").child(meetupId).setValue(true);

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

    /**
     * Method that fills the spinner with all of the squads stored in FireBase
     */
    private void fillSquadSpinner()
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
                ArrayAdapter<String> squadAdapter = new ArrayAdapter<String>(NewMeetupActivity.this, android.R.layout.simple_spinner_item, squadNames);
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
    public void CreateAlertDiolog()
    {
        new AlertDialog.Builder(NewMeetupActivity.this)
                .setTitle("Confirm Address")
                .setMessage("" + geocodeAddress + "\n" + "Is this the correct address?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Confirm Address", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                        // Calls the createMeetup method with the strings entered
                        createMeetup(name, description, squadId);


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
            //If address was found
            if (resultCode == LocContants.SUCCESS_RESULT)
            {
                //Create address object
                final Address address = resultData.getParcelable(LocContants.RESULT_ADDRESS);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        //Get longitude, latittude and addresss from addresss
                        latitude = address.getLatitude();
                        longitude = address.getLongitude();

                        geocodeAddress = resultData.getString(LocContants.RESULT_DATA_KEY);

                        int addressLength = address.getMaxAddressLineIndex();

                        //Switch used to sort how to display app dependent on address length
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
                                county = address.getAddressLine(3);
                                pc = address.getAddressLine(4);
                                break;
                            case 6:
                                a1 = address.getAddressLine(0);
                                a2 = address.getAddressLine(1) + " " + address.getAddressLine(2);
                                tc = address.getAddressLine(4);
                                county = address.getAddressLine(3);
                                pc = address.getAddressLine(5);
                                break;
                        }


                        CreateAlertDiolog();

                    }
                });
                //if unable to get address
            } else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        loadingOverlay.setVisibility(View.INVISIBLE);
                        Toast.makeText(NewMeetupActivity.this, "Invalid Address, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * Method thats called when the User wants to use a new address.
     */
    public void newAddress()
    {
        spinnerAddress = false;

        newAddressLayout.setVisibility(View.VISIBLE);
        placeCard.setVisibility(View.GONE);
    }

    /**
     * Method thats called when the User wants to use a existing Place.
     */
    public void fromPlace()
    {
        spinnerAddress = true;

        newAddressLayout.setVisibility(View.GONE);
        placeCard.setVisibility(View.VISIBLE);
        fillPlaceSpinner(squads.get(spinnerSquad.getSelectedItem().toString().trim()));

    }

    /**
     * Method to fill a spinner with all of a Squad's Places.
     *
     * @param id Squad's id to retreive Places.
     */
    public void fillPlaceSpinner(String id)
    {
        loadingText.setText("Getting the Squad's Places");
        loadingOverlay.setVisibility(View.VISIBLE);

        mDatabase.child("places").orderByChild("squad").equalTo(id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.getChildrenCount() == 0)
                {
                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);

                    newAddress();

                    Toast.makeText(NewMeetupActivity.this, "No Places in this squad", Toast.LENGTH_SHORT).show();
                } else
                {

                    places = new HashMap<String, String>();

                    // Get all the squads
                    for (DataSnapshot placesSnapshot : dataSnapshot.getChildren())
                    {
                        Place place = placesSnapshot.getValue(Place.class);
                        places.put(place.getName(), place.getPlaceId());
                    }

                    final List<String> placeNames = new ArrayList<String>();
                    for (String name : places.keySet())
                    {
                        placeNames.add(name);
                    }

                    // Fill the spinner
                    ArrayAdapter<String> squadAdapter = new ArrayAdapter<String>(NewMeetupActivity.this, android.R.layout.simple_spinner_item, placeNames);
                    squadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPlace.setAdapter(squadAdapter);

                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    /**
     * Method that gets and displays the current Date and Time on the date and time pickers.
     */
    public void displayCurrentDateTime()
    {
        // Getting the users current DateTime
        currentDateTime = Calendar.getInstance();
        fromDateTime = (Calendar) currentDateTime.clone();
        untilDateTime = (Calendar) currentDateTime.clone();

        currentYear = currentDateTime.get(Calendar.YEAR);
        currentMonth = currentDateTime.get(Calendar.MONTH);
        currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
        currentHour = currentDateTime.get(Calendar.HOUR_OF_DAY);
        currentMinute = currentDateTime.get(Calendar.MINUTE);

        // Displaying the current DateTime
        btnFromDate.setText(currentDay + "/" + (currentMonth + 1) + "/" + currentYear);
        btnUntilDate.setText(currentDay + "/" + (currentMonth + 1) + "/" + currentYear);

        if (currentMinute < 10)
        {
            btnFromTime.setText(currentHour + ":0" + currentMinute);
            btnUntilTime.setText(currentHour + ":0" + currentMinute);
        } else
        {
            btnFromTime.setText(currentHour + ":" + currentMinute);
            btnUntilTime.setText(currentHour + ":" + currentMinute);
        }
    }
}