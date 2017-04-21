package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.users.FBUser;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference mDatabase;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    private String bioText;

    TextView profileName;
    TextView bio;
    Button attendingBtn;
    Button signOutBtn;
    ImageButton editBioBtn;
    ImageView profileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Getting instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Getting ui elements
        profileImage = (ImageView)findViewById(R.id.profileImage_ImageView);

        profileName = (TextView) findViewById(R.id.profileName);

        bio = (TextView) findViewById(R.id.bio);

        attendingBtn = (Button) findViewById(R.id.attendingBtn);
        attendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttending();
            }
        });

        signOutBtn = (Button) findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        editBioBtn = (ImageButton) findViewById(R.id.profile_imageButtonProfileBioEdit);
        editBioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Dialog to edit Bio
                editBio();
            }
        });

        // Load info and display loading overlay
        loadInfo();
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading profile info...");
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    public void showAttending()
    {
        // Loads the ViewMeetups acitivty displaying the meetups that the user has attended
        Intent intent = new Intent(this, ViewMeetups.class);
        Bundle b = new Bundle();
        b.putBoolean("ATT", true);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void signOut()
    {
        // Signs the user out of Firebase Auth and then Google Sign In
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        // Loads the SplashScreen activity and closes all other activites
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    public void editBio()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bio:");

        // EditText for input
        final EditText editTextBio = new EditText(this);
        // Sets the expected input types, text, long message, auto correct and multi line
        editTextBio.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        // Sets the maximum characters to 120
        editTextBio.setFilters(new InputFilter[] { new InputFilter.LengthFilter(120) });
        builder.setView(editTextBio);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bioText = editTextBio.getText().toString();
                bio.setText(bioText);
                updateBio(bioText);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Displays the Dialog
        builder.show();
    }

    public void updateBio(String bio)
    {
        if (firebaseUser != null)
        {
            // Pushing the new bio to the bio field of the User's data
            mDatabase.child(firebaseUser.getUid()).child("bio").setValue(bio);

        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadInfo()
    {
        if (firebaseUser != null)
        {
            // Gets the photo from the Firebase User and displays it in the ImageView
            Glide.with(this)
                    .load(mAuth.getCurrentUser().getPhotoUrl())
                    .fitCenter()
                    .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                    .into(profileImage);

            // Gets the user's displayname and displays it in the editText
            profileName.setText(mAuth.getCurrentUser().getDisplayName());

            // Get the user's Bio
            mDatabase.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    // If user has created a bio
                    if(dataSnapshot.hasChild("bio"))
                    {
                        // Gets the data from Firebase and stores it in a FBUser class
                        FBUser user = dataSnapshot.getValue(FBUser.class);

                        // Displays the found meetup's attributes
                        bio.setText(user.getBio());
                    } else
                    {
                        bio.setText("Write something about yourself!");
                    }
                    // If profileName != default and profileImage isnt null
                    if ((!profileName.getText().equals("'Users Profile")) && (profileImage != null))
                    {
                        // Hiding loading overlay
                        loadingOverlay.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }
}
