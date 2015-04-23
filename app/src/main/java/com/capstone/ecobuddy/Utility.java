package com.capstone.ecobuddy;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Utility {
    public static String currentFragment = "";

    /**
     * Convert Location to LatLng
     * @param location
     * @return
     */
    public static LatLng latLngFromLocation(Location location) {
        LatLng latLng;

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        return latLng;
    }
}
