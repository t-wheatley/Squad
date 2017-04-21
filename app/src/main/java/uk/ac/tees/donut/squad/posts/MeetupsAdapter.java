package uk.ac.tees.donut.squad.posts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import uk.ac.tees.donut.squad.R;

/**
 * Created by q5273202 on 07/04/2017.
 */

public class MeetupsAdapter extends RecyclerView.Adapter<MeetupsAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView meetupText;
        public TextView squadText;
        public TextView description;

        public ViewHolder(View itemView)
        {
            super(itemView);

            meetupText = (TextView) itemView.findViewById(R.id.meetupName);
            squadText = (TextView) itemView.findViewById(R.id.squad);
            description = (TextView) itemView.findViewById(R.id.description);
        }
    }

    //store a member variable for the meetups
    private List<Meetup> mMeetups;

    //store the context for easy access
    private Context mContext;

    //pass in the meetups array into the constructor
    public MeetupsAdapter(Context context, List<Meetup> meetups)
    {
        mMeetups = meetups;
        mContext = context;
    }

    private Context getContext()
    {
        return mContext;
    }

    //inflates a layout XML and returns the holder
    @Override
    public MeetupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate the custom layout
        View meetupView = inflater.inflate(R.layout.item_threeText, parent, false);

        //return a new holder instance
        ViewHolder viewHolder = new ViewHolder(meetupView);
        return viewHolder;
    }

    //populates data into the item through holder
    @Override
    public void onBindViewHolder(MeetupsAdapter.ViewHolder viewHolder, int position)
    {
        //get the data model based on position
        Meetup meetup = mMeetups.get(position);

        //set item views based on your views and data model
        TextView meetupText = viewHolder.meetupText;
        meetupText.setText(meetup.getName());

        TextView squadText = viewHolder.squadText;
        squadText.setText(meetup.getInterest());

        TextView description = viewHolder.description;
        description.setText(meetup.getDescription().substring(0, 15) + "...");
    }

    //returns the total count of items in the list
    @Override
    public int getItemCount()
    {
        return mMeetups.size();
    }


}
