package de.ur.mi.android.teamseeker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.helpers.OverlayActivity;
import de.ur.mi.android.teamseeker.services.ChatService;
import de.ur.mi.android.teamseeker.services.ServiceManager;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public final class MyEventsManager {

    public static void initialize(Context context, OnCompleteListener onCompleteListener){
        getMyEventsFromPreferences(context, onCompleteListener);
    }


    private static ArrayList<EventData> myEvents = new ArrayList<>();

    /**
     * getMyEvents() - Returns the user's "My Event" list
     *
     * @return ArrayList
     */
    public static ArrayList<EventData> getMyEvents() {
        return myEvents;
    }

    /**
     * addToMyEvents() - method for adding subscribed events to user's "My Events" list
     *
     * @param eventData eventData to be saved
     * @return
     */
    public static boolean addToMyEvents(Context context, EventData eventData) {
        if (!myEvents.contains(eventData)) {
            myEvents.add(eventData);
            storeMyEventsInPreferences(context);
            return true;
        }
        return false;
    }

    /**
     * removeFromMyEvents() - removes an entry from your "My Events" list
     *
     * @param eventData
     * @return
     */
    public static boolean removeFromMyEvents(Context context, EventData eventData) {
        EventData toRemove = null;
        for (EventData event : myEvents) {
            if (event.equals(eventData)) {
                toRemove = event;
            }
        }
        if (toRemove != null) {
            myEvents.remove(toRemove);
            storeMyEventsInPreferences(context);
            return true;
        } else {
            return false;
        }
    }

    private static void getMyEventsFromPreferences(final Context context, final OnCompleteListener onCompleteListener) {
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(context.getString(R.string.prefs_myevents) + userID)) {
            ArrayList<String> documentIds = new ArrayList<>(prefs.getStringSet(context.getString(R.string.prefs_myevents) + userID, null));
            DatabaseManager.getDocumentsById(EventData.class, DatabaseManager.DB_KEY_EVENT, documentIds, new de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener<EventData>() {
                @Override
                public void onDataDownloadComplete(List<EventData> data, int resultCode) {
                    if (resultCode == RESULT_OK && data != null) {
                        ArrayList<EventData> newMyEvents = new ArrayList<>();
                        for (EventData event : data) {
                            if (event != null && event.getParticipants() != null && event.getParticipants().contains(userID)) {
                                newMyEvents.add(event);
                            }
                        }
                        myEvents = newMyEvents;
                        onCompleteListener.onComplete(RESULT_OK);
                        storeMyEventsInPreferences(context); //Updates events
                    } else {
                        onCompleteListener.onComplete(RESULT_CANCELED);
                        //Events couldn't be retrieved
                    }
                    startChatService(context);
                }
            });
        } else {
            startChatService(context);
        }
    }

    private static void startChatService(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        intent.putExtra(context.getString(R.string.intent_chatservice_events), myEvents);

        ServiceManager.getInstance().createServiceInstance(ChatService.class, intent);
    }

    private static void storeMyEventsInPreferences(final Context context) {
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<String> eventIDs = new ArrayList<>();
        for (EventData event : myEvents) {
            eventIDs.add(event.getEventID());
        }
        DatabaseManager.getDocumentIds(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, eventIDs, new de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener<String>() {
            @Override
            public void onDataDownloadComplete(List<String> data, int resultCode) {
                Set<String> documentIds = new HashSet<>(data);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet(context.getString(R.string.prefs_myevents) + userID, documentIds);
                editor.apply();
            }
        });
    }
}
