package de.ur.mi.android.teamseeker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.Interfaces.OnDataUpdateReceivedListener;
import de.ur.mi.android.teamseeker.adapters.EventPagerAdapter;
import de.ur.mi.android.teamseeker.filter.Filter;
import de.ur.mi.android.teamseeker.fragments.EventEditFragment;
import de.ur.mi.android.teamseeker.helpers.OverlayActivity;
import de.ur.mi.android.teamseeker.helpers.Utility;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;
import de.ur.mi.android.teamseeker.interfaces.OnDataUploadCompleteListener;
import de.ur.mi.android.teamseeker.services.ChatService;
import de.ur.mi.android.teamseeker.services.ServiceManager;

public class EventActivity extends OverlayActivity {

    private EventData currentEvent;
    private GoogleMap mMap = null;
    private CameraPosition creationStartPosition;

    boolean creationMode = false;

    private Button button_changeEventParticipationState, button_editEvent;
    private ViewGroup viewGroup_event_show, viewGroup_event_edit, viewGroup_event_create;
    private EventViewManager eventViewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_event);
        getViews();
        currentEvent = processIntent();
        setupEventViewManager();
        setupSimpleMap();
        setGroupViewVisibility();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        toggleChatServiceHelper(hasFocus);
        if (userIsParticipant()) {
            if (hasFocus) {
                subscribeEvent();
            } else {
                unsubscribeEvent();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (eventViewManager.getCurrentFragmentType() != EventViewManager.FragmentType.EDIT || creationMode) {
            switchActivity(MapsActivity.class);
        } else {
            eventViewManager.enableShowMode();
            setGroupViewVisibility();
            setButtonState();
        }
    }

    @Override
    protected void onOverlayReady() {
        hideToolbarItem(R.id.action_filter);
    }

    //region Setup
    //region Intent handling
    private EventData processIntent() {
        Intent intent = getIntent();
        String extraKey = getString(R.string.event_intent_key);
        if (intent.hasExtra(extraKey)) {
            return intent.getParcelableExtra(extraKey);
        } else {
            creationMode = true;
            return new EventData(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }
    //endregion

    //region UI Setup
    private void getViews() {
        viewGroup_event_create = findViewById(R.id.viewGroup_event_create);
        viewGroup_event_edit = findViewById(R.id.viewGroup_event_edit);
        viewGroup_event_show = findViewById(R.id.viewGroup_event_show);
    }
    //endregion

    //region Manager Setup
    private void setupEventViewManager() {
        eventViewManager = new EventViewManager(this, findViewById(R.id.layout_event), creationMode);
        if (!userIsParticipant()) {
            eventViewManager.setSwipeAllowed(false);
        }
    }
    //endregion

    //region Map Setup
    private void setupSimpleMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_eventMap);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                UiSettings settings = mMap.getUiSettings();
                settings.setMapToolbarEnabled(false);
                if (creationMode) {
                    EventActivity.super.requestLocationPermission(new OnCompleteListener() {
                        @Override
                        public void onComplete(int resultCode) {
                            if (resultCode == RESULT_OK) {
                                MyLocationManager.getDeviceLocation(EventActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        currentEvent.setEventLatitude(latLng.latitude);
                                        currentEvent.setEventLongitude(latLng.longitude);
                                        changeMapViewAndLocation(latLng, null);
                                        creationStartPosition = mMap.getCameraPosition();
                                        setOnMapClickListener();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    LatLng coords = new LatLng(currentEvent.getEventLatitude(), currentEvent.getEventLongitude());
                    changeMapViewAndLocation(coords, null);
                }
                setOnMapClickListener();
            }
        });
    }

    private void setOnMapClickListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                switch (eventViewManager.getCurrentFragmentType()) {
                    case EDIT:
                        try {
                            startActivityForResult(builder.build(EventActivity.this), 1);
                        } catch (GooglePlayServicesRepairableException e) {

                        } catch (GooglePlayServicesNotAvailableException e) {

                        }
                        break;
                    default: //anything but EDIT
                        final LatLng eventLoc = new LatLng(currentEvent.getEventLatitude(), currentEvent.getEventLongitude());

                        EventActivity.super.requestLocationPermission(new OnCompleteListener() {
                            @Override
                            public void onComplete(int resultCode) {
                                String title = null;
                                if (resultCode == RESULT_OK) {
                                    title = MyLocationManager.getAddressFromLocation(EventActivity.this, eventLoc);
                                }
                                String geoUri = "http://maps.google.com/maps?q=loc:" + eventLoc.latitude + "," + eventLoc.longitude + title;
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                                intent.setPackage("com.google.android.apps.maps");
                                startActivity(intent);
                            }
                        });
                        break;
                }
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                LatLng coord = place.getLatLng();
                currentEvent.setEventLatitude(coord.latitude);
                currentEvent.setEventLongitude(coord.longitude);
                changeMapViewAndLocation(coord, (String) (place.getName()));
            }
        }
    }
    //endregion

    //endregion

    //region UI Management
    public void setButtonState() {
        button_changeEventParticipationState.setVisibility(View.INVISIBLE);
        button_editEvent.setVisibility(View.INVISIBLE);
        switch (eventViewManager.getCurrentFragmentType()) {
            case EDIT:
                button_editEvent.setVisibility(View.VISIBLE);
                button_editEvent.setBackground(getResources().getDrawable(R.drawable.drawable_savebutton));
                button_editEvent.setText(R.string.event_save);

                break;
            default:
                button_changeEventParticipationState.setVisibility(View.VISIBLE);
                if (userIsParticipant()) {
                    if (userIsHost()) {
                        button_editEvent.setVisibility(View.VISIBLE);
                        button_editEvent.setBackground(getResources().getDrawable(R.drawable.drawable_savebutton));
                        button_editEvent.setText(R.string.event_edit);
                    }
                    button_changeEventParticipationState.setBackground(getResources().getDrawable(R.drawable.drawable_leavebutton));
                    button_changeEventParticipationState.setText(R.string.event_leave);
                } else if (currentEvent.getParticipants().size() >= currentEvent.getMaxParticipants()) {
                    button_changeEventParticipationState.setBackground(getResources().getDrawable(R.drawable.drawable_joinbutton_full));
                    button_changeEventParticipationState.setText(R.string.event_join);
                } else {
                    button_changeEventParticipationState.setBackground(getResources().getDrawable(R.drawable.drawable_joinbutton));
                    button_changeEventParticipationState.setText(R.string.event_join);
                }
                break;
        }
    }

    private void setGroupViewVisibility() {
        viewGroup_event_create.setVisibility(View.GONE);
        viewGroup_event_edit.setVisibility(View.GONE);
        viewGroup_event_show.setVisibility(View.GONE);
        switch (eventViewManager.getCurrentFragmentType()) {
            case INFO:
                viewGroup_event_show.setVisibility(View.VISIBLE);
                break;
            case EDIT:
                if (creationMode) {
                    viewGroup_event_create.setVisibility(View.VISIBLE);
                } else {
                    viewGroup_event_edit.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
    //endregion

    //region External Calls
    public void setButtonReferences(Button button_changeEventParticipationState, Button button_editEvent) {
        this.button_editEvent = button_editEvent;
        this.button_changeEventParticipationState = button_changeEventParticipationState;
    }

    @NonNull
    public EventData getCurrentEvent() {
        return currentEvent;
    }

    //endregion

    //region On Click Methods


    public void onClickEdit() {
        switch (eventViewManager.getCurrentFragmentType()) {
            case EDIT:
                final EventData collectedData = ((EventEditFragment) eventViewManager.getCurrentFragment()).collectEventData();
                if (collectedData != null) {
                    if (creationMode && mMap.getCameraPosition().equals(creationStartPosition)) {
                        Utility.createAlertDialog(EventActivity.this, true, getString(R.string.warning_locationunchanged), getString(R.string.create_event), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentEvent = collectedData;
                                createEvent();
                            }
                        }, null, null, getString(R.string.edit_event), null);
                    } else {
                        currentEvent = collectedData;
                        createEvent();
                    }
                } else {
                    Toast.makeText(EventActivity.this, R.string.error_eventnotpopulated, Toast.LENGTH_SHORT).show();
                }
                break;
            case INFO:
                eventViewManager.enableEditMode();
                setGroupViewVisibility();
                break;
        }
    }

    public void onClickParticipation() {
        final String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userIsParticipant()) {
            if (userIsHost()) {
                if (currentEvent.getParticipants().size() <= 1) {
                    Utility.createAlertDialog(EventActivity.this, true, getString(R.string.warning_lastparticipant), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteEvent();
                        }
                    }, getString(R.string.cancel), null, null, null);
                } else {
                    Utility.createAlertDialog(EventActivity.this, true, getString(R.string.warning_newhost), getString(R.string.leave_event), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utility.createAlertDialog(EventActivity.this, true, getString(R.string.warning_second_newhost), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    leaveEvent();
                                    handleHostMigration();
                                }
                            }, getString(R.string.cancel), null, getString(R.string.delete_event), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteEvent();
                                }
                            });
                        }
                    }, getString(R.string.cancel), null, getString(R.string.delete_event), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteEvent();
                        }
                    });
                }
            } else {
                leaveEvent();
            }
        } else {
            if (currentEvent.getParticipants().size() >= currentEvent.getMaxParticipants()) {
                Toast.makeText(EventActivity.this, R.string.error_eventfull, Toast.LENGTH_LONG).show();
                return;
            }
            final String userBirthday = PreferenceManager.getDefaultSharedPreferences(EventActivity.this).getString(UserData.USERBIRTHDATE_KEY + uID, null);
            if (userBirthday == null) {
                DatabaseManager.getData(UserData.class, DatabaseManager.DB_KEY_EVENT, UserData.USERID_KEY, uID, new OnDataDownloadCompleteListener<UserData>() {
                    @Override
                    public void onDataDownloadComplete(List<UserData> data, int resultCode) {
                        if (resultCode == RESULT_OK) {
                            UserData userData = data.get(0);
                            int userAge = Utility.calculateAge(userData.getBirthDate());
                            handleAgeCheck(userAge);
                        } else {
                            Toast.makeText(EventActivity.this, R.string.error_profilecorrupted, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                int userAge = Utility.calculateAge(userBirthday);
                handleAgeCheck(userAge);
            }
        }
    }

    public void onInfoClicked(View v) {
        if (eventViewManager.getCurrentFragmentType() == EventViewManager.FragmentType.INFO) {
            return;
        }
        eventViewManager.manualSwipe(EventPagerAdapter.FRAGMENT_INFO);

    }

    public void onChatClicked(View v) {
        if (!userIsParticipant()) {
            Toast.makeText(EventActivity.this, R.string.error_noaccess, Toast.LENGTH_LONG).show();
            return;
        }
        if (eventViewManager.getCurrentFragmentType() == EventViewManager.FragmentType.CHAT) {
            return;
        }
        eventViewManager.manualSwipe(EventPagerAdapter.FRAGMENT_CHAT);
        //customFragmentManager.switchToFragment(R.layout.fragment_chat);
    }

    public void onPartyClicked(View v) {
        if (!userIsParticipant()) {
            Toast.makeText(EventActivity.this, R.string.error_noaccess, Toast.LENGTH_LONG).show();
            return;
        }
        if (eventViewManager.getCurrentFragmentType() == EventViewManager.FragmentType.PARTICIPANTS) {
            return;
        }
        eventViewManager.manualSwipe(EventPagerAdapter.FRAGMENT_PARTICIPANTS);
    }

    //endregion

    //region Event Utility (Join, Leave etc.)
    private void createEvent() {
        if (creationMode) {
            DatabaseManager.addData(currentEvent, DatabaseManager.DB_KEY_EVENT, new OnDataUploadCompleteListener<EventData>() {
                @Override
                public void onDataUploadComplete(EventData data, int resultCode) {
                    if (MyEventsManager.addToMyEvents(EventActivity.this, currentEvent)) {
                        updateDrawerMenuItem(OverlayActivity.DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
                    }
                    subscribeEvent();
                }
            });

        } else {
            DatabaseManager.updateData(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, currentEvent.getEventID(), currentEvent, new OnCompleteListener() {
                @Override
                public void onComplete(int resultCode) {

                }
            });
        }
        creationMode = false;

        eventViewManager.enableShowMode();
        eventViewManager.setSwipeAllowed(true);

        setGroupViewVisibility();
        setButtonState();
        updateChatService();
    }

    private void joinEvent() {
        final String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        currentEvent.userJoined(uID);
        if (MyEventsManager.addToMyEvents(this, currentEvent)) {
            updateDrawerMenuItem(OverlayActivity.DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
        }

        eventViewManager.setSwipeAllowed(true);
        updateCloudParticipantList(true);
        subscribeEvent();
        setButtonState();
        updateChatService();
    }

    private void leaveEvent() {
        final String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        currentEvent.userLeft(uID);
        if (MyEventsManager.removeFromMyEvents(this, currentEvent)) {
            updateDrawerMenuItem(OverlayActivity.DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
        }
        eventViewManager.setSwipeAllowed(false);
        updateCloudParticipantList(true);
        unsubscribeEvent();
        setButtonState();
        updateChatService();
    }

    private void deleteEvent() {
        unsubscribeEvent();
        if (MyEventsManager.removeFromMyEvents(this, currentEvent)) {
            updateDrawerMenuItem(OverlayActivity.DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
        }
        updateChatService();
        DatabaseManager.deleteDocument(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, currentEvent.getEventID(), new OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
                switchActivity(MapsActivity.class);
            }
        });

    }

    public void kickUser(String userID) {
        currentEvent.userLeft(userID);
        updateCloudParticipantList(false);
    }

    public boolean userIsParticipant() {
        return currentEvent.getParticipants().contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public boolean userIsHost() {
        return currentEvent.getEventID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    /**
     * Since users can only have one event, only users who are not hosts of another event are eligible for becoming the new host
     * This method checks if there are any valid hosts and attempts to migrate the host, if no host is found the event is deleted
     */
    private void handleHostMigration() {

        Filter filter = Filter.empty();
        for (String uID : currentEvent.getParticipants()) {
            filter.addFilter(Filter.FILTER_ID, uID);
        }
        DatabaseManager.getEventData(this, DatabaseManager.DB_KEY_EVENT, filter, new OnDataDownloadCompleteListener<EventData>() {
            @Override
            public void onDataDownloadComplete(List<EventData> data, int resultCode) {
                ArrayList<String> eventIDs = new ArrayList<>();
                for (EventData event : data) {
                    eventIDs.add(event.getEventID());
                }
                ArrayList<String> validHosts = new ArrayList<>();
                validHosts.addAll(currentEvent.getParticipants());
                validHosts.removeAll(eventIDs);

                if (validHosts.size() > 0) {
                    updateEventID(currentEvent.getEventID(), validHosts.get(0));
                } else {
                    deleteEvent();
                }
            }
        });
    }

    /**
     * Subscribe to event to get latest version including event info and participant list
     */
    private void subscribeEvent() {
        DatabaseManager.subscribeToEventUpdates(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, currentEvent.getEventID(), new OnDataUpdateReceivedListener() {
            @Override
            public void onDataUpdateReceived(int source, EventData data, int result) {
                if (result != RESULT_OK || data == null) {
                    return;
                }
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                switch (source) {
                    case DatabaseManager.SOURCE_LOCAL:
                        if (!data.hasParticipant(userID)) {
                            return;
                        }
                        break;
                    case DatabaseManager.SOURCE_DB:
                        if (!data.hasParticipant(userID)) {
                            if (currentEvent.hasParticipant(userID)) {
                                unsubscribeEvent();
                                Toast.makeText(EventActivity.this, getString(R.string.kicked_event) + data.getEventName(), Toast.LENGTH_LONG).show();
                                MyEventsManager.removeFromMyEvents(EventActivity.this, data);
                                if (hasWindowFocus() && currentEvent.equals(data)) {
                                    switchActivity(MapsActivity.class);
                                }
                                return;
                            }
                            return;
                        }
                        break;
                }
                EventViewManager.FragmentType fragType = eventViewManager.getCurrentFragmentType();
                EventFragment frag = (EventFragment) eventViewManager.getCurrentFragment();
                if (hasWindowFocus() && fragType != EventViewManager.FragmentType.EDIT) {
                    if (fragType != EventViewManager.FragmentType.CHAT) {
                        if (data.hasNewMessage()) {
                            Toast.makeText(EventActivity.this, R.string.unread_messages, Toast.LENGTH_SHORT).show();
                        }
                        if (fragType == EventViewManager.FragmentType.INFO) {
                            setButtonState();
                        }
                    }
                    frag.updateEventData(data);
                }
                EventActivity.this.currentEvent = data;
            }
        });
    }


    /*
    //Tested, doesn't work, very hard to implement with database based chat
    @SuppressWarnings("unchecked")
    private ArrayList<ChatMessage> fixMessageCollision(ArrayList<ChatMessage> newData, ArrayList<ChatMessage> currentData) {
        if(currentData == null || newData == null){
            return null;
        }
        ArrayList<ChatMessage> newCloudMessages = new ArrayList<>();
        ArrayList<ChatMessage> combinedUnsortedNewMessages = new ArrayList<>();

        int lastMatchingIndex = -1;
        for (int i = newData.size() - 1; i > 0; i--) {
            if (currentData.contains(newData.get(0))) {
                lastMatchingIndex = i;
                break;
            }
        }
        for (int i = lastMatchingIndex; i < newData.size(); i++) {
            if (!currentData.contains(newData.get(i))) {
                newCloudMessages.add(newData.get(i));
            }
        }
        combinedUnsortedNewMessages.addAll(currentData);
        combinedUnsortedNewMessages.addAll(newCloudMessages);

        Collections.sort(combinedUnsortedNewMessages);

        return combinedUnsortedNewMessages;
    }*/

    private void unsubscribeEvent() {
        DatabaseManager.unsubscribeEventUpdates(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, currentEvent.getEventID());
    }
    //endregion

    //region Database and Local update methods
    private void updateCloudParticipantList(final boolean showToast) {
        DatabaseManager.updateData(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, currentEvent.getEventID(), currentEvent, new OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
                if (!showToast) {
                    return;
                }
                if (userIsParticipant()) {
                    Toast.makeText(EventActivity.this, R.string.joined_event, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventActivity.this, R.string.left_event, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEventID(String oldID, String newID) {
        currentEvent.attemptSetHost(newID);
        DatabaseManager.updateData(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, oldID, currentEvent, new OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
            }
        });
    }

    //endregion

    //region Misc
    private void changeMapViewAndLocation(LatLng coord, String markerTitle) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(coord).title(markerTitle);
        mMap.addMarker(markerOptions).showInfoWindow();
    }

    private void updateChatService() {
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra(getString(R.string.intent_chatservice_events), MyEventsManager.getMyEvents());
        ServiceManager.getInstance().updateServiceInstance(ChatService.class, intent);
    }

    private void handleAgeCheck(int age) {
        if (age < currentEvent.getMinParticipantAge()) {
            Toast.makeText(EventActivity.this, R.string.error_agenomatch, Toast.LENGTH_LONG).show();
        } else {
            joinEvent();
        }
    }

    private void toggleChatServiceHelper(boolean status) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.prefs_chatservice_helper) + currentEvent.getEventID(), status);
        editor.apply();
    }
    //endregion
}
