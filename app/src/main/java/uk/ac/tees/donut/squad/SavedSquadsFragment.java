package uk.ac.tees.donut.squad;

/**
 * Created by jlc-1 on 10/03/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SavedSquadsFragment extends Fragment {

    public SavedSquadsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_saved_squads, container, false);
        getSavedSquads();
        return rootView;
    }

    private ArrayList<String> getSavedSquads()
    {
        ArrayList<String> squads = new ArrayList<String>();
        return squads;
    }

}

