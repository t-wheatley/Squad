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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.squads.Squad;

public class SquadListActivity extends AppCompatActivity {

    DatabaseReference mDatabase;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;

    String userId;
    Boolean member;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squad_list);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        // Initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.squadList_recyclerView);

        member = false;

        // Gets the extra passed from the last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if(b != null)
        {
            // Collects the userId passed from the RecyclerView
            userId = (String) b.get("userId");
            member = true;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("squads");

        // If came from 'Your Squads' button on profile
        if(member)
        {
            loadingText.setText("Loading your Squads...");
            getUsers(userId);
        }
        else
        {
            loadingText.setText("Loading Squads...");
            getAll();
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    public void getAll()
    {
        if(mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        // Setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting up Firebase recycler adapter
        mAdapter = new FirebaseRecyclerAdapter<Squad, SquadViewHolder>(
                Squad.class,
                R.layout.item_three_text,
                SquadViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(SquadViewHolder viewHolder, final Squad model, int position) {

                viewHolder.nameField.setText(model.getName());

                String description = model.getDescription().replace("\n", "");
                String elipsis = "";
                if(description.length() > 54)
                    elipsis = "...";

                String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

                viewHolder.descriptionfield.setText(shortDesc);
                viewHolder.placeHolder.setText("Placeholder");

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
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
        };
    }

    public void getUsers(String userId)
    {
        if(mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        // Setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting up Firebase recycler adapter
        mAdapter = new FirebaseRecyclerAdapter<Squad, SquadViewHolder>(
                Squad.class,
                R.layout.item_three_text,
                SquadViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabase.orderByChild("users/" + userId).equalTo(true)
        ) {
            @Override
            protected void populateViewHolder(SquadViewHolder viewHolder, final Squad model, int position) {

                viewHolder.nameField.setText(model.getName());

                String description = model.getDescription().replace("\n", "");
                String elipsis = "";
                if(description.length() > 54)
                    elipsis = "...";

                String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

                viewHolder.descriptionfield.setText(shortDesc);
                viewHolder.placeHolder.setText("Placeholder");

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
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
        };


    }

    public static class SquadViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView descriptionfield;
        TextView placeHolder;

        public SquadViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.text1);
            descriptionfield = (TextView) v.findViewById(R.id.text2);
            placeHolder = (TextView) v.findViewById(R.id.text3);
        }
    }
}
