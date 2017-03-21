package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import uk.ac.tees.donut.squad.squads.Interest;
import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.User;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static uk.ac.tees.donut.squad.squads.Interest.*;

public class MainActivity extends AppCompatActivity
{
    Button btnNewMeetup;
    Button btnViewMeetups;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        Intent intent = new Intent(this, ViewMeetups.class);
        startActivity(intent);
    }
    public void openHost(View view)
    {
        Intent intent = new Intent(this, NewMeetup.class);
        startActivity(intent);
    }
    public void openSettings(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
