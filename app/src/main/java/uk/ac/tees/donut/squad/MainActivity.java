package uk.ac.q5081793tees.squads;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import uk.ac.q5081793tees.squads.database.DatabaseHandler;
import uk.ac.q5081793tees.squads.users.User;


import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.User;

import static uk.ac.tees.donut.squad.squads.Interest.*;

public class MainActivity extends AppCompatActivity
{

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        User user = new User();
        //Manually set for test data
        user.setName("James");
        user.setHostable(true);
    }
    //Click functionality
    public void openProfile(View view)
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    public void openSquads(View view)
    {
        Intent intent = new Intent(this, SquadsActivity.class);
        startActivity(intent);
    }
    public void openEvents(View view)
    {
        Intent intent = new Intent(this, NearbyActivity.class);
        startActivity(intent);
    }
    public void openHost(View view)
    {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }
    public void openSettings(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
