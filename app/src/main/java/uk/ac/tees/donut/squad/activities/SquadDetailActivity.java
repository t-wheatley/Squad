package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import uk.ac.tees.donut.squad.UserGridViewAdapter;
import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.FBUser;

public class SquadDetailActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    Squad squad;
    Boolean member;

    TextView nameDisplay;
    TextView descriptionDisplay;
    TextView memberDisplay;
    String squadId;

    // Members display
    GridView membersGrid;
    List<String> userNames;
    List<String> userPics;
    List<String> userIds;

    int memberCount;

    Button joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squad_detail);


        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Squad...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Declaring everything
        nameDisplay = (TextView) findViewById(R.id.squadDetail_textEditName);
        descriptionDisplay = (TextView) findViewById(R.id.squadDetail_textEditDescription);
        memberDisplay = (TextView) findViewById(R.id.squadDetail_textEditMembers);
        membersGrid = (GridView)findViewById(R.id.squadDetail_userGrid);
        joinBtn = (Button) findViewById(R.id.squadDetail_joinBtn);

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(b != null)
        {
            // Collects the squadId passed from the RecyclerView
            squadId = (String) b.get("squadId");
            this.setTitle("Squad Details");
        } else
        {
            new AlertDialog.Builder(SquadDetailActivity.this)
                    .setTitle("Error")
                    .setMessage("The squad went missing somewhere, please try again.")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Defaults
        member = false;
        joinBtn.setText("Join Squad");
        memberCount = 0;

        // Starts the loading chain
        // loadSquad -> loadUsers
        loadSquad();
    }

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

                // Load the members of the Squad
                loadUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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
            if(users.containsKey(firebaseUser.getUid()))
            {
                member = true;
                joinBtn.setText("Leave Squad");
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
                        userNames.add(user.getName());
                        userPics.add(user.getPicture());
                        userIds.add(uId);


                        memberCount++;
                        // If all members added
                        if(usersSize == memberCount)
                        {
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
            memberDisplay.setText("This Squad has no members yet!");

            // Hiding loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    public void squadButton(View view)
    {
        if(member)
        {
            // User is in the Squad
            leaveSquad();
        }
        else
        {
            // User is not in the squad
            joinSquad();
        }
    }

    public void joinSquad()
    {
        // Adds the user to the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("squads").child(squadId).setValue(true);
        mDatabase.child("squads").child(squadId).child("users").child(firebaseUser.getUid()).setValue(true);
        member = true;
        joinBtn.setText("Leave Squad");
    }

    public void leaveSquad()
    {
        // Removes the user from the squad and changes the button
        mDatabase.child("users").child(firebaseUser.getUid()).child("squads").child(squadId).removeValue();
        mDatabase.child("squads").child(squadId).child("users").child(firebaseUser.getUid()).removeValue();
        member = false;
        joinBtn.setText("Join Squad");
    }
}
