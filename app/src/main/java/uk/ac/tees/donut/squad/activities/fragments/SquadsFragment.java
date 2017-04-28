package uk.ac.tees.donut.squad.activities.fragments;

/**
 * Created by jlc-1 on 31/03/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.tees.donut.squad.R;

public class SquadsFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.squads_fragment, container, false);
    }
}