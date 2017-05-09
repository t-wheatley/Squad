package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.debug.SquadDebug;
import uk.ac.tees.donut.squad.location.GeocoderActivity;
import uk.ac.tees.donut.squad.location.MapActivity;

public class MenuActivity extends AppCompatActivity
{
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    //Click functionality
    public void openProfile(View view)
    {
        if (firebaseUser != null)
        {
            //Sends the user's id to the profile activity
            Intent detail = new Intent(MenuActivity.this, ProfileActivity.class);
            detail.putExtra("uId", firebaseUser.getUid());
            startActivity(detail);
        }
    }

    public void openSquads(View view)
    {
        Intent intent = new Intent(this, SquadListActivity.class);
        startActivity(intent);
    }

    public void openEvents(View view)
    {
        Intent intent = new Intent(this, MeetupsListActivity.class);
        startActivity(intent);
    }

    public void openHost(View view)
    {
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

    public void openLocation(View view)
    {
        Intent intent = new Intent(this, GeocoderActivity.class);
        startActivity(intent);
    }

    public void openMap(View view)
    {
        Intent detail = new Intent(MenuActivity.this, MapActivity.class);
        detail.putExtra("uId", firebaseUser.getUid());
        startActivity(detail);
    }

    // USE THIS WHEN YOU NEED TO DEBUG OR TEST CERTAIN THINGS WITHOUT DESTROYING THE REST OF THE APP
    public void openMystery(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
