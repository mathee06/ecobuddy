package com.capstone.ecobuddy;

import com.google.android.gms.maps.model.LatLng;

public class Utility {
    public static String currentFragment = "";

    /**
     * Convert lat/lng to string format
     * - To be used in http request
     */
    public static String latLngToStringFormat(LatLng latLng) {
        StringBuilder sb = new StringBuilder();

        sb.append(Double.valueOf(latLng.latitude).toString());
        sb.append(",");
        sb.append(Double.valueOf(latLng.longitude).toString());

        return sb.toString();
    }
}
