package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;

public class MeetupsListActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;
    DatabaseReference mDatabase;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetups_list);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Meetups");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.meetupsList_recyclerView);

        if(mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        //setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //setting up firebase recycler adapter
        mAdapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_three_text,
                MeetupViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabaseReference.child("meetups").getRef()
        ) {
            @Override
            protected void populateViewHolder(final MeetupViewHolder viewHolder, final Meetup model, int position) {

                viewHolder.nameField.setText(model.getName());

                String description = model.getDescription().replace("\n", "");
                String elipsis = "";
                if(description.length() > 54)
                    elipsis = "...";

                String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

                viewHolder.descriptionfield.setText(shortDesc);

                // Get Squad name from id
                mDatabase.child("squads").child(model.getSquad()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        viewHolder.squadField.setText(dataSnapshot.child("name").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        //Stores the current item's key in a string
                        String mId = model.getId();

                        //Sends the id to the details activity
                        Intent detail = new Intent(MeetupsListActivity.this, MeetupDetailActivity.class);
                        detail.putExtra("meetupId", mId);
                        startActivity(detail);
                    }
                });

                // If loading the last item or empty
                if ((mAdapter.getItemCount() == loadingCount))
                {
                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                }
                loadingCount++;
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    public static class MeetupViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView descriptionfield;
        TextView squadField;

        public MeetupViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.text1);
            descriptionfield = (TextView) v.findViewById(R.id.text2);
            squadField = (TextView) v.findViewById(R.id.text3);
        }
    }
}
