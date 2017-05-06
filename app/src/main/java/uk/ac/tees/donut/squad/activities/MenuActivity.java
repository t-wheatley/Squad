package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.debug.SquadDebug;
import uk.ac.tees.donut.squad.fragments.HomeFragment;
import uk.ac.tees.donut.squad.fragments.MeetupFragment;
import uk.ac.tees.donut.squad.fragments.ProfileFragment;
import uk.ac.tees.donut.squad.fragments.SquadFragment;
import uk.ac.tees.donut.squad.location.GeocoderActivity;
import uk.ac.tees.donut.squad.location.MapActivity;

public class MenuActivity extends AppCompatActivity
{
    FirebaseUser firebaseUser;
    private static final String SELECTED_ITEM = "arg_selected_item";

    private BottomNavigationView mBottomNav;
    private int mSelectedItem;
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
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    // USE THIS WHEN YOU NEED TO DEBUG OR TEST CERTAIN THINGS WITHOUT DESTROYING THE REST OF THE APP
    public void openMystery(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNav = (BottomNavigationView) findViewById(R.id.nav_bar);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = mBottomNav.getMenu().getItem(0);
        }
        selectFragment(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = mBottomNav.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            // select home item
            selectFragment(homeItem);
        } else {
            super.onBackPressed();
        }
    }
    private void fragmentManager(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (ft != null) {
                ft.replace(R.id.container, fragment);
                ft.commit();
            }
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.menu_home:
                System.out.println("-------Selected: Home Fragment-------");
                fragmentManager(new HomeFragment());
                break;
            case R.id.menu_squads:
                System.out.println("-------Selected: Squads Fragment-------");
                fragmentManager(new SquadFragment());
                break;
            case R.id.menu_meetups:
                System.out.println("-------Selected: Meetups Fragment-------");
                fragmentManager(new MeetupFragment());
                break;
            case R.id.menu_profile:
                System.out.println("-------Selected: Profile Fragment-------");
                fragmentManager(new ProfileFragment());
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        // uncheck the other items.
        for (int i = 0; i< mBottomNav.getMenu().size(); i++) {
            MenuItem menuItem = mBottomNav.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == item.getItemId());
        }

        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, frag, frag.getTag());
            ft.commit();
        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }
}
