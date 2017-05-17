package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

/**
 * Activity which allows the user to view the details of a Meetup.
 */
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
    ImageView meetupImageFull;
    Button editName;
    Button editDesc;
    Button editPhoto;
    Button attendBtn;
    Button deleteBtn;
    CardView imageViewCard;
    TextView addressDisplay;

    //Burger Menu
    FloatingActionButton fab;
    RelativeLayout burgerMenu;
    LinearLayout hostOptions;
    boolean burger = false;

    // Members display
    GridView attendeesGrid;
    List<String> userNames;
    List<String> userPics;
    List<String> userIds;

    // Variables
    String meetupId;
    Meetup meetup;
    Boolean attending;
    int secretCount;
    int memberCount;
    double latitude;
    double longitude;
    boolean noPhoto;
    boolean fullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingImage = (ProgressBar) findViewById(R.id.meetupDetail_imageProgress);
        loadingText.setText("Loading Meetup...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingImage.setVisibility(View.VISIBLE);

        // Initialising UI Elements
        nameDisplay = (TextView) findViewById(R.id.meetupDetail_meetupName);
        squadDisplay = (TextView) findViewById(R.id.meetupDetail_squadName);
        hostDisplay = (TextView) findViewById(R.id.meetupDetail_host);
        descriptionDisplay = (TextView) findViewById(R.id.meetupDetail_description);
        statusDisplay = (TextView) findViewById(R.id.meetupDetail_status);
        startDateDisplay = (TextView) findViewById(R.id.meetupDetail_startDate);
        endDateDisplay = (TextView) findViewById(R.id.meetupDetail_endDate);
        attendeesGrid = (GridView) findViewById(R.id.meetupDetail_userGrid);
        attendBtn = (Button) findViewById(R.id.meetupDetail_attendBtn);
        deleteBtn = (Button) findViewById(R.id.meetupDetail_deleteBtn);
        editName = (Button) findViewById(R.id.meetupDetail_editNameBtn);
        editDesc = (Button) findViewById(R.id.meetupDetail_editDescriptionBtn);
        editPhoto = (Button) findViewById(R.id.meetupDetail_editPhotoBtn);
        attendingDisplay = (TextView) findViewById(R.id.meetupDetail_noAttendees);
        memberCountDisplay = (TextView) findViewById(R.id.meetupDetail_attendeeCount);
        fab = (FloatingActionButton) findViewById(R.id.meetupDetail_fab);
        burgerMenu = (RelativeLayout) findViewById(R.id.meetupDetail_burgerMenu);
        hostOptions = (LinearLayout) findViewById(R.id.meetupDetail_hostBurgerMenu);
        imageViewCard = (CardView) findViewById(R.id.meetupDetail_ImageViewCard);
        addressDisplay = (TextView) findViewById(R.id.meetupDetail_address);
        meetupImageFull = (ImageView) findViewById(R.id.meetupDetail_imageFullScreen);
        meetupImageFull.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fullScreen = false;

                meetupImageFull.setVisibility(View.GONE);
            }
        });
        meetupImage = (ImageView) findViewById(R.id.meetupDetail_ImageView);
        meetupImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fullScreen = true;

                meetupImageFull.setVisibility(View.VISIBLE);
            }
        });

        // Disabling the edit ImageButtons and delete Button
        hostOptions.setVisibility(View.GONE);

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
        attendBtn.setText("Attend");
        secretCount = 0;
        memberCount = 0;

        // Starts the loading chain
        // loadMeetup -> loadSquad -> loadHost -> loadUsers
        loadMeetup();
    }

    @Override
    int getContentViewId()
    {
        return R.layout.activity_meetup_detail;
    }

    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_meetups;
    }

    @Override
    public void onBackPressed()
    {
        // If displaying a fullscreen image
        if(fullScreen)
        {
            // Hide the fullscreen
            fullScreen = false;
            meetupImageFull.setVisibility(View.GONE);
        } else if (burger)
        { // If burger menu is open
            // Close the burger menu
            fab(fab);
        } else
        {
            // Close the activity
            MeetupDetailActivity.this.finish();
        }
    }

    /**
     * Uses the meetupId to create a Meetup object and display its details.
     */
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

                // Displays the found Meetup's attributes
                nameDisplay.setText(meetup.getName());
                descriptionDisplay.setText(meetup.getDescription());
                addressDisplay.setText(meetup.fullAddress());

                //get longitude and latitude of meetup
                latitude = meetup.getLatitude();
                longitude = meetup.getLongitude();

                // Gets the start and end date of the Meetup
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                String startDate = sdf.format(meetup.getStartDateTime() * 1000L);
                String endDate = sdf.format(meetup.getEndDateTime() * 1000L);
                startDateDisplay.setText(startDate);
                endDateDisplay.setText(endDate);

                // Checking if the Meetup has changed state
                meetup.updateStatus();

                // Displaying the state of the Meetup
                int status = meetup.gimmeStatus();
                if (status == 0)
                    statusDisplay.setText("Upcoming");
                else if (status == 1)
                    statusDisplay.setText("Ongoing");
                else if (status == 2)
                    statusDisplay.setText("Expired");
                else
                    statusDisplay.setText("Deleted");

                // If signed-in user is the Host of the Meetup
                if (firebaseUser.getUid().equals(meetup.getHost()))
                {
                    // Display editing controls
                    editMode();
                }

                // Creating the reference for the meetup's Firebase Storage, used to store pictures
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

    /**
     * Uses the squadId of the Meetup to load the name of the Squad it belongs to.
     */
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
                // Displays the Squad's name
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

    /**
     * Method to load the SquadDetailActivity of the selected squad.
     *
     * @param view The TextEdit holding the Squad's name.
     */
    public void viewSquad(View view)
    {
        //Sends the id to the SquadDetailActivity
        Intent detail = new Intent(MeetupDetailActivity.this, SquadDetailActivity.class);
        detail.putExtra("squadId", meetup.getSquad());
        startActivity(detail);
    }

    /**
     * Uses the hostId of the Meetup to load the name of the Host it belongs to.
     */
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
                // Displays the Host's name
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

    /**
     * Method to load the ProfileActivity of the Host.
     *
     * @param view The TextEdit holding the Host's name.
     */
    public void viewHost(View view)
    {
        //Sends the id to the ProfileActivity
        Intent detail = new Intent(MeetupDetailActivity.this, ProfileActivity.class);
        detail.putExtra("uId", meetup.getHost());
        startActivity(detail);
    }

    /**
     * Method to load the Users attending the Meetup and display them in a GridView.
     */
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
                attendBtn.setText("Unattend");
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
                        if (user.getSecret() == null || user.getSecret() == false)
                        {
                            userNames.add(user.getName());
                            userPics.add(user.getPicture());
                            userIds.add(uId);
                        } else
                        {
                            secretCount++;
                        }

                        // Incrementing the memberCount
                        memberCount++;

                        // If all members have been added
                        if (usersSize == memberCount)
                        {
                            // Get the amount of members
                            String memberString = "" + memberCount;

                            // If there is secret members
                            if (secretCount != 0)
                            {
                                // Get the amount of secret members
                                memberString = memberString + " (" + secretCount + " Secret)";
                            }

                            // Display the amount of members
                            memberCountDisplay.setText(memberString);

                            // Display the members in the GridView
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
            attendingDisplay.setVisibility(View.VISIBLE);
            memberCountDisplay.setText("0");

            // Load the meetup's picture
            loadPicture();
        }
    }

    /**
     * Method to load the picture of the Meetup and display it in an ImageView.
     */
    public void loadPicture()
    {
        // Setting the loading text
        loadingText.setText("Getting the Meetup's picture...");
        loadingImage.setVisibility(View.VISIBLE);

        // Gets the Uri to download
        meetupStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // If a picture exist
                imageViewCard.setVisibility(View.VISIBLE);

                // Download and display using Glide
                Glide.with(MeetupDetailActivity.this)
                        .load(uri)
                        .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(meetupImage);

                // Also to the fullscreen
                Glide.with(MeetupDetailActivity.this)
                        .load(uri)
                        .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(meetupImageFull);

                loadingImage.setVisibility(View.GONE);
                noPhoto = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // If no picture exists
                imageViewCard.setVisibility(View.GONE);
                noPhoto = true;
            }
        });

        // Hiding loading overlay
        loadingOverlay.setVisibility(View.GONE);
    }


    /**
     * Method to display an AlertDialog warning the user they are about to delete the Meetup.
     */
    public void deleteMeetupPrompt()
    {
        // Display AlertDialog
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

    /**
     * Method to delete the Meetup from the Firebase Database.
     */
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
        if (meetup.getPlace() != null)
        {
            mDatabase.child("places").child(meetup.getPlace()).child("meetups").child(meetupId).removeValue();
        }

        // Removing the meetup
        mDatabase.child("meetups").child(meetupId).removeValue();
        finish();
    }

    /**
     * Method that displays editing controls for the Meetup if the signed-in User is the Host.
     */
    public void editMode()
    {
        hostOptions.setVisibility(View.VISIBLE);

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

        editPhoto.setEnabled(true);
        editPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Select and image for the Meetup
                selectImage(v);
            }
        });
    }

    /**
     * Method used to edit the Meetup's description. Displays an AlertDialog asking for input then
     * calls the updateDesc method with the new description.
     */
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

    /**
     * Method used to post the new description for the Meetup to the FirebaseDatabase.
     *
     * @param desc The new description to be posted.
     */
    public void updateDesc(String desc)
    {
        // Closing the burgerMenu
        fab(fab);

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

    /**
     * Method used to edit the Meetup's name. Displays an AlertDialog asking for input then
     * calls the updateName method with the new name.
     */
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

    /**
     * Method used to post the new name for the Meetup to the FirebaseDatabase.
     *
     * @param name The new name to be posted.
     */
    public void updateName(String name)
    {
        // Closing the burgerMenu
        fab(fab);

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

    /**
     * Method to make a User attend a Meetup.
     */
    public void attendMeetup()
    {
        // Adds the user to the Meetup and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("meetups").child(meetupId).setValue(true);
        mDatabase.child("meetups").child(meetupId).child("users").child(firebaseUser.getUid()).setValue(true);
        attending = true;
        attendBtn.setText("Un-Attend");
    }

    /**
     * Method to make a User leave a Meetup.
     */
    public void leaveMeetup()
    {
        // Removes the user from the Meetup and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("meetups").child(meetupId).removeValue();
        mDatabase.child("meetups").child(meetupId).child("users").child(firebaseUser.getUid()).removeValue();
        attending = false;
        attendBtn.setText("Attend");
        finish();
    }

    /**
     * Method called on result of the ImagePicker Intent.
     *
     * @param requestCode The request code that was passed to startActivityForResult().
     * @param resultCode  The result code, RESULT_OK if successful, RESULT_CANCELED if not.
     * @param data        An Intent that carries the data of the result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Getting a bitmap from the Intent
        final Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);

        // If Bitmap exists
        if (bitmap != null)
        {
            loadingText.setText("Uploading photo...");
            loadingOverlay.setVisibility(View.VISIBLE);

            // Creating a ByteArray from the bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            // Upload to Firebase Storage
            UploadTask uploadTask = meetupStorage.putBytes(bytes);

            // Upload Listener
            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                    // If upload failed
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(MeetupDetailActivity.this, "Upload failed, please try again!"
                            , Toast.LENGTH_SHORT);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    // If upload successful
                    // Close the burger menu
                    fab(fab);

                    // Notifying the user and displaying the new image
                    meetupImage.setImageBitmap(bitmap);
                    meetupImageFull.setImageBitmap(bitmap);
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(MeetupDetailActivity.this, "Photo uploaded!", Toast.LENGTH_SHORT);

                    // If not photo previously, display the cardview
                    if(noPhoto = true)
                    {
                        noPhoto = false;
                        imageViewCard.setVisibility(View.VISIBLE);
                        loadingImage.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    /**
     * Method that displays an ImagePicker intent.
     *
     * @param view The ImageView holding the Meetup's picture.
     */
    public void selectImage(View view)
    {
        // Click on image button
        ImagePicker.pickImage(this, "Select your image:");
    }


    /**
     * Method to open and close the burger menu when the FloatingActionButton is pressed.
     *
     * @param view The FloatingActionButton that was pressed.
     */
    public void fab(View view)
    {
        if (burger == false)
        {
            burger = true;
            fab.setImageResource(R.drawable.ic_cross);
            burgerMenu.setVisibility(View.VISIBLE);
        } else
        {
            burger = false;
            fab.setImageResource(R.drawable.ic_burger);
            burgerMenu.setVisibility(View.GONE);
        }
    }

    public void mapLocation(View view)
    {
        Intent newDetail = new Intent(MeetupDetailActivity.this, PlaceMapsActivity.class);
        newDetail.putExtra("latitude", latitude);
        newDetail.putExtra("longitude", longitude);
        newDetail.putExtra("placeName", nameDisplay.getText().toString());
        newDetail.putExtra("placeDescription", descriptionDisplay.getText().toString());
        startActivity(newDetail);
    }
}
