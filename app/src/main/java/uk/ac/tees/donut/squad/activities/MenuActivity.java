package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD:app/src/main/java/uk/ac/tees/donut/squad/MainActivity.java
=======
import android.support.v7.app.AppCompatActivity;
>>>>>>> master:app/src/main/java/uk/ac/tees/donut/squad/activities/MenuActivity.java
import android.view.View;

<<<<<<< HEAD:app/src/main/java/uk/ac/tees/donut/squad/MainActivity.java
import uk.ac.tees.donut.squad.location.GeocoderActivity;
=======
import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.location.LocationActivity;
>>>>>>> master:app/src/main/java/uk/ac/tees/donut/squad/activities/MenuActivity.java
import uk.ac.tees.donut.squad.location.MapActivity;

public class MenuActivity extends AppCompatActivity {
    boolean firstStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
