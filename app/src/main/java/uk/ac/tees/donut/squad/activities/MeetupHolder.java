package uk.ac.tees.donut.squad.activities;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Tom on 15/03/2017.
 */

public class MeetupHolder extends RecyclerView.ViewHolder
{
    private final TextView nameField;
    private final TextView interestField;
    View mView;

    public MeetupHolder(View itemView)
    {
        super(itemView);
        nameField = (TextView) itemView.findViewById(android.R.id.text1);
        interestField = (TextView) itemView.findViewById(android.R.id.text2);
        mView = itemView;
    }

    public void setName(String name)
    {
        nameField.setText(name);
    }

    public void setInterest(String interest)
    {
        interestField.setText(interest);
    }
}
