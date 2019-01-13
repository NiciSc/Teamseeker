package de.ur.mi.android.teamseeker.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.ur.mi.android.teamseeker.fragments.tutorial.FilterEventsTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.FinishSetupTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.JoinEventTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.SearchMapTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.SetProfileTutorialFragment;

public class TutorialPagerAdapter extends FragmentPagerAdapter {

    public TutorialPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SetProfileTutorialFragment.newInstance();
            case 1:
                return SearchMapTutorialFragment.newInstance();
            case 2:
                return FilterEventsTutorialFragment.newInstance();
            case 3:
                return JoinEventTutorialFragment.newInstance();
            case 4:
                return FinishSetupTutorialFragment.newInstance();
            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
