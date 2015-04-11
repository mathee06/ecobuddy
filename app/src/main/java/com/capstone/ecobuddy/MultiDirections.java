package com.capstone.ecobuddy;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Amir on 2015-04-10.
 */
public class MultiDirections {

    // Field variables
    private String origin;
    private ArrayList<String> stops;
    private String destination;

    // * Directions
    private ArrayList<Directions> multiDirections;

    /**
     * Multi directions
     * - Used for rerouting
     * @param origin
     * @param destination
     */
    public MultiDirections(String origin, String destination) {
        this.origin = origin;
        stops = new ArrayList<String>();
        this.destination = destination;
        multiDirections = new ArrayList<Directions>();
    }

    /**
     * Add a stop
     * - Used in rerouting to add a gas/charging station
     * @param stop
     */
    public void addAStop(String stop) {
        stops.add(stop);
    }

    /**
     * Connect to the http server
     * - Get the routes
     * - Called after all stops are added
     */
    public void connect() {

        // Make sure that there is at least one stop
        Directions tmp;

        if(this.stops.size() > 0) {

            // * Add the directions from the origin to the first stop
            tmp = new Directions(this.origin, stops.get(0));
            tmp.connect();
            multiDirections.add(tmp);

            // * Go through the other stops
            for(int i = 1; i < stops.size(); i++) {
                tmp = new Directions(stops.get(i - 1), stops.get(i));
                tmp.connect();
                multiDirections.add(tmp);
            }

            // * Add the directions from the final stop to the destination
            tmp = new Directions(stops.get(stops.size() - 1), this.destination);
            tmp.connect();
            multiDirections.add(tmp);

        } else {

            // * There should be at least one stop
            //   - Use Directions instead next time
            tmp = new Directions(this.origin, this.destination);
            tmp.connect();
            multiDirections.add(tmp);
        }
    }

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

    /**
     * Get multi directions
     * @return
     */
    public ArrayList<Directions> getDirections() {
        return multiDirections;
    }
}
