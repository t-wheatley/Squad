package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
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

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.AddressPlace;

public class PlacesListActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;

    RelativeLayout loadingOverlay;
    TextView loadingText;

    int loadingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        // Display loading overlay
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        loadingText.setText("Loading Places...");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingCount = 1;

        //initialising RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        if(mRecyclerView != null)
        {
            mRecyclerView.setHasFixedSize(true);
        }

        //setting up the layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //setting up firebase recycler adapter
        mAdapter = new FirebaseRecyclerAdapter<AddressPlace, PlacesListActivity.PlaceViewHolder>(
                AddressPlace.class,
                R.layout.item_three_text,
                PlacesListActivity.PlaceViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabaseReference.child("places").getRef()
        ) {
            @Override
            protected void populateViewHolder(PlacesListActivity.PlaceViewHolder viewHolder, final AddressPlace model, int position) {

                viewHolder.nameField.setText(model.getName());
                viewHolder.addressField.setText(model.fullAddress());
                viewHolder.squadField.setText(model.getInterest());

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Stores the current item's key in a string
                        String mId = model.getPlaceId();

                        //Sends the id to the details activity
                        Intent detail = new Intent(PlacesListActivity.this, PlaceDetailsActivity.class);
                        detail.putExtra("placeId", mId);
                        startActivity(detail);
                    }
                }
                );

                // If loading the last item
                if (mAdapter.getItemCount() == loadingCount)
                {
                    // Hide the loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                }

                loadingCount++;
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView nameField;
        TextView addressField;
        TextView squadField;

        public PlaceViewHolder(View v)
        {
            super(v);
            mView = v;
            nameField = (TextView) v.findViewById(R.id.text1);
            addressField = (TextView) v.findViewById(R.id.text2);
            squadField = (TextView) v.findViewById(R.id.text3);
        }
    }
}
