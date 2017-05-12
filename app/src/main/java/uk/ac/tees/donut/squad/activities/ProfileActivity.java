package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener
{
    // Firebase + Google
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference mDatabase;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;

    // Activity UI
    TextView profileName;
    TextView bio;
    Button squadBtn;
    Button attendingBtn;
    Button hostingBtn;
    Button secretBtn;
    Button signOutBtn;
    Button editBioBtn;
    ImageView profileImage;

    FloatingActionButton fab;
    boolean burger = false;
    RelativeLayout burgerMenu;


    // Variables
    Boolean personal;
    String uId;
    FBUser user;
    Boolean hasSquad;
    Boolean hasMeetup;
    Boolean hasHost;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
        profileImage = (ImageView) findViewById(R.id.profileImage_ImageView);

        profileName = (TextView) findViewById(R.id.profile_name);

        bio = (TextView) findViewById(R.id.bio);

        fab = (FloatingActionButton) findViewById(R.id.profile_fab);
        fab.setVisibility(View.GONE);
        burgerMenu = (RelativeLayout) findViewById(R.id.profile_burgerMenu);

        squadBtn = (Button) findViewById(R.id.profile_squadsBtn);
        squadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSquads();
            }
        });

        attendingBtn = (Button) findViewById(R.id.profile_attendingBtn);
        attendingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showAttending();
            }
        });

        hostingBtn = (Button) findViewById(R.id.profile_hostingBtn);
        hostingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHosted();
            }
        });
        hostingBtn.setVisibility(View.GONE);

        secretBtn = (Button) findViewById(R.id.profile_secretBtn);
        secretBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                secretMode();
            }
        });
        secretBtn.setVisibility(View.GONE);

        signOutBtn = (Button) findViewById(R.id.profile_signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });
        signOutBtn.setVisibility(View.GONE);

        editBioBtn = (Button) findViewById(R.id.profile_editBio);
        editBioBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editBio();
            }
        });
        editBioBtn.setVisibility(View.GONE);

        // Setting defaults
        hasSquad = false;
        hasMeetup = false;
        hasHost = false;

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            // Collects the uId passed into the activity
            uId = (String) b.get("uId");

            // If the user is looking at their own profile
            if (uId.equals(firebaseUser.getUid()))
            {
                personal = true;
                personalMode();
            }  else
            {
                personal = false;
            }
        } else
        {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Error")
                    .setMessage("The user went missing somewhere, please try again.")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Load info and display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading profile info...");
        loadingOverlay.setVisibility(View.VISIBLE);
        // Starts the loading chain
        // secretCheck -> loadInfo
        secretCheck();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_profile;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.menu_profile;
    }

    public void secretCheck()
    {
        if (firebaseUser != null)
        {
            // Get the user's info
            mDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    // Gets the data from Firebase and stores it in a FBUser class
                    user = dataSnapshot.getValue(FBUser.class);

                    if (user != null)
                    {
                        // If not secret
                        if (user.getSecret() == null || user.getSecret() == false)
                        {
                            // Load user info
                            loadInfo();
                        } else
                        {
                            // User is secret
                            if (!personal)
                            {
                                // User is trying to view a secret profile which is not theirs
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle("Secret User!")
                                        .setMessage("Sorry, we cant show you this user's profile!")
                                        .setPositiveButton("Back", new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            } else
                            {
                                // User viewing their own profile
                                secretBtn.setText("Disable Secret Mode");
                                loadInfo();
                            }
                        }
                    } else
                    {
                        new AlertDialog.Builder(ProfileActivity.this)
                                .setTitle("Something went wrong!")
                                .setMessage("We do not appear to be able to find this user, please try again.")
                                .setPositiveButton("Back", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        } else
        {
            // No user found
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadInfo()
    {
        if (user != null)
        {
            // Checking if user has Squads
            if (user.getSquads() != null)
            {
                hasSquad = true;
            } else
            {
                hasSquad = false;
            }

            // Checking if user has Meetups
            if (user.getMeetups() != null)
            {
                hasMeetup = true;
            } else
            {
                hasMeetup = false;
            }

            // Checking if user has Squads
            if (user.getHosting() != null)
            {
                hasHost = true;
            } else
            {
                hasHost = false;
            }

            // Displays the user's name in the editText
            profileName.setText(user.getName());

            // If user has created a bio
            if (user.getBio() != null)
            {
                // Displays the user's bio in the editText
                bio.setText(user.getBio().trim());
            } else
            {
                // Default bio
                bio.setText("This user has no bio!");
            }

            // Displays the photo in the ImageView
            Glide.with(ProfileActivity.this)
                    .load(user.getPicture().trim())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .listener(new RequestListener<String, GlideDrawable>()
                    {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
                        {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                        {
                            // If profileName != default and profileImage isnt null
                            if ((!profileName.getText().equals("'Users Profile")) && (profileImage != null))
                            {
                                // Hiding loading overlay
                                loadingOverlay.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    })
                    .dontAnimate()
                    .fitCenter()
                    .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                    .into(profileImage);
        } else
        {
            // No user found
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showSquads()
    {
        // If a user has squads
        if (hasSquad)
        {
            // Loads the SquadList activity displaying the Squads that the user has joined
            Intent intent = new Intent(this, SquadListActivity.class);
            intent.putExtra("userId", uId);
            startActivity(intent);
        } else
        {
            Toast.makeText(getApplicationContext(), user.getName().trim() +
                    " has no squads!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAttending()
    {
        // If a user has meetups
        if (hasMeetup)
        {
            // Loads the MeetupsList activity displaying the Meetups that the user has attended
            Intent intent = new Intent(this, MeetupsListActivity.class);
            intent.putExtra("userId", uId);
            startActivity(intent);
        } else
        {
            Toast.makeText(getApplicationContext(), user.getName().trim() +
                    " has no meetups!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showHosted()
    {
        // If a user is hosting meetups
        if (hasHost)
        {
            // Loads the MeetupsList activity displaying the Meetups that the user is hosting
            Intent intent = new Intent(this, MeetupsListActivity.class);
            intent.putExtra("userId", uId);
            intent.putExtra("host", true);
            startActivity(intent);
        } else
        {
            Toast.makeText(getApplicationContext(), user.getName().trim() +
                    " is hosting no meetups!", Toast.LENGTH_SHORT).show();
        }
    }

    public void secretMode()
    {
        // If user not secret
        if (user.getSecret() == null || user.getSecret() == false)
        {
            enableSecret();
            Toast.makeText(this, "Secret mode enabled, your profile is now hidden on meetup's and squad's", Toast.LENGTH_LONG).show();
        } else
        {
            disableSecret();
            Toast.makeText(this, "Secret mode disabled, your profile is now visible on meetup's and squad's", Toast.LENGTH_LONG).show();
        }
    }

    public void enableSecret()
    {
        // Sets the user to secret and changes the button
        mDatabase.child(firebaseUser.getUid()).child("secret").setValue(true);
        user.setSecret(true);
        secretBtn.setText("Disable Secret Mode");
    }

    public void disableSecret()
    {
        // Disables the user's secret mode and changes the button
        mDatabase.child(firebaseUser.getUid()).child("secret").setValue(false);
        user.setSecret(false);
        secretBtn.setText("Enable Secret Mode");
    }

    public void signOut()
    {
        // Signs the user out of Firebase Auth and then Google Sign In
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        // Loads the LoginActivity activity and closes all other activites
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        editTextBio.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
        builder.setView(editTextBio);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String newBio = editTextBio.getText().toString();
                user.setBio(newBio);
                bio.setText(newBio);
                updateBio(newBio);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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

    public void personalMode()
    {
        // Displaying what the user should see on their own profile
        squadBtn.setText("My Squads");
        attendingBtn.setText("My Meetups");
        fab.setVisibility(View.VISIBLE);
        hostingBtn.setVisibility(View.VISIBLE);
        secretBtn.setVisibility(View.VISIBLE);
        signOutBtn.setVisibility(View.VISIBLE);
        editBioBtn.setVisibility(View.VISIBLE);

        // Moving the user's Name if displaying the fab
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        newParams.setMarginEnd(88);
        profileName.setLayoutParams(newParams);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    public void fab(View view)
    {
        if(burger == false)
        {
            burgerMenu.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.ic_cross);
            burger = true;
        }
        else
        {
            burgerMenu.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.ic_burger);
            burger = false;
        }
    }
}
