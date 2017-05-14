package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.donut.squad.R;


/**
 * Activity which displays all the features of the application to the User.
 */
public class MenuActivity extends BaseActivity
{
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Gets the signed-in User
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    int getContentViewId()
    {
        return R.layout.activity_map;
    }

    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_map;
    }

    /**
     * Method to send the User to their ProfileActivity.
     *
     * @param view The profile button.
     */
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

    /**
     * Method to send the User to the SquadListActivity, to view Squads.
     *
     * @param view The squads button.
     */
    public void openSquads(View view)
    {
        Intent intent = new Intent(this, SquadListActivity.class);
        System.out.println("------ Opened Squads ------");
        startActivity(intent);
    }

    /**
     * Method to send the User to the MeetupsListActivity, to view Meetups.
     *
     * @param view The events button.
     */
    public void openEvents(View view)
    {
        Intent intent = new Intent(this, MeetupsListActivity.class);
        System.out.println("------ Opened Meetups ------");
        startActivity(intent);
    }

    /**
     * Method to send the User to the NewMeetupActivity, to create a new Meetup.
     *
     * @param view The host button.
     */
    public void openHost(View view)
    {
        Intent intent = new Intent(this, NewMeetupActivity.class);
        System.out.println("------ Opened New Meetup ------");
        startActivity(intent);
    }

    /**
     * Method to send the User to the NewPlaceActivity, to create a new Place.
     *
     * @param view The new place button.
     */
    public void openNewPlace(View view)
    {
        Intent intent = new Intent(this, NewPlaceActivity.class);
        startActivity(intent);
    }

    /**
     * Method to send the User to the PlacesListActivity, to view Places.
     *
     * @param view places host button.
     */
    public void openPlaces(View view)
    {
        Intent intent = new Intent(this, PlacesListActivity.class);
        startActivity(intent);
    }

    /**
     * Method to send the User to the MapActivity, to view nearby Meetups.
     *
     * @param view The map button.
     */
    public void openMap(View view)
    {
        Intent detail = new Intent(MenuActivity.this, MapActivity.class);
        detail.putExtra("uId", firebaseUser.getUid());
        startActivity(detail);
    }

    // USE THIS WHEN YOU NEED TO DEBUG OR TEST CERTAIN THINGS WITHOUT DESTROYING THE REST OF THE APP
    public void openMystery(Bundle savedInstanceState)
    {
        //Intent intent = new Intent(this, BaseActivity.class);
        //startActivity(intent);
    }
}
