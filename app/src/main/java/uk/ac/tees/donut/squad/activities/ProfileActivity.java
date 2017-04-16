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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.users.User;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    DatabaseReference mDatabase;

    TextView profileName;
    TextView bio;
    Button attendingBtn;
    Button signOutBtn;
    ImageButton editBioBtn;

    private String bioText;

    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;

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

        // Getting ui elements
        ImageView profileImage = (ImageView)findViewById(R.id.profileImage_ImageView);

        profileName = (TextView) findViewById(R.id.profileName);

        bio = (TextView) findViewById(R.id.bio);
        bio.setText(User.getBio());

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

        editBioBtn = (ImageButton) findViewById(R.id.imageButtonEdit);
        editBioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Dialog to edit Bio
                editBio();
            }
        });

        // Gets the photo from the Firebase User and displays it in the ImageView
        Glide.with(this)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .fitCenter()
                .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                .into(profileImage);

        // Gets the users displayname and displays it in the editText
        profileName.setText(mAuth.getCurrentUser().getDisplayName());


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

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }
}
