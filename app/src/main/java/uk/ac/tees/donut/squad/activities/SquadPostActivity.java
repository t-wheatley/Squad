package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Post;
import uk.ac.tees.donut.squad.users.FBUser;

/**
 * Activity which allows the user to view a Squad's posts.
 */
public class SquadPostActivity extends AppCompatActivity
{
    // Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseRecyclerAdapter mAdapter;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;

    // Activity UI
    private Button btnPost;
    private EditText Txtbox;
    private TextView listText;
    private ImageView profileImage;
    private LinearLayout burgerMenu;
    private FloatingActionButton fab;
    private TextView title;
    boolean burger = false;

    // RecyclerView
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.AdapterDataObserver mObserver;

    // Variables
    int loadingCount;
    FBUser user;
    private String squadId;
    private String post;

    // Final values
    private static final String TAG = "Auth";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squad_post);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Posts...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Initialising UI Elements
        Txtbox = (EditText) findViewById(R.id.txtboxPost);
        btnPost = (Button) findViewById(R.id.btnPost);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        listText = (TextView) findViewById(R.id.squadPost_textView);
        profileImage = (ImageView) findViewById(R.id.userPP);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fab = (FloatingActionButton) findViewById(R.id.squadPost_fab);
        burgerMenu = (LinearLayout) findViewById(R.id.squadPost_burgerMenu);
        title = (TextView) findViewById(R.id.squadPost_title);

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();

        if (b != null)
        {
            // Collects the squadId passed from the RecyclerView
            squadId = (String) b.get("squadId");
        } else
        {
            new AlertDialog.Builder(SquadPostActivity.this)
                    .setTitle("Error")
                    .setMessage("The squad went missing somewhere, please try again.")
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

        // onClick listener for the post button
        btnPost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // When pressed calls the createPost method
                if (Txtbox.getText().toString() != "")
                {
                    post = Txtbox.getText().toString();
                    createPost(post, squadId);
                    burgerMenu.setVisibility(View.GONE);
                    fab.setImageResource(R.drawable.ic_speechbubble);
                    burger = false;
                }
            }
        });

        // AuthListener used to check if the user is signed in
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if (firebaseUser != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                } else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    new AlertDialog.Builder(SquadPostActivity.this)
                            .setTitle("Sign-in Error")
                            .setMessage("You do not appear to be signed in, please try again.")
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
            }
        };

        // Setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true); //supposed to reverse order.. but don't think it does
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Get the Squad's Posts
        getPost(squadId);

        // Displaying the mAdapter in the recyclerView
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Method to get all of the Squad's Posts.
     *
     * @param squadId The Squad to get Posts from.
     */
    public void getPost(String squadId)
    {
        // Database reference to get posts
        Query postsQuery = mDatabase.child("posts").orderByChild("squad").equalTo(squadId);

        // Check to see if any Posts exist
        checkForEmpty(postsQuery);

        // Loads the adapter with all the Posts returned by the query
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.item_post,
                PostViewHolder.class,
                postsQuery
        )
        {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, final Post model, int position)
            {
                // Populates a viewHolder with the found Post
                populatePostViewHolder(viewHolder, model, position);
            }
        };
    }

    /**
     * Checks if Posts exist.
     *
     * @param query The query to check.
     */
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
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    /**
     * Method to add an observer on the RecyclerView to check if empty on data changes.
     */
    public void adapterObserver()
    {
        mObserver = new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                if (mAdapter.getItemCount() == 0)
                {
                    listText.setVisibility(View.VISIBLE);
                } else
                {
                    listText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount)
            {
                if (mAdapter.getItemCount() == 0)
                {
                    listText.setVisibility(View.VISIBLE);
                } else
                {
                    listText.setVisibility(View.GONE);
                }
            }
        };
        mAdapter.registerAdapterDataObserver(mObserver);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Adding a listener for the AuthState
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
        {
            // Removing the listener for the AuthState
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Method to post a Post to the Firebase Realtime Database.
     *
     * @param post    The text of the Post.
     * @param squadId The Squad the Post belongs to.
     */
    public void createPost(String post, String squadId)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            // User is signed in
            // Creating a new post node and getting the key value
            String postId = mDatabase.child("posts").push().getKey();

            // Getting current DateTime
            Calendar currentDateTime = Calendar.getInstance();
            Long dateTime = currentDateTime.getTimeInMillis() / 1000L;

            // Creating a post object
            Post postObject = new Post(user.getUid(), squadId, post, postId, dateTime);

            // Pushing the post to the "posts" node using the postId
            mDatabase.child("posts").child(postId).setValue(postObject);

            // Pushing the post to the user's posts
            mDatabase.child("users").child(user.getUid()).child("posts").child(postId).setValue(true);

            finish();
            startActivity(getIntent());
        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Method that populates the viewHolder with the info it needs.
     *
     * @param viewHolder The ViewHolder to be populated.
     * @param model      The Post to be displayed.
     * @param position   The position of the ViewHolder.
     */
    public void populatePostViewHolder(final SquadPostActivity.PostViewHolder viewHolder, final Post model, int position)
    {
        // Display the Post
        viewHolder.postField.setText(model.getPost());

        // Display the post DateTime
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        String startDate = sdf.format(model.getDateTime() * 1000L);
        // Display here

        // Getting the user's name and picture
        mDatabase.child("users").child(model.getUser()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Getting user
                user = dataSnapshot.getValue(FBUser.class);

                if (user != null)
                {

                    // Displaying the user's name
                    viewHolder.nameField.setText(user.getName());

                    // Displays the user's photo in the ImageView
                    Glide.with(viewHolder.itemView.getContext())
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
                                    if (profileImage != null)
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
                            .into(viewHolder.profilePic);
                } else
                {
                    new AlertDialog.Builder(SquadPostActivity.this)
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


        // If loading the last item
        if (mAdapter.getItemCount() == loadingCount)
        {
            // Hide the loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }

        loadingCount++;
    }

    /**
     * Static class to be filled by populatePostViewHolder.
     */
    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView postField;
        ImageView profilePic;

        public PostViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.userName);
            postField = (TextView) v.findViewById(R.id.txtPost);
            profilePic = (ImageView) v.findViewById(R.id.userPP);
        }
    }

    public void fab(View view)
    {
        if(burger == false)
        {
            burger = true;
            fab.setImageResource(R.drawable.ic_cross);
            burgerMenu.setVisibility(View.VISIBLE);
        }
        else
        {
            burger = false;
            fab.setImageResource(R.drawable.ic_speechbubble);
            burgerMenu.setVisibility(View.GONE);
        }
    }
}


