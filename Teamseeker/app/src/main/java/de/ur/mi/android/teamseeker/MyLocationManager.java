package de.ur.mi.android.teamseeker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.ur.mi.android.teamseeker.helpers.OverlayActivity;

public final class MyLocationManager {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 619;
    private static MyLocationManager instance;

    public MyLocationManager(Context context) {
        instance = this;
        requestLocationPermission(context);
    }

    public static MyLocationManager getInstance() {
        return instance;
    }

    /**
     * requestLocationPermission() - Checks if permissions have been granted, asks for permissions if not granted
     */
    public static boolean requestLocationPermission(Context context) {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ActivityCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((AppCompatActivity)context, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    /**
     * getDeviceLocation() - tries to find device location, call only when location permission is granted
     *
     * @param onSuccessListener listens for success
     */
    public static void getDeviceLocation(Context context, final OnSuccessListener<Location> onSuccessListener) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    onSuccessListener.onSuccess(location);
                }
            });
        } catch (SecurityException e) {
        }
    }

    /**
     * distanceTo() - Returns distance in metres
     *
     * @param p1 latitude and longitude values for Point 1
     * @param p2 latitude and longitude values for Point 1
     * @return metres between Point 1 and 2
     */
    public static double distanceTo(LatLng p1, LatLng p2) {
        float[] m = new float[1];
        try {
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, m);
        } catch (NullPointerException e) {
        }
        return (double) m[0];
    }
    /**
     * getAddressFromLocation() - maps a location to an address, call only when location permission is granted
     *
     * @param coords argument passed by location
     * @return null
     */
    public static String getAddressFromLocation(Context context, LatLng coords) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(coords.latitude, coords.longitude, 1);
            if (addresses.size() < 1) {
                return null;
            }
            return addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
}
