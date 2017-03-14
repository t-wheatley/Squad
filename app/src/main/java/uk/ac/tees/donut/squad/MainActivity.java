package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import uk.ac.tees.donut.squad.squads.Squad;
import uk.ac.tees.donut.squad.users.User;

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
        user.addInterest(MOVIES);
    }
}
