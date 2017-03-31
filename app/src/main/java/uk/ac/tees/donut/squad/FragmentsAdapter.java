package uk.ac.tees.donut.squad;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by jlc-1 on 31/03/2017.
 */

public class FragmentsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public FragmentsAdapter(FragmentManager fm, int NumTabs) {
        super(fm);
        this.mNumOfTabs = NumTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                HomeFragment tab1 = new HomeFragment();
                return tab1;
            case 1:
                SquadsFragment tab2 = new SquadsFragment();
                return tab2;
            case 2:
                MeetupsFragment tab3 = new MeetupsFragment();
                return tab3;
            case 3:
                ProfileFragment tab4 = new ProfileFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
