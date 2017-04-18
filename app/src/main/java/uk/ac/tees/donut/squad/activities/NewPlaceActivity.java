package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.posts.Place;

public class NewPlaceActivity extends AppCompatActivity {

    private static final String TAG = "Auth";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

    private EditText editName;
    private Spinner spinnerInterest;
    private EditText editDescription;
    private Button btnSubmit;

    private EditText editA1;
    private EditText editA2;
    private EditText editTC;
    private EditText editC;
    private EditText editPC;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);
        this.setTitle("New Meetup");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Links the variables to their layout items.
        editName = (EditText) findViewById(R.id.textEditName);
        spinnerInterest = (Spinner) findViewById(R.id.spinnerInterest);
        editDescription = (EditText) findViewById(R.id.textEditDescription);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);

        editA1 = (EditText) findViewById(R.id.textEditAddress1);
        editA2 = (EditText) findViewById(R.id.textEditAddress2);
        editTC = (EditText) findViewById(R.id.textEditAddressTC);
        editC = (EditText) findViewById(R.id.textEditAddressCounty);
        editPC = (EditText) findViewById(R.id.textEditAddressPC);

        // onClick listener for the submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When pressed calls the submitMeeup method
                submitPlace();
            }
        });

        fillSpinner();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(NewPlaceActivity.this, "User: " + user.getUid(), Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(NewPlaceActivity.this, "No User", Toast.LENGTH_SHORT).show();

                    new AlertDialog.Builder(NewPlaceActivity.this)
                            .setTitle("Sign-in Error")
                            .setMessage("You do not appear to be signed in, please try again.")
                            .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void submitPlace(){
        // Gets the strings from the editTexts
        final String name = editName.getText().toString();
        final String interest = spinnerInterest.getSelectedItem().toString();
        final String description = editDescription.getText().toString();

        final String a1 = editA1.getText().toString();
        final String a2 = editA2.getText().toString();
        final String tc = editTC.getText().toString();
        final String c = editC.getText().toString();
        final String pc = editPC.getText().toString();


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
        createPlace(name, interest, description, a1, a2, tc, c, pc);

        // Re-enables the editTexts and buttons and finishes the activity
        setEditingEnabled(true);
        finish();
    }

    // Takes a meetup and pushes it to the Firebase Realtime Database (Without extras)
    public void createPlace(String n, String i, String d, String a1, String a2, String tc, String c, String pc){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in

            // Creating a new meetup node and getting the key value
            String placeId = mDatabase.child("places").push().getKey();

            // Creating a place object
            Place place = new Place(placeId, n, i, d, a1, a2, tc, c, pc, user.getUid());

            // Pushing the meetup to the "meetups" node using the placeId
            mDatabase.child("places").child(placeId).setValue(place);
        } else {
            // No user is signed in

            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }


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

    // Fill's the spinner with all of the interests stored in FireBase
    private void fillSpinner()
    {
        mDatabase.child("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(NewPlaceActivity.this, "Loading interests...", Toast.LENGTH_SHORT).show();
                final List<String> interests = new ArrayList<String>();

                for (DataSnapshot interestSnapshot: dataSnapshot.getChildren()) {
                    String interest = interestSnapshot.child("name").getValue(String.class);
                    interests.add(interest);
                }

                ArrayAdapter<String> interestAdapter = new ArrayAdapter<String>(NewPlaceActivity.this, android.R.layout.simple_spinner_item, interests);
                interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInterest.setAdapter(interestAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
