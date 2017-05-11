package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mvc.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.UserGridViewAdapter;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.users.FBUser;

public class MeetupDetailActivity extends BaseActivity
{
    // Firebase
    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference meetupStorage;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;
    ProgressBar loadingImage;

    // Activity UI
    TextView nameDisplay;
    TextView squadDisplay;
    TextView hostDisplay;
    TextView statusDisplay;
    TextView descriptionDisplay;
    TextView startDateDisplay;
    TextView endDateDisplay;
    TextView attendingDisplay;
    TextView memberCountDisplay;
    ImageView meetupImage;
    ImageButton editName;
    ImageButton editDesc;
    Button attendBtn;
    Button deleteBtn;

    // Variables
    String meetupId;
    Meetup meetup;
    Boolean attending;

    // Members display
    GridView attendeesGrid;
    List<String> userNames;
    List<String> userPics;
    List<String> userIds;

    int secretCount;
    int memberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_detail);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingImage = (ProgressBar) findViewById(R.id.meetupDetail_imageProgress);
        loadingText.setText("Loading Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingImage.setVisibility(View.VISIBLE);

        // Declaring everything
        nameDisplay = (TextView) findViewById(R.id.meetupDetail_textEditName);
        squadDisplay = (TextView) findViewById(R.id.meetupDetail_textEditSquad);
        hostDisplay = (TextView) findViewById(R.id.meetupDetail_textEditHost);
        descriptionDisplay = (TextView) findViewById(R.id.meetupDetail_textEditDescription);
        statusDisplay = (TextView) findViewById(R.id.meetupDetail_textStatus);
        startDateDisplay = (TextView) findViewById(R.id.meetupDetail_startDate);
        endDateDisplay = (TextView) findViewById(R.id.meetupId_endDate);
        attendeesGrid = (GridView) findViewById(R.id.meetupDetail_userGrid);
        attendBtn = (Button) findViewById(R.id.meetupDetail_attendBtn);
        deleteBtn = (Button) findViewById(R.id.meetupDetail_deleteBtn);
        editName = (ImageButton) findViewById(R.id.meetupDetail_imageButtonEditName);
        editDesc = (ImageButton) findViewById(R.id.meetupDetail_imageButtonEditDescription);
        attendingDisplay = (TextView) findViewById(R.id.meetupDetail_textEditAttendees);
        memberCountDisplay = (TextView) findViewById(R.id.meetupDetail_textViewAttendees);
        meetupImage = (ImageView) findViewById(R.id.meetupDetail_ImageView);


        // Disabling the edit ImageButtons and delete Button
        editName.setEnabled(false);
        editName.setVisibility(View.GONE);
        editDesc.setEnabled(false);
        editDesc.setVisibility(View.GONE);
        deleteBtn.setEnabled(false);
        deleteBtn.setVisibility(View.GONE);
        meetupImage.setEnabled(false);

        // Disabling the editTexts
        nameDisplay.setEnabled(false);
        descriptionDisplay.setEnabled(false);

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            // Collects the meetupId passed from the RecyclerView
            meetupId = (String) b.get("meetupId");
            this.setTitle("Meetup Details");
        } else
        {
            new AlertDialog.Builder(MeetupDetailActivity.this)
                    .setTitle("Error")
                    .setMessage("The meetup went missing somewhere, please try again.")
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

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Getting the reference for the Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Defaults
        attending = false;
        attendBtn.setText("Attend Meetup");
        secretCount = 0;
        memberCount = 0;

        // Starts the loading chain
        // loadMeetup -> loadSquad -> loadHost -> loadUsers
        loadMeetup();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_meetup_detail;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.menu_meetups;
    }

    public void loadMeetup()
    {
        // Reads the data from the meetupId in Firebase
        mDatabase.child("meetups").child(meetupId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Meetup class
                meetup = dataSnapshot.getValue(Meetup.class);

                // Displays the found meetup's attributes
                nameDisplay.setText(meetup.getName());
                descriptionDisplay.setText(meetup.getDescription());

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                String startDate = sdf.format(meetup.getStartDateTime() * 1000L);
                String endDate = sdf.format(meetup.getEndDateTime() * 1000L);

                startDateDisplay.setText(startDate);
                endDateDisplay.setText(endDate);

                meetup.updateStatus();
                int status = meetup.gimmeStatus();
                if(status == 0)
                    statusDisplay.setText("Upcoming");
                else if(status == 1)
                    statusDisplay.setText("Ongoing");
                else if(status == 2)
                    statusDisplay.setText("Expired");
                else
                    statusDisplay.setText("Deleted");

                // If user is the host
                if (firebaseUser.getUid().equals(meetup.getHost()))
                {
                    editMode();
                }

                // Creating the reference for the meetup's Firebase Storage
                meetupStorage = firebaseStorage.getReference().child("meetups/" + meetup.getId() + ".jpg");

                // Load the name of the Squad
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
        loadingText.setText("Getting the Meetup's Squad...");

        // Get Squad name from id
        mDatabase.child("squads").child(meetup.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                squadDisplay.setText(dataSnapshot.child("name").getValue(String.class));

                // Load the name of the Host
                loadHost();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void viewSquad(View view)
    {
        //Sends the id to the details activity
        Intent detail = new Intent(MeetupDetailActivity.this, SquadDetailActivity.class);
        detail.putExtra("squadId", meetup.getSquad());
        startActivity(detail);
    }


    public void loadHost()
    {
        // Setting the loading text
        loadingText.setText("Getting the Meetup's Host...");

        // Get Host name from id
        mDatabase.child("users").child(meetup.getHost()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                hostDisplay.setText(dataSnapshot.child("name").getValue(String.class));

                // Load the attendees of the Meetup
                loadUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void viewHost(View view)
    {
        //Sends the id to the details activity
        Intent detail = new Intent(MeetupDetailActivity.this, ProfileActivity.class);
        detail.putExtra("uId", meetup.getHost());
        startActivity(detail);
    }


    public void loadUsers()
    {
        // Array of names
        userNames = new ArrayList<String>();
        // Array of pictures
        userPics = new ArrayList<String>();
        // Array of uIds
        userIds = new ArrayList<String>();

        // Setting the loading text
        loadingText.setText("Getting the Meetup's attendees...");

        // Getting the users HashMap
        HashMap<String, Boolean> users = meetup.getUsers();

        // If the HashMap isnt empty
        if (users != null)
        {

            // Getting the amount of users
            final int usersSize = users.size();

            // Checking if the user is already in the Meetup
            if (users.containsKey(firebaseUser.getUid()))
            {
                attending = true;
                attendBtn.setText("Leave Meetup");
            }

            // Displaying members of the Meetup
            for (final String uId : users.keySet())
            {
                mDatabase.child("users").child(uId).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Getting each member and adding their name to the memberList
                        FBUser user = dataSnapshot.getValue(FBUser.class);

                        // Checks if the user is not secret
                        if(user.getSecret() == null || user.getSecret() == false)
                        {
                            userNames.add(user.getName());
                            userPics.add(user.getPicture());
                            userIds.add(uId);
                        } else
                        {
                            secretCount++;
                        }

                        memberCount++;
                        // If all members added
                        if (usersSize == memberCount)
                        {
                            String memberString = "Members: " + memberCount;

                            // If there is secret members
                            if(secretCount != 0)
                            {
                                memberString = memberString + " (" + secretCount + " Secret)";
                            }

                            // Display the amount of members
                            memberCountDisplay.setText(memberString);

                            // Display the members
                            UserGridViewAdapter gridAdapter = new UserGridViewAdapter(MeetupDetailActivity.this, userNames, userPics, userIds);
                            attendeesGrid.setAdapter(gridAdapter);

                            // Load the meetup's picture
                            loadPicture();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        } else
        {
            // If the squad has no members
            attendingDisplay.setText("This Meetup has no one attending!");
            memberCountDisplay.setText("Members: 0");

            // Load the meetup's picture
            loadPicture();
        }
    }

    public void loadPicture()
    {
        // Setting the loading text
        loadingText.setText("Getting the Meetup's picture...");

        // If the meetup has an image display it
        meetupStorage.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                meetupImage.setVisibility(View.VISIBLE);
                meetupImage.setImageBitmap(image);
                loadingImage.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                meetupImage.setVisibility(View.VISIBLE);
                loadingImage.setVisibility(View.GONE);
            }
        });

        // Hiding loading overlay
        loadingOverlay.setVisibility(View.GONE);
    }

    public void deleteMeetupPrompt()
    {
        new AlertDialog.Builder(MeetupDetailActivity.this)
                .setTitle("Delete Meetup")
                .setMessage("Are you sure you want to delete this Meetup?" +
                        "\nYou will not be able to get it back!")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteMeetup();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Do nothing
                    }
                })
                .show();
    }

    public void deleteMeetup()
    {
        // Displaying the loading overlay
        loadingText.setText("Deleting Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Getting the users HashMap
        final HashMap<String, Boolean> users = meetup.getUsers();

        // If the HashMap isnt empty
        if (users != null)
        {
            // Removing the meetup from User's personal lists
            for (String uId : users.keySet())
            {
                mDatabase.child("users").child(uId).child("meetups").child(meetupId).removeValue();
            }
        }

        // Removing the meetup from the user's hosting
        mDatabase.child("users").child(firebaseUser.getUid()).child("hosting").child(meetupId).removeValue();

        // Removing the meetup from the place's meetups
        if(meetup.getPlace() != null)
        {
            mDatabase.child("places").child(meetup.getPlace()).child("meetups").child(meetupId).removeValue();
        }

        // Removing the meetup
        mDatabase.child("meetups").child(meetupId).removeValue();
        finish();
    }

    public void editMode()
    {
        // Enabling the edit ImageButtons
        editName.setEnabled(true);
        editName.setVisibility(View.VISIBLE);
        editName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Load Dialog to edit Name
                editName();
            }
        });

        editDesc.setEnabled(true);
        editDesc.setVisibility(View.VISIBLE);
        editDesc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Load Dialog to edit Description
                editDesc();
            }
        });

        deleteBtn.setEnabled(true);
        deleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Load Dialog to confirm deletion of Meetup
                deleteMeetupPrompt();
            }
        });

        meetupImage.setEnabled(true);
        meetupImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectImage(v);
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
        editTextDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
        builder.setView(editTextDesc);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String desc = editTextDesc.getText().toString();
                descriptionDisplay.setText(desc);
                updateDesc(desc);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
            mDatabase.child("meetups").child(meetupId).child("description").setValue(desc);
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
        editTextName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        builder.setView(editTextName);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String name = editTextName.getText().toString();
                nameDisplay.setText(name);
                updateName(name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
            mDatabase.child("meetups").child(meetupId).child("name").setValue(name);
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void attendButton(View view)
    {
        if (attending)
        {
            // User is in the Squad
            leaveMeetup();
        } else
        {
            // User is not in the squad
            attendMeetup();
        }
    }

    public void attendMeetup()
    {
        // Adds the user to the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("meetups").child(meetupId).setValue(true);
        mDatabase.child("meetups").child(meetupId).child("users").child(firebaseUser.getUid()).setValue(true);
        attending = true;
        attendBtn.setText("Leave Meetup");
    }

    public void leaveMeetup()
    {
        // Removes the user from the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("meetups").child(meetupId).removeValue();
        mDatabase.child("meetups").child(meetupId).child("users").child(firebaseUser.getUid()).removeValue();
        attending = false;
        attendBtn.setText("Attend Meetup");
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If picture selected
        final Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        if(bitmap != null)
        {
            loadingText.setText("Uploading photo...");
            loadingOverlay.setVisibility(View.VISIBLE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            // Upload to Firebase Storage
            UploadTask uploadTask = meetupStorage.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(MeetupDetailActivity.this, "Upload failed, please try again!"
                            , Toast.LENGTH_SHORT);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    meetupImage.setImageBitmap(bitmap);
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(MeetupDetailActivity.this, "Photo uploaded!", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    public void selectImage(View view) {
        // Click on image button
        ImagePicker.pickImage(this, "Select your image:");
    }


}
