package de.ur.mi.android.teamseeker.adapters;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.fragments.EventChatFragment;
import de.ur.mi.android.teamseeker.fragments.EventInfoFragment;
import de.ur.mi.android.teamseeker.fragments.EventParticipantsFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.FilterEventsTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.FinishSetupTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.JoinEventTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.SearchMapTutorialFragment;
import de.ur.mi.android.teamseeker.fragments.tutorial.SetProfileTutorialFragment;

public class EventPagerAdapter extends FragmentPagerAdapter {

    //https://developer.android.com/reference/android/support/annotation/StringDef
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            FRAGMENT_INFO,
            FRAGMENT_CHAT,
            FRAGMENT_PARTICIPANTS
    })
    public @interface EventFragmentDef {
    }

    public static final int FRAGMENT_INFO = 0;
    public static final int FRAGMENT_CHAT = 1;
    public static final int FRAGMENT_PARTICIPANTS = 2;

    private ArrayList<EventFragment> fragments = new ArrayList<>();


    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case FRAGMENT_INFO:
                EventInfoFragment ifm = EventInfoFragment.newInstance();
                fragments.add(ifm);
                return ifm;
            case FRAGMENT_CHAT:
                EventChatFragment cfm = EventChatFragment.newInstance();
                fragments.add(cfm);
                return cfm;
            case FRAGMENT_PARTICIPANTS:
                EventParticipantsFragment pfm = EventParticipantsFragment.newInstance();
                fragments.add(pfm);
                return pfm;
            default:
                return null;
        }
    }
    public EventFragment getFragment(int id){
        return fragments.get(id);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
