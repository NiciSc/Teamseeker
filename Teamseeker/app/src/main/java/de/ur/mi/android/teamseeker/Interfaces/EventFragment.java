package de.ur.mi.android.teamseeker.Interfaces;

import android.content.Context;

import de.ur.mi.android.teamseeker.EventData;

public interface EventFragment {
    void updateEventData(EventData updatedEventData);
    void onFocusChanged(boolean hasFocus);
    void fetchEventData();
}
