package com.capstone.ecobuddy;

import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ClayFaith on 2015-02-11.
 */
public class Directions {
    public static String LOG_TAG = Directions.class.getSimpleName();

    public static JSONObject g_json;

    public static JSONObject getDirections(String origin, String destination) {

        String modeType = "driving";
        String key = DeveloperKey.GOOGLE_MAPS_API_KEY;

        // http://api.soundcloud.com/tracks.json?q=F&client_id=57119900bbf2d460a8e1954315827230&limit=5
        // Construct the URL for the query
        final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
        final String ORIGIN_PARAM = "origin";
        final String DEST_PARAM = "destination";
        final String MODE_PARAM = "mode";
        final String KEY_PARAM = "key";

        final Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(ORIGIN_PARAM, origin)
                .appendQueryParameter(DEST_PARAM, destination)
                .appendQueryParameter(MODE_PARAM, modeType)
                .appendQueryParameter(KEY_PARAM, key)
                .build();

        Log.v(LOG_TAG, "BUILT URI FOR DIRECTIONS: " + builtUri.toString());

        //NEW

        //Set up the http client
        HttpClient http_client;
        HttpGet http_get;
        HttpResponse http_response;
        JSONObject json;
        ByteArrayOutputStream output_stream;

        //~Get the default client
        http_client = new DefaultHttpClient();

        //Set up the request
        http_get = new HttpGet(builtUri.toString());

        //Make the request and catch the response
        output_stream = new ByteArrayOutputStream();
        try {
            http_response = http_client.execute(http_get);
            if(http_response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                http_response.getEntity().writeTo(output_stream);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //Make the json object
        json = new JSONObject();
        try {
            json = new JSONObject(output_stream.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    //
    // Method: Get an array of points by parsing the json
    //
    public static Double[][] getRoutePoints(JSONObject json) {

        JSONArray routes;
        JSONObject routes_obj0;
        JSONArray legs;
        JSONObject legs_obj0;
        JSONArray steps;
        JSONObject steps_obj;
        Double[][] ret;

        ret = new Double[0][0];
        try {
            routes = json.getJSONArray("routes");
            routes_obj0 = routes.getJSONObject(0);
            legs = routes_obj0.getJSONArray("legs");
            legs_obj0 = legs.getJSONObject(0);
            steps = legs_obj0.getJSONArray("steps");

            //Get the points
            int steps_length;
            Double[][] points;

            steps_length = steps.length();
            points = new Double[steps_length + 1][2];

            //~Fill in the points
            JSONObject step_point;
            Double lat;
            Double lng;

            for(int i = 0; i < steps_length; i++) {
                steps_obj = steps.getJSONObject(i);
                step_point = steps_obj.getJSONObject("start_location");
                lat = step_point.getDouble("lat");
                lng = step_point.getDouble("lng");
                points[i][0] = lat;
                points[i][1] = lng;
            }

            //~Fill the last point
            steps_obj = steps.getJSONObject(steps_length - 1);
            step_point = steps_obj.getJSONObject("end_location");
            lat = step_point.getDouble("lat");
            lng = step_point.getDouble("lng");
            points[steps_length][0] = lat;
            points[steps_length][1] = lng;

            ret = points;
        }

        catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
