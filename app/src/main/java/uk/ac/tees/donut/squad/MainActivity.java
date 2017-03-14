package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import uk.ac.tees.donut.squad.database.DatabaseHandler;
import uk.ac.tees.donut.squad.database.FirebaseDB;
import uk.ac.tees.donut.squad.posts.Meetup;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHandler dbh = new DatabaseHandler(this);
        FirebaseDB firebaseDB = new FirebaseDB();

        //Meetup meetup = new Meetup("TestName2", "TestInterest2");
        //firebaseDB.createMeetup(meetup);

        List<Meetup> meetupList = firebaseDB.getMeetups();
        Log.d("List size: ", Integer.toString(meetupList.size()));
        Log.d("0 name: ", meetupList.get(0).getName());
        Log.d("1 name: ", meetupList.get(1).getName());
        Log.d("2 name: ", meetupList.get(2).getName());
    }
}
