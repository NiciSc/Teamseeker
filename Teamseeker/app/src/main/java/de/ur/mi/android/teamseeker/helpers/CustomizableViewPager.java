package de.ur.mi.android.teamseeker.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.adapters.EventPagerAdapter;

public class CustomizableViewPager extends ViewPager {
    private boolean allowSwiping = true;

    public CustomizableViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomizableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return allowSwiping && super.onInterceptTouchEvent(ev);
            default:
                return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return allowSwiping && super.onTouchEvent(ev);
            default:
                return super.onTouchEvent(ev);
        }
    }

    public EventFragment getCurrentFragment() {
        return ((EventPagerAdapter) getAdapter()).getFragment(getCurrentItem());
    }

    public void setSwipeAllowed(boolean allowSwiping) {
        this.allowSwiping = allowSwiping;
    }

}
