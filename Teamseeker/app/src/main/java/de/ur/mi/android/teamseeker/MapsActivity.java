package de.ur.mi.android.teamseeker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import org.joda.time.LocalTime;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.filter.Filter;
import de.ur.mi.android.teamseeker.fragments.EventFilterFragment;

import de.ur.mi.android.teamseeker.helpers.OverlayActivity;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;
import de.ur.mi.android.teamseeker.services.ChatService;
import de.ur.mi.android.teamseeker.services.ServiceManager;


public class MapsActivity extends OverlayActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {


    private GoogleMap mMap;
    private Toast backToast;
    EventFilterFragment frag;
    private FloatingActionButton floatingActionButton;
    private Marker marker;
    private ArrayList<Marker> markerlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_maps);
        floatingActionButton = findViewById(R.id.searchEventBtn);
        frag = new EventFilterFragment();
        setupMap();
        setupEventManager();
    }

    private void setupEventManager() {
        if (checkAppStart()) {
            MyEventsManager.initialize(MapsActivity.this, new OnCompleteListener() {
                @Override
                public void onComplete(int resultCode) {
                    MapsActivity.super.updateDrawerMenuItem(DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
                }
            });
        }
    }

    private boolean checkAppStart() {
        return getIntent() != null && getIntent().hasExtra(getString(R.string.app_started));
    }

    /**
     * onBackPressed() - Moves back to previous activity, shows Toast
     */
    @Override
    public void onBackPressed() {
        if (frag.isVisible()) {
            toggleFilterWindow();
            return;
        }
        if (backToast != null && backToast.getView().getWindowToken() != null) {
            moveTaskToBack(true);
            backToast.cancel();
        } else {
            backToast = Toast.makeText(MapsActivity.this, R.string.toast_pressagaintoexit, Toast.LENGTH_SHORT);
            backToast.show();
        }
    }

    @Override
    protected void onOverlayReady() {
        //Nothing to do
    }

    /**
     * setEventMarker() - sets event marker with corresponding settings
     *
     * @param eventID       given event ID
     * @param eventLocation Event Coordinates
     * @param eventType     used event type
     */
    public void setEventMarker(String eventID, LatLng eventLocation, String eventType) {
        int eventIndex = ArrayUtils.indexOf(getResources().getStringArray(R.array.eventType), eventType);
        BitmapDescriptor bitmapDescriptor = null;
        switch (eventIndex) {
            case 0: //Board Games
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_boardgames);
                break;
            case 1: //Movies
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_movies);
                break;
            case 2: //Sports
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_sports);
                break;
            case 3: //Cooking
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_cooking);
                break;
            case 4: //Chill
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_chill);
                break;
            case 5: //Party
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bmp_party);
                break;
            default:
                break;
        }
        marker = mMap.addMarker(
                new MarkerOptions()
                        .position(eventLocation)
                        .snippet(eventID)
                        .icon(bitmapDescriptor));
        markerlist.add(marker);

        marker.setVisible(true);
    }

    /**
     * moveCameraView() - moves Camera view on Map
     *
     * @param latlng Target Coordinates
     * @param zoom   Zoom setting on view
     */
    public void moveCameraView(LatLng latlng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    /**
     * onMarkerClick() - Handles clicking an event marker, moves camera to event and shows its eventscreen (EventActivity.class)
     *
     * @param marker Marker clicked
     * @return returns false by default
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng markerposition = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        moveCameraView(markerposition, getResources().getInteger(R.integer.default_zoom));
        DatabaseManager.getData(EventData.class, DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, marker.getSnippet(), new OnDataDownloadCompleteListener<EventData>() {
            @Override
            public void onDataDownloadComplete(List<EventData> data, int resultCode) {
                switch (resultCode) {
                    case RESULT_OK:
                        EventData eventData = data.get(0);

                        Intent switchToEventActivity = new Intent(MapsActivity.this, EventActivity.class);
                        switchToEventActivity.putExtra(getString(R.string.event_intent_key), eventData);
                        startActivity(switchToEventActivity);
                        break;
                    default:
                        break;
                }
            }
        });

        return false;
    }

    /**
     * setupMap() - sets map up
     */
    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_mainMap);
        mapFragment.getMapAsync(this);
    }

    /**
     * onSearchEventsClicked() - Method called when Floating Action Button is clicked
     *
     * @param v View parameter
     */
    public void onSearchEventsClicked(View v) {
        Filter filter = Filter.all();
        searchEventsByFilter(filter);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.anim_rotate);
        floatingActionButton.startAnimation(animation);
    }

    /**
     * deleteMarkers() - Deletes all markers found in "markerlist"
     */
    public void deleteMarkers() {

        for (Marker marker : markerlist) {
            marker.remove();
        }
    }

    /**
     * searchEventsByFilter() - Searches for all events with a set filter from the EventFilterFragment.class
     *
     * @param filter filter passed from EventFilterFragment method applyFilter()
     */
    public void searchEventsByFilter(final Filter filter) {

        DatabaseManager.getEventData(this, DatabaseManager.DB_KEY_EVENT, filter, new OnDataDownloadCompleteListener<EventData>() {
            @Override
            public void onDataDownloadComplete(List<EventData> data, int resultCode) {
                if (resultCode == RESULT_OK && data != null) {
                    String eventID;
                    LatLng eventLocation;
                    String eventType;
                    deleteMarkers();

                    for (EventData event : data) {
                        eventID = event.getEventID();
                        eventLocation = new LatLng(event.getEventLatitude(), event.getEventLongitude());
                        eventType = event.getEventType();

                        setEventMarker(eventID, eventLocation, eventType);

                    }
                }

            }
        });
    }

    /**
     * onMapReady() - method given by Google Maps API for Android
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocalTime localTime = new LocalTime();
        if (localTime.getHourOfDay() > 18 || localTime.getHourOfDay() < 6) {
            googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        }
        mMap.setOnMarkerClickListener(MapsActivity.this);
        onSearchEventsClicked(null);
        super.requestLocationPermission(new OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
                if (resultCode == RESULT_OK) {
                    MyLocationManager.getDeviceLocation(MapsActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * toggleFilterWindow() - method for toggling filter fragment
     */
    public void toggleFilterWindow() {

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        if (!frag.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container_filter, frag)
                    .show(frag)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .hide(frag)
                    .commit();
        }
    }


}
