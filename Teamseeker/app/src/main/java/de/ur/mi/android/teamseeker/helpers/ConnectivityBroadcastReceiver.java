package de.ur.mi.android.teamseeker.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    /**
     * Sends a broadcast to the activity that is currently listening for broadcasts
     * whenever the connection has been lost or re-established
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        ((OverlayActivity) context).onConnectivityStatusChanged(isConnected);
    }
}
