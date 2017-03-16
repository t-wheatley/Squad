package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import uk.ac.tees.donut.squad.database.DatabaseHandler;
import android.content.ContentValues;
import uk.ac.tees.donut.squad.posts.Meetup;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
    }
    //Click functionality
    public void addHost(View view) {

        DatabaseHandler dbh = new DatabaseHandler(HostActivity.this);
        //Create map of values
        ContentValues values = new ContentValues();

        String name = findViewById(R.id.newNameTv).toString();
        System.out.println(name);
        String description = findViewById(R.id.newDescriptTv).toString();
        System.out.println(description);
        String interest = findViewById(R.id.newInterestTv).toString();
        System.out.println(interest);
        String address = findViewById(R.id.newAddressTv).toString();
        System.out.println(address);
        String postcode = findViewById(R.id.newPostcodeTv).toString();
        System.out.println(postcode);


        Meetup m = new Meetup();
        dbh.addEvent(m);
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    }
