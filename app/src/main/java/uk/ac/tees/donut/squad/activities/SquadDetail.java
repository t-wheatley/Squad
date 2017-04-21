package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.squads.Squad;

public class SquadDetail extends AppCompatActivity {

    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    Squad squad;
    Boolean member;

    TextView nameDisplay;
    TextView descriptionDisplay;
    String squadId;

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
            new AlertDialog.Builder(SquadDetail.this)
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
        mDatabase = FirebaseDatabase.getInstance().getReference("squads");

        member = false;
        joinBtn.setText("Join Squad");

        // Loads the data for the Squad from Firebase
        loadSquad();
    }

    public void loadSquad()
    {
        if(mDatabase.child(squadId).child("users").child(firebaseUser.getUid()).equals(true))
        {
            member = true;
            joinBtn.setText("Leave Squad");
        }

        // Reads the data from the meetupId in Firebase
        mDatabase.child(squadId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Gets the data from Firebase and stores it in a Squad class
                squad = dataSnapshot.getValue(Squad.class);

                // Displays the found squad's attributes
                nameDisplay.setText(squad.getName());
                descriptionDisplay.setText(squad.getDescription());

                // Hiding loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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
        mDatabase.child(squadId).child("users").child(firebaseUser.getUid()).setValue(true);
        member = true;
        joinBtn.setText("Leave Squad");
    }

    public void leaveSquad()
    {
        mDatabase.child(squadId).child("users").child(firebaseUser.getUid()).setValue(false);
        member = false;
        joinBtn.setText("Join Squad");
    }
}
