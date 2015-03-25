package com.capstone.ecobuddy;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
public class DirectionsOO
{
    //Http stuff
    private String request;
    private JSONObject json;

    //Status
    private boolean status;

    //Summary
    private String summary;

    //Data
    private int distanceInMeters;
    private int durationInMinutes;

    //~Addresses
    private String startAddress;
    private String endAddress;

    //~Locations
    private LatLng startLocation;
    private LatLng endLocation;

    //~List of steps to do the journey
    private ArrayList<Step> steps;

    /**
     * Direction Object Oriented
     * @param origin Current location
     * @param destination Destination to go to
     * @param apiKey Google's API key with Directions enabled
     */
    public DirectionsOO(String origin, String destination, String apiKey) {

        //Make the request and get the json
        this.request = makeRequest(origin, destination, apiKey);
    }

    /**
     * Connect
     * - Actually get the data from a http request
     * - Do this in a thread or it will crash because it takes time to get them
     */
    public void connect() {
        this.json = getJsonViaHttp();

        //Process the json and put the data in the variables
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

        //Variables for this step
        int distanceInMeters;
        int durationInSeconds;
        String instructions;
        String travelMode;
        LatLng startLocation;
        LatLng endLocation;
        List<LatLng> polylinePoints;  //Used to draw a detailed route on the map

        /**
         * Step
         * - The constructor of the step
         * @param step
         * @throws JSONException
         */
        public Step(JSONObject step) throws JSONException {

            //Get some data and put them in the variables
            distanceInMeters = step.getJSONObject("distance").getInt("value");
            durationInSeconds = step.getJSONObject("duration").getInt("value");
            instructions = step.getString("html_instructions");
            travelMode = step.getString("travel_mode");

            //Get the start location
            startLocation = new LatLng(
                    step.getJSONObject("start_location").getDouble("lat"),
                    step.getJSONObject("start_location").getDouble("lng")
            );

            //Get the end location
            endLocation = new LatLng(
                    step.getJSONObject("end_location").getDouble("lat"),
                    step.getJSONObject("end_location").getDouble("lng")
            );

            //Get the polyline
            polylinePoints = com.google.maps.android.PolyUtil.decode(
                    step.getJSONObject("polyline").getString("points")
            );
        }

        /**
         * Print Step (work in progress)
         * @return
         */
        public String printStep() {
            StringBuilder stringBuilder;
            Iterator<LatLng> latLngIterator;

            stringBuilder = new StringBuilder();
            latLngIterator = polylinePoints.iterator();

            stringBuilder.append("POLYLINE ");
            while(latLngIterator.hasNext()) {
                stringBuilder.append(latLngIterator.next().toString());
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }
    }

    /**
     * Make Request
     * - Make the http request to use Google's Directions API
     * @param origin
     * @param destination
     * @param apiKey
     * @return
     */
    private String makeRequest(String origin, String destination, String apiKey) {

        //Make the http string
        StringBuilder sb;

        sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=" + origin);
        sb.append("&");
        sb.append("destination=" + destination);
        sb.append("&");
        sb.append("mode=driving");
        sb.append("&");
        sb.append("key=" + apiKey);

        //Return it
        return sb.toString();
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

        //Get the default client
        httpClient = new DefaultHttpClient();

        //Set up the request
        httpGet = new HttpGet(request);

        //Make the request and catch the response
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

        //Make the json object
        json = new JSONObject();
        try {
            json = new JSONObject(byteArrayOutputStream.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //Done and return!
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
        JSONArray routes;
        JSONObject routesObj0;
        JSONArray legs;
        JSONObject legsObj0;
        JSONArray steps;

        try {
            routes = json.getJSONArray("routes");
            routesObj0 = routes.getJSONObject(0);
            legs = routesObj0.getJSONArray("legs");
            legsObj0 = legs.getJSONObject(0);

            //Get summary
            this.summary = routesObj0.getString("summary");
//            Log.i(this.getClass().getSimpleName(), summary);

            //Get distance
            this.distanceInMeters = legsObj0.getJSONObject("distance").getInt("value");
//            Log.i(this.getClass().getSimpleName(), new Integer(distanceInMeters).toString());

            //Get duration
            this.durationInMinutes = legsObj0.getJSONObject("duration").getInt("value");
//            Log.i(this.getClass().getSimpleName(), new Integer(durationInMinutes).toString());

            //Get start address
            this.startAddress = legsObj0.getString("start_address");
//            Log.i(this.getClass().getSimpleName(), startAddress);

            //Get end address
            this.endAddress = legsObj0.getString("end_address");
//            Log.i(this.getClass().getSimpleName(), endAddress);

            //Get start location
            this.startLocation = new LatLng(
                    legsObj0.getJSONObject("start_location").getDouble("lat"),
                    legsObj0.getJSONObject("start_location").getDouble("lng")
            );
//            Log.i(this.getClass().getSimpleName(), startLocation.toString());

            //Get end location
            this.endLocation = new LatLng(
                    legsObj0.getJSONObject("end_location").getDouble("lat"),
                    legsObj0.getJSONObject("end_location").getDouble("lng")
            );
//            Log.i(this.getClass().getSimpleName(), endLocation.toString());

            //Fill the steps
            JSONObject tempStepJson;
            ArrayList<Step> stepList;

            //~Get the steps
            steps = legsObj0.getJSONArray("steps");
            stepList = new ArrayList<Step>();

            //~Get the elements
            for (int i = 0; i < steps.length(); i++) {
                tempStepJson = steps.getJSONObject(i);
                stepList.add(new Step(tempStepJson));
            }

            //~Put the steps
            this.steps = stepList;
        }

        catch (JSONException e) {
            e.printStackTrace();
        }
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

    /**
     * Get Request String
     * @return
     */
    public String getRequestString() {
        return this.request;
    }
}
