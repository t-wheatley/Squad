package uk.ac.q5081793tees.squads;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import uk.ac.q5081793tees.squads.posts.Meetup;
import uk.ac.q5081793tees.squads.database.DatabaseHandler;
public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
    }

    //Click functionality
    public void addHost(View view)
    {

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
