package uk.ac.tees.donut.squad.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.R;


/**
 * Created by jlc-1 on 29/04/2017.
 */

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
         View Homeview = inflater.inflate(R.layout.activity_menu, container, false);
        System.out.println("-------Inflated: Home Fragment-------");
        final Toolbar myToolbar = (Toolbar) Homeview.findViewById(R.id.my_toolbar);
        final Spinner toolbarSpin = (Spinner) Homeview.findViewById(R.id.spinner_nav);
        final Button toolbarMapBtn = (Button) Homeview.findViewById(R.id.button_maps);
        /*
        toolbarSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                userSelectedIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        }

    );*/
        toolbarMapBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                inflater.inflate(R.layout.activity_home_map_fragment, container, false);
                System.out.println("-------Inflated: Home-map Fragment-------");

            }
        });
        return Homeview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
