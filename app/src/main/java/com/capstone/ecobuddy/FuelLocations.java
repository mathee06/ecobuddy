package com.capstone.ecobuddy;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Mathee on 2015-04-08.
 */
public class FuelLocations {

    private final String LOG_TAG = FuelLocations.class.getSimpleName();

    // Charging Location Variables
    public String placeID;
    public String name;
    public String address;
    public String latitude;
    public String longitude;

    public FuelLocations (String ID, String latitude, String longitude) {
        this.placeID = ID;
        this.latitude = latitude;
        this.longitude = longitude;
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
                    Marker fuelMarkers = MapsFragment.mMap.addMarker(new MarkerOptions()
                            .position(getCoords())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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

    LatLng getCoords() {
        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }
}
