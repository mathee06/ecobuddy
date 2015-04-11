package com.capstone.ecobuddy;

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
        Directions tmpDirections;

        if(this.stops.size() > 0) {

            // * Add the directions from the origin to the first stop
            tmpDirections = new Directions(this.origin, stops.get(0));
            tmpDirections.connect();

            multiDirections.add(tmpDirections);

            // * Go through the other stops
            for(int i = 1; i < stops.size(); i++) {
                tmpDirections = new Directions(stops.get(i - 1), stops.get(i));
                tmpDirections.connect();

                multiDirections.add(tmpDirections);
            }

            // * Add the directions from the final stop to the destination
            tmpDirections = new Directions(stops.get(stops.size() - 1), this.destination);
            tmpDirections.connect();

            multiDirections.add(tmpDirections);

        } else {

            // * There should be at least one stop
            //   - Use Directions instead next time
            tmpDirections = new Directions(this.origin, this.destination);
            tmpDirections.connect();

            multiDirections.add(tmpDirections);
        }
    }

    /**
     * Get multi directions
     * @return
     */
    public ArrayList<Directions> getDirections() {
        return multiDirections;
    }
}
