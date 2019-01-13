package de.ur.mi.android.teamseeker.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Calendar;

import de.ur.mi.android.teamseeker.ChatMessage;
import de.ur.mi.android.teamseeker.DatabaseManager;
import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.OnDataUpdateReceivedListener;
import de.ur.mi.android.teamseeker.R;

import static android.app.Activity.RESULT_OK;

public class ChatService extends Service {

    private ArrayList<EventData> localEventData = new ArrayList<>();

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (intent.hasExtra(getString(R.string.intent_chatservice_events))) {
            localEventData = bundle.getParcelableArrayList(getString(R.string.intent_chatservice_events));
            subscribeToEvents(localEventData);
        } else {
            unsubscribeEvents();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unsubscribeEvents();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void unsubscribeEvents() {
        for (EventData data : localEventData) {
            DatabaseManager.unsubscribeEventUpdates(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, data.getEventID());
        }

    }

    private void subscribeToEvents(final ArrayList<EventData> intentEvents) {
        for (EventData data : intentEvents) {
            DatabaseManager.subscribeToEventUpdates(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, data.getEventID(), new OnDataUpdateReceivedListener() {
                @Override
                public void onDataUpdateReceived(int source, EventData cloudEventData, int resultCode) {
                    if(source == DatabaseManager.SOURCE_LOCAL){
                        return;
                    }
                    if (resultCode == RESULT_OK) {
                        for (EventData localData : localEventData) {
                            if (localData.getEventID().equals(cloudEventData.getEventID())) {
                                localEventData.set(localEventData.indexOf(localData), cloudEventData);
                                break;
                            }
                        }
                        if (cloudEventData.hasNewMessage()) {
                            if (!isChatInForeground()) {
                                sendNotification(cloudEventData);
                            }
                        }
                    }
                }
            });
        }
    }

    private boolean isChatInForeground() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (EventData data : localEventData) {
            String key = getString(R.string.prefs_chatservice_helper) + data.getEventID();
            if (sharedPreferences.contains(key) &&
                    sharedPreferences.getBoolean(key, true)) {
                return true;
            }
        }
        return false;
    }

    private void sendNotification(EventData cloudData) {
        Intent intent = new Intent(getApplicationContext(), EventActivity.class);
        intent.putExtra(getString(R.string.event_intent_key), cloudData);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ChatMessage newMessage = cloudData.getChatMessages().get(cloudData.getChatMessages().size() - 1);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_id), NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(getString(R.string.notification_channeldesc));
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.notification_channel_id))
                .setContentTitle(cloudData.getEventName())
                .setContentText(getString(R.string.notification_messagefrom) + newMessage.getUserName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(newMessage.getUserName() + "\n " + newMessage.getMessage()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(0, mBuilder.build());
    }
}
