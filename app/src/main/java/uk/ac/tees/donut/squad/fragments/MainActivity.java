package uk.ac.tees.donut.squad.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.activities.MeetupsListActivity;
import uk.ac.tees.donut.squad.activities.MenuActivity;
import uk.ac.tees.donut.squad.activities.ProfileActivity;
import uk.ac.tees.donut.squad.activities.SquadListActivity;
import uk.ac.tees.donut.squad.location.GeocoderActivity;


public class MainActivity extends AppCompatActivity {
    private static final String SELECTED_ITEM = "arg_selected_item";

    private BottomNavigationView mBottomNav;
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_home:
                System.out.println("-------Selected: Home Fragment-------");
                //fragmentManager(new HomeFragment());
                intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_squads:
                System.out.println("-------Selected: Squads Fragment-------");
                //fragmentManager(new SquadFragment());
                intent = new Intent(this, SquadListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_meetups:
                System.out.println("-------Selected: Meetups Fragment-------");
                //fragmentManager(new MeetupFragment());
                intent = new Intent(this, MeetupsListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_profile:
                System.out.println("-------Selected: Profile Fragment-------");
                //fragmentManager(new ProfileFragment());
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
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
