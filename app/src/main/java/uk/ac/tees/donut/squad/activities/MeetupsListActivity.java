package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.Meetup;

public class MeetupsListActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetups_list);


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
        FirebaseRecyclerAdapter<Meetup,MeetupViewHolder> adapter = new FirebaseRecyclerAdapter<Meetup, MeetupViewHolder>(
                Meetup.class,
                R.layout.item_meetup,
                MeetupViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabaseReference.child("meetups").getRef()
        ) {
            @Override
            protected void populateViewHolder(MeetupViewHolder viewHolder,final Meetup model, int position) {

                viewHolder.nameField.setText(model.getName());

                String description = model.getDescription().replace("\n", "");
                String elipsis = "";
                if(description.length() > 54)
                    elipsis = "...";

                String shortDesc = description.substring(0, Math.min(description.length(), 54)) + elipsis;

                viewHolder.descriptionfield.setText(shortDesc);
                viewHolder.squadField.setText(model.getInterest());

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        //Stores the current item's key in a string
                        String mId = model.getId();

                        //Sends the id to the details activity
                        Intent detail = new Intent(MeetupsListActivity.this, MeetupDetail.class);
                        detail.putExtra("meetupId", mId);
                        startActivity(detail);
                    }
                }
                );
            }
        };

        mRecyclerView.setAdapter(adapter);
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
            nameField = (TextView) v.findViewById(R.id.meetupName);
            descriptionfield = (TextView) v.findViewById(R.id.description);
            squadField = (TextView) v.findViewById(R.id.squad);
        }
    }
}
