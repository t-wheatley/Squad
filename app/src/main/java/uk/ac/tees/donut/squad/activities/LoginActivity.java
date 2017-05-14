package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.users.FBUser;

/**
 * Activity which allows the user to sign in using their Google account.
 */
public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    // Google
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount googleSignInAccount;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;

    // Final Values
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Auth";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Getting an instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialising loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Logging in...");

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Setting the onClick for the Google sign in button
        findViewById(R.id.sign_in_button).setOnClickListener(this);

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

        // AuthListener used to check if the user is signed in
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    // User is signed in, load MenuActivity
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // Display loading overlay
                    loadingOverlay.setVisibility(View.VISIBLE);

                    if (googleSignInAccount != null)
                    {
                        updateGuarantee(user);
                    } else
                    {
                        // Update the users details
                        tryUpdate(user);
                    }
                } else
                {
                    // User is not signed in, nothing
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Adding a listener for the AuthSate
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
        {
            // Removing the listener for the AuthSate
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(LoginActivity.this, "Connection failed, please try again.", Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View v)
    {
        // When the Google sign in button is pressed call the signIn() method
        switch (v.getId())
        {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    /**
     * Method called when Google sign in button is pressed, starts the Google sign in intent.
     */
    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Method called on result of the Google sign in Intent.
     *
     * @param requestCode The request code that was passed to startActivityForResult().
     * @param resultCode  The result code, RESULT_OK if successful, RESULT_CANCELED if not.
     * @param data        An Intent that carries the data of the result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Display loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                // Google Sign In was successful, authenticate with Firebase
                googleSignInAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
                handleSignInResult(result);
            } else
            {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, "Google Sign-In failed.",
                        Toast.LENGTH_SHORT).show();

                // Hiding loading overlay
                loadingOverlay.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Method that creates an AuthCredential and tries to sign into Firebase.
     *
     * @param acct The GoogleSignInAccount the user has signed-in with.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Getting the AuthCredential from the GoogleSignInAccount
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // Trying to sign into Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            // Hiding loading overlay
                            loadingOverlay.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Method that handles the GoogleSignInResult, will remove the loading overlay if failed.
     *
     * @param result GoogleSignInResult
     */
    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess())
        {
            // Signed in successfully
            Log.d(TAG, "GoogleSignInResult successful");
        } else
        {
            Log.d(TAG, "GoogleSignInResult not successful");

            Toast.makeText(LoginActivity.this, "GoogleSignInResult failed.",
                    Toast.LENGTH_SHORT).show();

            // Hiding loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    /**
     * Method to try update the FirebaseUser's details using getProviderData
     *
     * @param firebaseUser The Signed-in FirebaseUser.
     */
    private void tryUpdate(FirebaseUser firebaseUser)
    {
        final FirebaseUser user = firebaseUser;
        if (user != null)
        {
            for (UserInfo profile : user.getProviderData())
            {
                // Name and profile photo Url
                final String name = profile.getDisplayName();
                final Uri photoUrl = profile.getPhotoUrl();

                // Creating a UserProfileChangeRequest
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(photoUrl)
                        .build();


                // Sending the UserProfileChangeRequest
                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Log.d(TAG, "Firebase Auth Profile Updated");
                        updateFBUser(user.getUid(), name, photoUrl);
                    }
                });

            }
        }
    }

    /**
     * Method for guaranteed FirebaseUser's details using  using GoogleSignInAccount.
     *
     * @param firebaseUser The Signed-in FirebaseUser.
     */
    public void updateGuarantee(FirebaseUser firebaseUser)
    {
        // GoogleSignInAccount update
        final String uId = firebaseUser.getUid();
        final String newName = googleSignInAccount.getDisplayName();
        final Uri newPic = googleSignInAccount.getPhotoUrl();

        // Creating a UserProfileChangeRequest
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(newPic)
                .build();

        // Sending the UserProfileChangeRequest
        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Log.d(TAG, "Firebase Auth Profile Updated");
                updateFBUser(uId, newName, newPic);
            }
        });
    }

    /**
     * Method to send the new FirebaseUser details to the Firebase database.
     *
     * @param uId      The unique ID of the FirebaseUser
     * @param name     The new name of the FirebaseUser
     * @param photoUrl The new photo of the FirebaseUser
     */
    public void updateFBUser(final String uId, final String name, final Uri photoUrl)
    {
        if (uId != null)
        {
            mDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    // Gets the data from Firebase and stores it in a FBUser class
                    FBUser user = dataSnapshot.getValue(FBUser.class);

                    // If the user does not exist (Signed-in for the first time)
                    if (user == null)
                    {
                        // Create a new FBUser
                        user = new FBUser();
                    }

                    // If there is a new picture
                    if (photoUrl != null)
                    {
                        user.setPicture(photoUrl.toString());
                    }

                    // If there is a new name
                    if (name != null)
                    {
                        user.setName(name);
                    }

                    // Update the Firebase Database
                    mDatabase.child(uId).setValue(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        // Start the main menu
        Intent i = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(i);
        finish();

    }
}
