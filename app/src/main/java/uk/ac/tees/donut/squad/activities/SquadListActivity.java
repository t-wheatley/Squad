package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Activity which allows the user to view a list of Squads.
 */
public class SquadListActivity extends BaseActivity
{

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseStorage firebaseStorage;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;

    // Activity UI
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;
    TextView listText;

    // Variables
    FirebaseUser firebaseUser;
    String userId;
    Boolean member;
    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Squads...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Initialising UI Elements
        mRecyclerView = (RecyclerView) findViewById(R.id.squadList_recyclerView);
        listText = (TextView) findViewById(R.id.squadList_textView);

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        // Getting the currently signed-in User
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialising variables
        member = false;

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null)
        {
            // Collects the userId passed from the RecyclerView
            userId = (String) b.get("userId");
            member = true;
        }

        if (mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        // Setting up the layout manager
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // If came from 'View Squads' button on profile
        if (member)
        {
            getUsers(userId);
        } else
        {
            getAll();
        }

        // Displaying the mAdapter in the recyclerVIew
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    int getContentViewId()
    {
        return R.layout.activity_squad_list;
    }

    @Override
    int getNavigationMenuItemId()
    {
        return R.id.menu_squads;
    }

    /**
     * Method to get all of the Squads.
     */
    public void getAll()
    {
        // Database reference to get a Squad's Meetups
        Query allQuery = mDatabase.child("squads");

        // Check to see if any Squads exist
        checkForEmpty(allQuery);

        // Loads the adapter with all the Squads returned by the query
        mAdapter = new FirebaseRecyclerAdapter<Squad, SquadViewHolder>(
                Squad.class,
                R.layout.item_squad,
                SquadViewHolder.class,
                allQuery
        )
        {
            @Override
            protected void populateViewHolder(SquadViewHolder viewHolder, final Squad model, int position)
            {
                // Populates a viewHolder with the found Squad
                populateSquadViewHolder(viewHolder, model, position);
            }
        };
    }

    /**
     * Method to get a User's Squads.
     */
    public void getUsers(String userId)
    {
        // Database reference to get a Squad's Meetups
        Query userQuery = mDatabase.child("squads").orderByChild("users/" + userId).equalTo(true);

        // Check to see if any Squads exist
        checkForEmpty(userQuery);

        // Loads the adapter with all the Squads returned by the query
        mAdapter = new FirebaseRecyclerAdapter<Squad, SquadViewHolder>(
                Squad.class,
                R.layout.item_squad,
                SquadViewHolder.class,
                userQuery
        )
        {
            @Override
            protected void populateViewHolder(SquadViewHolder viewHolder, final Squad model, int position)
            {
                // Populates a viewHolder with the found Squad
                populateSquadViewHolder(viewHolder, model, position);
            }
        };
    }

    /**
     * Checks if Squads exist.
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
     * Method to add an observer on the RecyclerView to check if empty on data changes
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

    /**
     * Method that populates the viewHolder with the info it needs.
     *
     * @param viewHolder The ViewHolder to be populated.
     * @param model      The Squad to be displayed.
     * @param position   The position of the ViewHolder.
     */
    public void populateSquadViewHolder(final SquadViewHolder viewHolder, final Squad model, int position)
    {
        // Displaying the image loading
        viewHolder.imageLoading.setVisibility(View.VISIBLE);
        viewHolder.squadImage.setVisibility(View.INVISIBLE);

        // Displaying the name
        viewHolder.nameField.setText(model.getName());

        // Displaying the description
        String description = model.getDescription().replace("\n", "");
        String elipsis = "";
        if (description.length() > 54)
            elipsis = "...";

        String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

        viewHolder.descriptionfield.setText(shortDesc);

        // Getting the users HashMap
        HashMap<String, Boolean> users = model.getUsers();

        // If the HashMap isnt empty
        if (users != null)
        {
            // Checking if the user is already in the Squad
            if (users.containsKey(firebaseUser.getUid()))
            {
                viewHolder.joined.setVisibility(View.VISIBLE);
            } else
            {
                viewHolder.joined.setVisibility(View.GONE);
            }
        } else
        {
            viewHolder.joined.setVisibility(View.GONE);
        }

        // Get member count
        if (users != null)
        {
            viewHolder.squadMembers.setText(String.valueOf(users.size()));
        } else
        {
            viewHolder.squadMembers.setText("0");
        }


        // Gets the storage reference of the Squad's picture
        StorageReference squadStorage = firebaseStorage.getReference().child("squads/" + model.getId() + ".png");

        // Display picture loading
        viewHolder.imageLoading.setVisibility(View.VISIBLE);
        viewHolder.squadImage.setVisibility(View.VISIBLE);

        // Gets the Uri to download
        squadStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                // If a picture exists
                // Download and display using Glide
                Glide.with(viewHolder.itemView.getContext())
                        .load(uri)
                        .listener(new RequestListener<Uri, GlideDrawable>()
                        {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource)
                            {
                                viewHolder.imageLoading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                            {
                                viewHolder.imageLoading.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .error(R.drawable.ic_donut_minim)
                        .into(viewHolder.squadImage);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // If no picture exists
                viewHolder.imageLoading.setVisibility(View.GONE);
            }
        });

        // OnClick
        viewHolder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Stores the current item's key in a string
                String sId = model.getId();

                //Sends the id to the details activity
                Intent detail = new Intent(SquadListActivity.this, SquadDetailActivity.class);
                detail.putExtra("squadId", sId);
                startActivity(detail);
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
     * Static class to be filled by populateSquadViewHolder.
     */
    public static class SquadViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView descriptionfield;
        ImageView joined;
        ImageView squadImage;
        ProgressBar imageLoading;
        TextView squadMembers;

        public SquadViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.itemSquad_squadName);
            descriptionfield = (TextView) v.findViewById(R.id.itemSquad_description);
            joined = (ImageView) v.findViewById(R.id.itemSquad_tick);
            squadImage = (ImageView) v.findViewById(R.id.itemSquad_image);
            imageLoading = (ProgressBar) v.findViewById(R.id.itemSquad_imageProgress);
            squadMembers = (TextView) v.findViewById(R.id.itemSquad_memberCount);
        }
    }
}