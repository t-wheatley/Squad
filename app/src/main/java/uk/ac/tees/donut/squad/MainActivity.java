package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import uk.ac.tees.donut.squad.location.LocationActivity;
import uk.ac.tees.donut.squad.users.User;

public class MainActivity extends AppCompatActivity {
    Button btnNewMeetup;
    Button btnViewMeetups;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = new User("defaultUser");
        user.addMeetup("-KfRv3Q8wXywzAT1mFy-");

    }

    //Click functionality
    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("USER", user);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void openSquads(View view) {
        Intent intent = new Intent(this, SquadsActivity.class);
        startActivity(intent);
    }

    public void openEvents(View view) {
        Intent intent = new Intent(this, ViewMeetups.class);
        Bundle b = new Bundle();
        b.putSerializable("USER", user);
        intent.putExtras(b);
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
    public void openSignIn (View view)
    {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
