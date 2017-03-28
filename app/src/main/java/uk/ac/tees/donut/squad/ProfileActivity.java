package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import uk.ac.tees.donut.squad.users.User;

public class ProfileActivity extends AppCompatActivity
{
    DatabaseReference mDatabase;

    TextView profileName;
    TextView profileInterest;
    Button attendingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Declaring editText
        profileName = (TextView) findViewById(R.id.profileName);
        profileName.setText(User.getName());

        profileInterest = (TextView) findViewById(R.id.profileInterest_EditText);

        attendingBtn = (Button) findViewById(R.id.attendingBtn);
        attendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttending();
            }
        });
    }

    public void showAttending()
    {
        Intent intent = new Intent(this, ViewMeetups.class);
        Bundle b = new Bundle();
        b.putBoolean("ATT", true);
        intent.putExtras(b);
        startActivity(intent);
    }

}
