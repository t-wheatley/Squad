package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.UserGridViewAdapter;
import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.FBUser;

/**
 * Activity which allows the user to view the details of a Squad.
 */
public class SquadDetailActivity extends BaseActivity
{
    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser firebaseUser;

    // Loading Overlay
    private RelativeLayout loadingOverlay;
    private TextView loadingText;

    // Activity UI
    private TextView nameDisplay;
    private TextView descriptionDisplay;
    private TextView memberCountDisplay;
    private TextView memberDisplay;
    private String squadId;
    private Button joinBtn;
    private ImageView squadImage;
    private ProgressBar imageLoading;
    private TextView meetupCount;
    private TextView placeCount;

    // Members display
    private GridView membersGrid;
    private List<String> userNames;
    private List<String> userPics;
    private List<String> userIds;

    // Variables
    private Squad squad;
    private Boolean member;
    private int secretCount;
    private int memberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Squad...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Initialising UI Elements
        nameDisplay = (TextView) findViewById(R.id.squadDetail_squadName);
        descriptionDisplay = (TextView) findViewById(R.id.squadDetail_description);
        memberDisplay = (TextView) findViewById(R.id.squadDetail_noMembers);
        membersGrid = (GridView) findViewById(R.id.squadDetail_userGrid);
        memberCountDisplay = (TextView) findViewById(R.id.squadDetail_memberCount);
        joinBtn = (Button) findViewById(R.id.squadDetail_joinBtn);
        meetupCount = (TextView) findViewById(R.id.squadDetail_meetupsCount);
        placeCount = (TextView) findViewById(R.id.squadDetail_placesCount);
        imageLoading = (ProgressBar) findViewById(R.id.squadDetail_imageProgress);
        squadImage = (ImageView) findViewById(R.id.squadDetail_image);


        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (b != null)
        {
            // Collects the squadId passed from the RecyclerView
            squadId = (String) b.get("squadId");
            this.setTitle("Squad Details");
        } else
        {
            new AlertDialog.Builder(SquadDetailActivity.this)
                    .setTitle("Error")
                    .setMessage("The squad went missing somewhere, please try again.")
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

        // Getting the reference for the Firebase Realtime Database and Storage
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        // Defaults
        member = false;
        joinBtn.setText("Join");
        secretCount = 0;
        memberCount = 0;

        // Starts the loading chain
        // loadSquad -> loadPicture -> loadUsers
        loadSquad();
    }

    @Override
    int getContentViewId()
    {
        return R.layout.activity_squad_detail;
    }

    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_squads;
    }

    /**
     * Uses the squadId to create a Squad object and display its details.
     */
    public void loadSquad()
    {
        // Reads the data from the squadId in Firebase
        mDatabase.child("squads").child(squadId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Squad class
                squad = dataSnapshot.getValue(Squad.class);

                // Displays the found squad's attributes
                nameDisplay.setText(squad.getName());
                descriptionDisplay.setText(squad.getDescription());

                // Getting the placeCount
                if(squad.getPlaces() != null)
                {
                    placeCount.setText(squad.getPlaces().size() + "");

                } else
                {
                    placeCount.setText("0");
                }

                // Getting the meetupCount
                if(squad.getMeetups() != null)
                {
                    meetupCount.setText(squad.getMeetups().size() + "");

                } else
                {
                    meetupCount.setText("0");
                }

                // Load the Picture of the Squad
                loadPicture();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void loadPicture()
    {
        // Gets the storage reference of the Squad's picture
        StorageReference squadStorage = firebaseStorage.getReference().child("squads/" + squadId + ".png");

        // Display picture loading
        imageLoading.setVisibility(View.VISIBLE);

        // Gets the Uri to download
        squadStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                // If a picture exists
                // Download and display using Glide
                Glide.with(getApplicationContext())
                        .load(uri)
                        .listener(new RequestListener<Uri, GlideDrawable>()
                        {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource)
                            {
                                imageLoading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                            {
                                imageLoading.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .error(R.drawable.ic_donut_minim)
                        .into(squadImage);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // If no picture exists
                imageLoading.setVisibility(View.GONE);
            }
        });

        // Load the members of the Squad
        loadUsers();
    }

    /**
     * Method to load the Users in the Squad and display them in a GridView.
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
        loadingText.setText("Getting the Squad's members...");

        // Getting the users HashMap
        HashMap<String, Boolean> users = squad.getUsers();

        // If the HashMap isnt empty
        if (users != null)
        {
            // Changing the loading text
            loadingText.setText("Getting the Squad's members...");

            // Getting the amount of users
            final int usersSize = users.size();

            // Checking if the user is already in the Squad
            if (users.containsKey(firebaseUser.getUid()))
            {
                member = true;
                joinBtn.setText("Leave");
            }

            // Displaying members of the Squad
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

                        memberCount++;
                        // If all members added
                        if (usersSize == memberCount)
                        {
                            String memberString = "" + memberCount;

                            // If there is secret members
                            if (secretCount != 0)
                            {
                                memberString = memberString + " (" + secretCount + " Secret)";
                            }

                            // Display the amount of members
                            memberCountDisplay.setText(memberString);

                            // Display the members
                            UserGridViewAdapter gridAdapter = new UserGridViewAdapter(SquadDetailActivity.this, userNames, userPics, userIds);
                            membersGrid.setAdapter(gridAdapter);

                            // Hiding loading overlay
                            loadingOverlay.setVisibility(View.GONE);
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
            memberCountDisplay.setText("0");

            // Hiding loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    /**
     * Method to let the User join or leave the Squad.
     *
     * @param view The button that was pressed.
     */
    public void squadButton(View view)
    {
        if (member)
        {
            // User is in the Squad
            leaveSquad();
        } else
        {
            // User is not in the squad
            joinSquad();
        }
    }

    /**
     * Method to add the User to the Squad.
     */
    public void joinSquad()
    {
        // Adds the user to the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("squads").child(squadId).setValue(true);
        mDatabase.child("squads").child(squadId).child("users").child(firebaseUser.getUid()).setValue(true);
        member = true;
        joinBtn.setText("Leave");
    }

    /**
     * Method to remove the User from the Squad.
     */
    public void leaveSquad()
    {
        // Removes the user from the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("squads").child(squadId).removeValue();
        mDatabase.child("squads").child(squadId).child("users").child(firebaseUser.getUid()).removeValue();
        member = false;
        joinBtn.setText("Join");
        finish();
    }

    /**
     * Method to load the MeetupsListActivity displaying the Meetups belonging to this Squad.
     *
     * @param view The button that was pressed.
     */
    public void viewMeetups(View view)
    {
        Intent intent = new Intent(this, MeetupsListActivity.class);
        intent.putExtra("squadId", squadId);
        startActivity(intent);
    }

    /**
     * Method to load the PlacesListActivity displaying the Places belonging to this Squad.
     *
     * @param view The button that was pressed.
     */
    public void viewPlaces(View view)
    {
        Intent intent = new Intent(this, PlacesListActivity.class);
        intent.putExtra("squadId", squadId);
        startActivity(intent);
    }

    /**
     * Method to load the SquadPostActivity displaying the Posts belonging to this Squad.
     *
     * @param view The button that was pressed.
     */
    public void openPost(View view)
    {
        Intent intent = new Intent(this, SquadPostActivity.class);
        intent.putExtra("squadId", squadId);
        startActivity(intent);
    }
}
