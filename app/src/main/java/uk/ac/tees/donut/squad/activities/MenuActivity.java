package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.debug.SquadDebug;
import uk.ac.tees.donut.squad.location.GeocoderActivity;
import uk.ac.tees.donut.squad.location.MapActivity;
import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.User;

public class MenuActivity extends AppCompatActivity {
    boolean firstStart = true;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if(firstStart)
        {
            user = new User("Default User");
            firstStart = false;

            User.addMeetup("-Kg3OkIfWwS8YXCi6vF4");
        }

        if (getIntent().getBooleanExtra("EXIT", false))
        {
            Intent intent = new Intent(this, LoginActivity.class);
            finish();

            startActivity(intent);
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("squads");

        mDatabase.orderByChild("users/" + "Gk3J3QMMT9OOnn6ytN23DpIwMKQ2").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Squad squad = messageSnapshot.getValue(Squad.class);
                    squad.getName();
                }

                Intent detail = new Intent(MenuActivity.this, SquadDetailActivity.class);
                //detail.putExtra("squadId", squad.getId());
                //startActivity(detail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }

    //Click functionality
    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openSquads(View view) {
        Intent intent = new Intent(this, SquadListActivity.class);
        startActivity(intent);
    }

    public void openEvents(View view) {
        Intent intent = new Intent(this, MeetupsListActivity.class);
        startActivity(intent);
    }

    public void openHost(View view) {
        Intent intent = new Intent(this, NewMeetupActivity.class);
        startActivity(intent);
    }

    public void openNewPlace(View view)
    {
        Intent intent = new Intent(this, NewPlaceActivity.class);
        startActivity(intent);
    }

    public void openPlaces(View view)
    {
        Intent intent = new Intent(this, PlacesListActivity.class);
        startActivity(intent);
    }

    public void openLocation(View view) {
        Intent intent = new Intent (this, GeocoderActivity.class);
        startActivity(intent);
    }

    public void openMap(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    //USE THIS WHEN YOU NEED TO DEBUG OR TEST CERTAIN THINGS WITHOUT DESTROYING THE REST OF THE APP
    public void openMystery(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
