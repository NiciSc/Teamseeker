package de.ur.mi.android.teamseeker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.adapters.EventPagerAdapter;
import de.ur.mi.android.teamseeker.fragments.EventEditFragment;
import de.ur.mi.android.teamseeker.helpers.CustomizableViewPager;

public class EventViewManager {

    private CustomizableViewPager viewPager;
    private EventPagerAdapter pagerAdapter;
    private EventEditFragment eventEditFragment;
    private FragmentManager fragmentManager;
    private Context context;
    private FragmentType fragmentType;
    private SeekBar progressBar;

    public enum FragmentType {
        INFO,
        CHAT,
        PARTICIPANTS,
        EDIT
    }

    public EventViewManager(Context context, View view, boolean creationMode) {
        this.context = context;
        if (creationMode) {
            fragmentType = FragmentType.EDIT;
        } else {
            fragmentType = FragmentType.INFO;
        }
        getViews(view);
        setupViewPager();
        setupFragmentManager();
        setupView(creationMode);
    }

    private void setupView(boolean creationMode) {
        if (creationMode) {
            viewPager.setVisibility(View.INVISIBLE);
        } else {
            disableEditMode();
        }
    }

    public Fragment getCurrentFragment() {
        if (viewPager.getVisibility() == View.VISIBLE) {
            return (Fragment)viewPager.getCurrentFragment();
        } else {
            return eventEditFragment;
        }
    }

    public FragmentType getCurrentFragmentType() {
        return fragmentType;
    }

    private void getViews(View view) {
        viewPager = view.findViewById(R.id.viewPager_event);
        progressBar = view.findViewById(R.id.seekBar_fragmentStatus);
    }

    private void setupViewPager() {
        pagerAdapter = new EventPagerAdapter(((AppCompatActivity) context).getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        //viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                setProgress(position);
                switch (position) {
                    case EventPagerAdapter.FRAGMENT_CHAT:
                        fragmentType = FragmentType.CHAT;
                        break;
                    case EventPagerAdapter.FRAGMENT_INFO:
                        fragmentType = FragmentType.INFO;
                        break;
                    case EventPagerAdapter.FRAGMENT_PARTICIPANTS:
                        fragmentType = FragmentType.PARTICIPANTS;
                        break;
                    default:
                        break;
                }
                EventFragment focusedFragment = viewPager.getCurrentFragment();
                focusedFragment.onFocusChanged(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(!((EventActivity)context).userIsParticipant()){
                    viewPager.setCurrentItem(0);
                }
            }
        });
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

    public EventPagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public void manualSwipe(@EventPagerAdapter.EventFragmentDef int position) {
        viewPager.setCurrentItem(position, true);
        setProgress(position);
    }

    public void setSwipeAllowed(boolean allowSwiping) {
        viewPager.setSwipeAllowed(allowSwiping);
    }

    private void setupFragmentManager() {
        fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        eventEditFragment = new EventEditFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_eventEdit, eventEditFragment, null);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void enableEditMode() {
        fragmentType = FragmentType.EDIT;
        disableShowMode();
        fragmentManager.beginTransaction().show(eventEditFragment).commit();
        eventEditFragment.onFocusChanged(true);
    }

    public void enableShowMode() {
        fragmentType = FragmentType.INFO;
        disableEditMode();
        viewPager.setVisibility(View.VISIBLE);
        EventFragment focusedFragment = (EventFragment) viewPager.getCurrentFragment();
        focusedFragment.onFocusChanged(true);
    }

    private void disableEditMode() {
        fragmentManager.beginTransaction().hide(eventEditFragment).commit();
    }

    private void disableShowMode() {
        viewPager.setVisibility(View.GONE);
    }

    private void setProgress(int position) {
        int progress = (int) ((double) position / (double) (pagerAdapter.getCount() - 1) * 100);
        progressBar.setProgress(progress);
    }

}
