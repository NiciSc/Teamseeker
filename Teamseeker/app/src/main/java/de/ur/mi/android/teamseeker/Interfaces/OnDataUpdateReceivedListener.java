package de.ur.mi.android.teamseeker.Interfaces;

import de.ur.mi.android.teamseeker.EventData;

public interface OnDataUpdateReceivedListener {
    void onDataUpdateReceived(int source, EventData data, int resultCode);
}
