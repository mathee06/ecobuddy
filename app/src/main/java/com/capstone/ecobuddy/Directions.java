package com.capstone.ecobuddy;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ariffia.
 */
public class Directions {

    // Http stuff
    private String request;
    private JSONObject json;

    // * List of steps to do the journey
    private ArrayList<Step> steps;

    private static String LOG_TAG = Directions.class.getSimpleName();

    /**
     * Directions Object Oriented
     * @param origin Current location
     * @param destination Destination to go to
     */
    public Directions(String origin, String destination) {

        // Make the request and get the json
        this.request = makeRequest(origin, destination);
    }

    /**
     * Connect
     * - Actually get the data from a http request
     * - Do this in a thread or it will crash because it takes time to get them
     */
    public void connect() {
        this.json = getJsonViaHttp();

        // Process the json and put the data in the variables
        if(getOkStatus()) {
            getData();
        } else {
            Log.v(this.getClass().getSimpleName(), "HTTP REQUEST FAILED");
        }
    }

    /**
     * Step
     * - This stores the information of each step
     * - To get to a destination we need to do this steps
     */
    public class Step {

        // Variables for this step
        int distanceInMeters;
        int durationInSeconds;
        String instructions;
        String travelMode;
        LatLng startLocation;
        LatLng endLocation;
        List<LatLng> polylinePoints;  // Used to draw a detailed route on the map

        /**
         * Step
         * - The constructor of the step
         * @param step
         * @throws JSONException
         */
        public Step(JSONObject step) throws JSONException {

            // Get some data and put them in the variables
            distanceInMeters = step.getJSONObject("distance").getInt("value");
            durationInSeconds = step.getJSONObject("duration").getInt("value");
            instructions = step.getString("html_instructions");
            travelMode = step.getString("travel_mode");

            // Get the start location
            startLocation = new LatLng(
                    step.getJSONObject("start_location").getDouble("lat"),
                    step.getJSONObject("start_location").getDouble("lng")
            );

            // Get the end location
            endLocation = new LatLng(
                    step.getJSONObject("end_location").getDouble("lat"),
                    step.getJSONObject("end_location").getDouble("lng")
            );

            // Get the polyline
            polylinePoints = com.google.maps.android.PolyUtil.decode(
                    step.getJSONObject("polyline").getString("points")
            );
        }
    }

    /**
     * Make Request
     * - Make the http request to use Google's Directions API
     * @param origin
     * @param destination
     * @return
     */
    private String makeRequest(String origin, String destination) {
        String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
        String ORIGIN_PARAM = "origin";
        String DESTINATION_PARAM = "destination";
        String MODE_PARAM = "mode";
        String KEY_PARAM = "key";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(ORIGIN_PARAM, origin)
                .appendQueryParameter(DESTINATION_PARAM, destination)
                .appendQueryParameter(MODE_PARAM, "driving")
                .appendQueryParameter(KEY_PARAM, DeveloperKey.GOOGLE_MAPS_API_KEY)
                .build();

        Log.v(LOG_TAG, "BUILT URI FOR DIRECTIONS MAPPING: " + builtUri.toString());
        return builtUri.toString();
    }

