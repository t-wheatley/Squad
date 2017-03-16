package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.User;
import android.content.Intent;

import static uk.ac.tees.donut.squad.squads.Interest.*;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TEST USER WITH MOVIES IN THEIR mySquads
        User user = new User("defaultUser");
        Squad movies = new Squad("Movie Squad", MOVIES, "A collection of avid movie-goers");
//        user.addInterest(MOVIES);
//        user.setName("James");
//        user.setHostable(true);
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
