package uk.ac.tees.donut.squad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.posts.Meetup;

public class NewMeetup extends AppCompatActivity
{
    private DatabaseReference mDatabase;

    private EditText editName;
    private Spinner spinnerInterest;
    private EditText editDescription;
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
        spinnerInterest = (Spinner) findViewById(R.id.newMeetup_spinnerInterest);
        editDescription = (EditText) findViewById(R.id.newMeetup_textEditDescription);
        btnSubmit = (Button) findViewById(R.id.newMeetup_buttonSubmit);

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When pressed calls the submitMeeup method
                submitMeetup();
            }
        });

        fillSpinner();
    }

    private void submitMeetup()
    {
        // Gets the strings from the editTexts
        final String name = editName.getText().toString();
        final String interest = spinnerInterest.getSelectedItem().toString();
        final String description = editDescription.getText().toString();

        // Checks if the name field is empty
        if (TextUtils.isEmpty(name))
        {
            // If no string is found an error is output
            editName.setError("Required");
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
        spinnerInterest.setEnabled(enabled);
        editDescription.setEnabled(enabled);
        if (enabled)
        {
            btnSubmit.setVisibility(View.VISIBLE);
        } else
        {
            btnSubmit.setVisibility(View.GONE);
        }
    }

    private void fillSpinner()
    {
        mDatabase.child("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(NewMeetup.this, "Loading interests...", Toast.LENGTH_SHORT).show();
                final List<String> interests = new ArrayList<String>();

                for (DataSnapshot interestSnapshot: dataSnapshot.getChildren()) {
                    String interest = interestSnapshot.child("name").getValue(String.class);
                    interests.add(interest);
                }

                ArrayAdapter<String> interestAdapter = new ArrayAdapter<String>(NewMeetup.this, android.R.layout.simple_spinner_item, interests);
                interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInterest.setAdapter(interestAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
