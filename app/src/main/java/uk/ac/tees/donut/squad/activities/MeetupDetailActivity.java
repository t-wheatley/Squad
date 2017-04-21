package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
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
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.users.User;

public class MeetupDetailActivity extends AppCompatActivity
{
    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    Meetup meetup;

    TextView nameDisplay;
    TextView interestDisplay;
    TextView descriptionDisplay;
    String meetupId;

    ImageButton editName;
    ImageButton editDesc;
    Button attendBtn;
    Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_detail);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Declaring everything
        nameDisplay = (TextView) findViewById(R.id.meetupDetail_textEditName);
        interestDisplay = (TextView) findViewById(R.id.meetupDetail_textEditInterest);
        descriptionDisplay = (TextView) findViewById(R.id.meetupDetail_textEditDescription);
        attendBtn = (Button) findViewById(R.id.meetupDetail_attendBtn);
        deleteBtn = (Button) findViewById(R.id.meetupDetail_deleteBtn);
        editName = (ImageButton) findViewById(R.id.meetupDetail_imageButtonEditName);
        editDesc = (ImageButton) findViewById(R.id.meetupDetail_imageButtonEditDescription);

        // Disabling the edit ImageButtons and delete Button
        editName.setEnabled(false);
        editName.setVisibility(View.GONE);
        editDesc.setEnabled(false);
        editDesc.setVisibility(View.GONE);
        deleteBtn.setEnabled(false);
        deleteBtn.setVisibility(View.GONE);


        // Disabling the editTexts
        nameDisplay.setEnabled(false);
        interestDisplay.setEnabled(false);
        descriptionDisplay.setEnabled(false);

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(b != null)
        {
            // Collects the meetupId passed from the RecyclerView
            meetupId = (String) b.get("meetupId");
            this.setTitle("Meetup Details");
        } else
        {
            new AlertDialog.Builder(MeetupDetailActivity.this)
                    .setTitle("Error")
                    .setMessage("The meetup went missing somewhere, please try again.")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        if(User.myMeetupsContains(meetupId))
            attendBtn.setText("Unattend Meetup");
            
        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("meetups");

        // Loads the data for the Meetup from Firebase
        loadMeetup();

    }

    public void loadMeetup()
    {
        // Reads the data from the meetupId in Firebase
        mDatabase.child(meetupId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Meetup class
                meetup = dataSnapshot.getValue(Meetup.class);

                // Displays the found meetup's attributes
                nameDisplay.setText(meetup.getName());
                interestDisplay.setText(meetup.getInterest());
                descriptionDisplay.setText(meetup.getDescription());

                // If user is the host
                if(firebaseUser.getUid().equals(meetup.getUser()))
                {
                    editMode();
                }
                // Hiding loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void attendMeetup(View view)
    {
        if(User.myMeetupsContains(meetupId))
        {
            User.removeMeetup(meetupId);
            attendBtn.setText("Attend Button");
        }
        else
        {
            User.addMeetup(meetupId);
            attendBtn.setText("Unattend Button");
        }
    }

    public void deleteMeetup()
    {
        new AlertDialog.Builder(this)
                .setTitle("Delete Meetup")
                .setMessage("Are you sure you want to delete this Meetup?" +
                        "\nYou will not be able to get it back!")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child(meetupId).removeValue();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .show();
    }

    public void editMode()
    {
        // Enabling the edit ImageButtons
        editName.setEnabled(true);
        editName.setVisibility(View.VISIBLE);
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Dialog to edit Name
                editName();
            }
        });

        editDesc.setEnabled(true);
        editDesc.setVisibility(View.VISIBLE);
        editDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Dialog to edit Description
                editDesc();
            }
        });

        deleteBtn.setEnabled(true);
        deleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Dialog to confirm deletion of Meetup
                deleteMeetup();
            }
        });
    }

    public void editDesc()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Description:");

        // EditText for input
        final EditText editTextDesc = new EditText(this);
        // Sets the expected input types, text, long message, auto correct and multi line
        editTextDesc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        // Sets the maximum characters to 120
        editTextDesc.setFilters(new InputFilter[] { new InputFilter.LengthFilter(120) });
        builder.setView(editTextDesc);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String desc = editTextDesc.getText().toString();
                descriptionDisplay.setText(desc);
                updateDesc(desc);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Displays the Dialog
        builder.show();
    }

    public void updateDesc(String desc)
    {
        if (firebaseUser != null)
        {
            // Pushing the new description to the description field of the meetup's data
            mDatabase.child(meetupId).child("description").setValue(desc);
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void editName()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name:");

        // EditText for input
        final EditText editTextName = new EditText(this);
        // Sets the expected input types, text, long message, auto correct and multi line
        editTextName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        // Sets the maximum characters to 25
        editTextName.setFilters(new InputFilter[] { new InputFilter.LengthFilter(25) });
        builder.setView(editTextName);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString();
                nameDisplay.setText(name);
                updateName(name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Displays the Dialog
        builder.show();
    }

    public void updateName(String name)
    {
        if (firebaseUser != null)
        {
            // Pushing the new name to the name field of the meetup's data
            mDatabase.child(meetupId).child("name").setValue(name);
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }


}
