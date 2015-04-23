package com.capstone.ecobuddy;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mathee on 2015-04-22.
 */
public class POILocations {
    private final String LOG_TAG = POILocations.class.getSimpleName();

    // Charging Location Variables
    public String placeID;
    public String name;
    public String address;
    public String latitude;
    public String longitude;
    public float markerShade;
    public static List<Marker> poiMarkerList = new ArrayList<Marker>();

    public POILocations (String ID, String name, String address, String latitude, String longitude, float markerShade) {
        this.placeID = ID;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerShade = markerShade;
        placeMarker();
    }

    void placeMarker() {
        final Handler mHandler = new Handler(Looper.getMainLooper());
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Marker poiMarker = MapsFragment.mMap.addMarker(new MarkerOptions()
                            .title("Location Name: ".concat(getName()))
                            .snippet("Address: ".concat(getAddress()))
                            .position(getCoords())
                            .icon(BitmapDescriptorFactory.defaultMarker(getShade())));
                    poiMarkerList.add(poiMarker);
                    Log.v(LOG_TAG, "MARKER LIST SIZE IS: " + poiMarkerList.size());
                }
            });
        }
    }

    String getPlaceID() {
        return placeID;
    }

    String getAddress() {
        return address;
    }

    String getName() { return name; }

    float getShade() { return markerShade; }

    LatLng getCoords() {
        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }
}
