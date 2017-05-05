package uk.ac.q5081793tees.bottomnav.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.q5081793tees.bottomnav.R;

/**
 * Created by jlc-1 on 29/04/2017.
 */

public class SquadFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View squadView = inflater.inflate(R.layout.squad_fragment, container, false);
        System.out.println("-------Inflated: Squad Fragment-------");
        return squadView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
