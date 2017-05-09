package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.MyAdapter;
import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Post;

public class SquadPostActivity extends AppCompatActivity {

    private Button btnPost;
    private RecyclerView mRecyclerView;
    private static final String TAG = "Auth";
    private EditText Txtbox;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String squadId;
    private String post;
    private DatabaseReference mDatabase;
    private StaggeredGridLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squad_post);

        Txtbox = (EditText) findViewById(R.id.txtboxPost);
        btnPost = (Button) findViewById(R.id.btnPost);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        if (b != null) {
            // Collects the squadId passed from the RecyclerView
            squadId = (String) b.get("squadId");
        } else {
            new AlertDialog.Builder(SquadPostActivity.this)
                    .setTitle("Error")
                    .setMessage("The squad went missing somewhere, please try again.")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    new AlertDialog.Builder(SquadPostActivity.this)
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
            }
        };

                // onClick listener for the post button
                btnPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // When pressed calls the createPost method
                        if (Txtbox.getText().toString() != "") {
                            post=Txtbox.getText().toString();
                           // createPost(post, squadId);
                        }
                    }
                });

        // Setting up the layout manager
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mRecyclerView.setAdapter(mAdapter);
    }

    public void getPost(String userId)
    {
        // Database reference to get posts
        Query userQuery = mDatabase.child("posts").orderByChild("users/" + userId).equalTo(true);

        // Check to see if any Meetups exist
        checkForEmpty(userQuery);

        mAdapter = new FirebaseRecyclerAdapter<Post, SquadViewHolder>(
                Post.class,
                R.layout.item_squad,
                SquadViewHolder.class,
                userQuery
        )
        {
            @Override
            protected void populateViewHolder(SquadViewHolder viewHolder, final Squad model, int position)
            {
                populateSquadViewHolder(viewHolder, model, position);
            }
        };
    }

    // Checks if Squads in the selected query exist
    public void checkForEmpty(Query query)
    {
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Hide the loading screen
                loadingOverlay.setVisibility(View.GONE);

                // Checks if Squads will be found
                if (dataSnapshot.hasChildren())
                {
                    listText.setVisibility(View.GONE);
                } else
                {
                    listText.setVisibility(View.VISIBLE);
                }

                // Add an Observer to the RecyclerView
                adapterObserver();
            }

    @Override
    public void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Takes a post and pushes it to the Firebase Realtime Database (Without extras)
    public void createPost(String post, String squadId)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            // User is signed in
            // Creating a new post node and getting the key value
            String postId = mDatabase.child("posts").push().getKey();

            // Creating a post object
            Post postObject = new Post(user.getUid(), squadId, post, postId);

            // Pushing the post to the "posts" node using the postId
            mDatabase.child("posts").child(postId).setValue(postObject);

            finish();
            startActivity(getIntent());
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }


    }
}
