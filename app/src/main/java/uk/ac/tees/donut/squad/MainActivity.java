package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import uk.ac.tees.donut.squad.location.LocationActivity;
import uk.ac.tees.donut.squad.location.MapActivity;
import uk.ac.tees.donut.squad.users.User;

public class MainActivity extends AppCompatActivity {
    Button btnNewMeetup;
    Button btnViewMeetups;
    boolean firstStart = true;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(firstStart) {
            user = new User("Default User");
            firstStart = false;

            User.addMeetup("-Kg3OkIfWwS8YXCi6vF4");
        }
      
        //find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set the toolbar as an actionbar
        setSupportActionBar(toolbar);

        //Create a tab layout with each tab info
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Squads"));
        tabLayout.addTab(tabLayout.newTab().setText("Events"));
        tabLayout.addTab(tabLayout.newTab().setText("My Profile"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //viewpager handles swiping in the two directions
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        //pageradapter manages the individual pages
        final PagerAdapter adapter = new FragmentsAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                viewPager.setCurrentItem(tab.getPosition());
                Log.i("TAG", "onTabSelected: " + tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab){
                Log.i("TAG", "onTabReselected: " + tab.getPosition());
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab){
                Log.i("TAG", "onTabReselected: " + tab.getPosition());
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }

    //Click functionality
    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openSquads(View view) {
        Intent intent = new Intent(this, SquadsActivity.class);
        startActivity(intent);
    }

    public void openEvents(View view) {
        Intent intent = new Intent(this, ViewMeetups.class);
        startActivity(intent);
    }

    public void openHost(View view) {
        Intent intent = new Intent(this, NewMeetup.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openLocation(View view) {
    Intent intent = new Intent (this, LocationActivity.class);
        startActivity(intent);

    }

    public void openMap(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
