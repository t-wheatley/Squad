package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.posts.Meetup;

public class NewMeetup extends AppCompatActivity
{
    private DatabaseReference mDatabase;

    private EditText editName;
    private EditText editInterest;
    private EditText editDescription;
    private EditText  editAddress;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meetup);
        this.setTitle("New Meetup");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Links the variables to their layout items.
        editName = (EditText) findViewById(R.id.newMeetup_textEditName);
        editInterest = (EditText) findViewById(R.id.newMeetup_textEditInterest);
        editDescription = (EditText) findViewById(R.id.newMeetup_textEditDescription);
        editAddress = (EditText) findViewById(R.id.newMeetup_meetupAddress);
        btnSubmit = (Button) findViewById(R.id.newMeetup_buttonSubmit);

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When pressed calls the submitMeeup method
                submitMeetup();
            }
        });
    }

    private void submitMeetup()
    {
        // Gets the strings from the editTexts
        final String name = editName.getText().toString();
        final String interest = editInterest.getText().toString();
        final String description = editDescription.getText().toString();

        // Checks if the name field is empty
        if (TextUtils.isEmpty(name))
        {
            // If no string is found an error is output
            editName.setError("Required");
            return;
        }

        // Interest is required
        if (TextUtils.isEmpty(interest))
        {
            editInterest.setError("Required");
            return;
        }

        // Description is required
        if (TextUtils.isEmpty(description))
        {
            editDescription.setError("Required");
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // Calls the createMeetup method with the strings entered
        createMeetup(name, interest, description);

        // Re-enables the editTexts and buttons and finishes the activity
        setEditingEnabled(true);
        finish();
    }

    // Takes a meetup and pushes it to the Firebase Realtime Database (Without extras)
    public void createMeetup(String n, String i, String d)
    {
        // Creating a new meetup node and getting the key value
        String meetupId = mDatabase.child("meetups").push().getKey();

        // Creating a meetup object
        Meetup meetup = new Meetup(meetupId, n, i, d);

        // Pushing the meetup to the "meetups" node using the meetupId
        mDatabase.child("meetups").child(meetupId).setValue(meetup);
    }

    // Takes a boolean value to either enable or disable the UI elements, this is used to avoid multiple posts
    private void setEditingEnabled(boolean enabled)
    {
        editName.setEnabled(enabled);
        editInterest.setEnabled(enabled);
        editDescription.setEnabled(enabled);
        if (enabled)
        {
            btnSubmit.setVisibility(View.VISIBLE);
        } else
        {
            btnSubmit.setVisibility(View.GONE);
        }
    }
}
