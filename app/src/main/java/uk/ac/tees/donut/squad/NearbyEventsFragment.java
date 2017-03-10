package uk.ac.tees.donut.squad;

/**
 * Created by jlc-1 on 10/03/2017.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NearbyEventsFragment extends Fragment {

    public NearbyEventsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_nearby_events, container, false);

        return rootView;
    }
    //Access the database and retrieve 5-10 events until the user scrolls down

}
