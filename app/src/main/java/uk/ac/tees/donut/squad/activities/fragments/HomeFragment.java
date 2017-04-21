package uk.ac.tees.donut.squad.activities.fragments;

/**
 * Created by jlc-1 on 31/03/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.tees.donut.squad.activities.NewMeetup;
import uk.ac.tees.donut.squad.activities.ProfileActivity;
import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.activities.SettingsActivity;
import uk.ac.tees.donut.squad.activities.SquadsActivity;
import uk.ac.tees.donut.squad.activities.ViewMeetups;
import uk.ac.tees.donut.squad.location.GeocoderActivity;
import uk.ac.tees.donut.squad.location.MapActivity;

public class HomeFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }
}