    /**
     * Get Json Via Http
     * - Get the json from the server via http
     * @return
     */
    private JSONObject getJsonViaHttp() {
        HttpClient httpClient;
        HttpGet httpGet;
        HttpResponse httpResponse;
        ByteArrayOutputStream byteArrayOutputStream;
        JSONObject json;

        // Get the default client
        httpClient = new DefaultHttpClient();

        // Set up the request
        httpGet = new HttpGet(request);

        // Make the request and catch the response
        byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                httpResponse.getEntity().writeTo(byteArrayOutputStream);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Make the json object
        json = new JSONObject();
        try {
            json = new JSONObject(byteArrayOutputStream.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        // Done and return!
        return json;
    }

    /**
     * Get Status
     */
    private boolean getOkStatus() {
        try {
            if(json.getString("status").contentEquals("OK")) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get Data
     * - Take the json from the field and process it to get our variables
     */
    private void getData() {
        try {
            JSONArray routes = json.getJSONArray("routes");
            JSONObject routesOBJ = routes.getJSONObject(0);
            JSONArray legs = routesOBJ.getJSONArray("legs");
            JSONObject legsOBJ = legs.getJSONObject(0);

            String summary = routesOBJ.getString("summary");
            int durationInMinutes = legsOBJ.getJSONObject("duration").getInt("value");
            final String startAddress = legsOBJ.getString("start_address");
            final String endAddress = legsOBJ.getString("end_address");

            String distanceInKM = legsOBJ.getJSONObject("distance").getString("text").replace(" km", "").replace(",","").trim();
            Log.v(LOG_TAG, "THE TOTAL DISTANCE FOR THIS IS: " + distanceInKM);
            distanceInKM = String.valueOf(Float.valueOf(distanceInKM) * 0.63);
            Log.v(LOG_TAG, "THE RADIUS USED FOR LOCATIONS IS: " + distanceInKM);

            // Get start location
            final LatLng startLocation = new LatLng(
                    legsOBJ.getJSONObject("start_location").getDouble("lat"),
                    legsOBJ.getJSONObject("start_location").getDouble("lng")
            );

            // Get end location
            final LatLng endLocation = new LatLng(
                    legsOBJ.getJSONObject("end_location").getDouble("lat"),
                    legsOBJ.getJSONObject("end_location").getDouble("lng")
            );

            final LatLng centerCoords = MidPoint(startLocation, endLocation);

            final Handler mHandler = new Handler(Looper.getMainLooper());
            if (Looper.myLooper() == Looper.getMainLooper()) {
//                super.post(event);
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        MapsFragment.mMap.addMarker(new MarkerOptions()
                                .position(startLocation)
                                .title("Start Location")
                                .snippet("Location Address: ".concat(startAddress))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        );
                        MapsFragment.mMap.addMarker(new MarkerOptions()
                                .position(endLocation)
                                .title("Destination Location")
                                .snippet("Location Address: ".concat(endAddress))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        );
                    }
                });
            }

            ArrayList<String> fetchLocationsData = new ArrayList<String>();
            fetchLocationsData.add(distanceInKM);
            fetchLocationsData.add(Double.toString(centerCoords.latitude));
            fetchLocationsData.add(Double.toString(centerCoords.longitude));

            new FetchLocationsTask().execute(fetchLocationsData);

            // Fill the steps
            ArrayList<Step> stepList;

            // Get the steps
            JSONArray steps = legsOBJ.getJSONArray("steps");
            stepList = new ArrayList<Step>();

            // Get the elements
            for (int i = 0; i < steps.length(); i++) {
                stepList.add(new Step(steps.getJSONObject(i)));
            }

            // Put the steps
            this.steps = stepList;
        }

        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Find midpoint of the start/end destination
    private LatLng MidPoint(LatLng startCoords, LatLng endCoords) {
        double dLon = Math.toRadians(endCoords.longitude - startCoords.longitude);
        double Bx = Math.cos(Math.toRadians(endCoords.latitude)) * Math.cos(dLon);
        double By = Math.cos(Math.toRadians(endCoords.latitude)) * Math.sin(dLon);

        Double latitude = Math.toDegrees(Math.atan2(
                Math.sin(Math.toRadians(startCoords.latitude)) + Math.sin(Math.toRadians(endCoords.latitude)),
                Math.sqrt((Math.cos(Math.toRadians(startCoords.latitude)) + Bx) * (Math.cos(Math.toRadians(startCoords.latitude)) + Bx) + By * By)));

        Double longitude = startCoords.longitude + Math.toDegrees(Math.atan2(By, Math.cos(Math.toRadians(startCoords.latitude)) + Bx));

        return new LatLng(latitude, longitude);
    }

    /**
     * Get All Polyline Points From Steps
     * - Method to get the polyline points to draw a good route on the map
     * @return
     */
    public ArrayList<LatLng> getAllPolylinePointsFromSteps() {
        ArrayList<LatLng> retLatLngArrayList;
        Iterator<Step> stepIterator;
        Iterator<LatLng> polylineLatLngIterator;

        retLatLngArrayList = new ArrayList<LatLng>();
        stepIterator = steps.iterator();
        while(stepIterator.hasNext()) {
            polylineLatLngIterator = stepIterator.next().polylinePoints.iterator();
            while(polylineLatLngIterator.hasNext()) {
                retLatLngArrayList.add(polylineLatLngIterator.next());
            }
        }
        return retLatLngArrayList;
    }
}
