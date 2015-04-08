package com.capstone.ecobuddy;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Mathee on 2015-04-08.
 */
public class ChargeLocations {

    private final String LOG_TAG = ChargeLocations.class.getSimpleName();

    // Charging Location Variables
    public String ID;
    public String UUID;
    public String address;
    public String town;
    public String stateOrProvince;
    public String latitude;
    public String longitude;
    public String contactNum;

    public ChargeLocations (String ID, String UUID, String address, String town, String stateOrProvince, String latitude, String longitude, String contactNum) {
        this.ID = ID;
        this.UUID = UUID;
        this.address = address;
        this.town = town;
        this.stateOrProvince = stateOrProvince;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNum = contactNum;
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
                    MapsFragment.mMap.addMarker(new MarkerOptions()
                            .position(getCoords())
                            .title("Charge Location ID: ".concat(getID()))
                            .snippet("Location Address: ".concat(getAddress()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            });
        }
    }

    String getID() {
        return ID;
    }

    String getAddress() {
        return address;
    }

    LatLng getCoords() {
        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }

}
