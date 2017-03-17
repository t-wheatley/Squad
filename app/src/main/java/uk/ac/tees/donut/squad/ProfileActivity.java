package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

public class ProfileActivity extends AppCompatActivity
{
    DatabaseReference mDatabase;

    EditText profileName;
    EditText profileInterest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Declaring editText
        profileName= (EditText) findViewById(R.id.profileName_EditText);
        profileInterest = (EditText) findViewById(R.id.profileInterest_EditText);

        //Disabling the editTexts
        profileName.setEnabled(false);
        profileInterest.setEnabled(false);


    }
}
