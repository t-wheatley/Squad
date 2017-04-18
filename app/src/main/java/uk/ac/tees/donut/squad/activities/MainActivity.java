package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.location.GeocoderActivity;
import uk.ac.tees.donut.squad.location.MapActivity;
import uk.ac.tees.donut.squad.users.User;

public class MainActivity extends AppCompatActivity {
    boolean firstStart = true;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(firstStart)
        {
            user = new User("Default User");
            firstStart = false;

            User.addMeetup("-Kg3OkIfWwS8YXCi6vF4");
        }

        if (getIntent().getBooleanExtra("EXIT", false))
        {
            Intent intent = new Intent(this, SplashScreen.class);
            finish();

            startActivity(intent);
        }


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
        Intent intent = new Intent (this, GeocoderActivity.class);
        startActivity(intent);

    }

    public void openMap(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

}
