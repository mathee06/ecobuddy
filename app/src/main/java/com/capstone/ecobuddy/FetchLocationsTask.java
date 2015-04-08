package com.capstone.ecobuddy;

/**
 * Created by Mathee on 2015-04-07.
 */

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FetchLocationsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

    private static String LOG_TAG = FetchLocationsTask.class.getSimpleName();

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... input) {
        ArrayList<String> queries = null;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String queryJsonStr = null;

        String type = "json";
        String countrycode = "CA";
        String latitude = input[0].get(1);
        String longitude = input[0].get(2);
        String distance = input[0].get(0);
        String distanceunit = "km";
        String comments = "true";

        try {
            // Construct the URL for the query
            final String BASE_CHARGE_URL = "http://api.openchargemap.io/v2/poi/?";
            final String OUTPUT_PARAM = "output";
            final String COUNTRYCODE_PARAM = "countrycode";
            final String LATITUDE_PARAM = "latitude";
            final String LONGITUDE_PARAM = "longitude";
            final String DISTANCE_PARAM = "distance";
            final String DISTANCE_UNIT_PARAM = "distanceunit";
            final String COMMENTS_PARAM = "includecomments";

            Uri builtUri = Uri.parse(BASE_CHARGE_URL).buildUpon()
                    .appendQueryParameter(OUTPUT_PARAM, type)
                    .appendQueryParameter(COUNTRYCODE_PARAM, countrycode)
                    .appendQueryParameter(LATITUDE_PARAM, latitude)
                    .appendQueryParameter(LONGITUDE_PARAM, longitude)
                    .appendQueryParameter(DISTANCE_PARAM, distance)
                    .appendQueryParameter(DISTANCE_UNIT_PARAM, distanceunit)
                    .appendQueryParameter(COMMENTS_PARAM, comments)
                    .build();

            Log.v(LOG_TAG, "BUILT URI FOR LOCATION MAPPING: " + builtUri.toString());
            URL url = new URL(builtUri.toString());
            //Log.v(LOG_TAG, "BUILT URI: " + builtUri.toString());
            // Create the request to YouTube, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            StringBuffer buffer = new StringBuffer();
            try {
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
            } catch (FileNotFoundException fe) {
                Log.v(LOG_TAG, "FILE NOT FOUND EXCEPTION CAUGHT...");
            } catch (Exception e) {
                Log.v(LOG_TAG, "EXCEPTION CAUGHT: " + e);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            queryJsonStr = buffer.toString();

            //Log.v(LOG_TAG, "Query JSON String: " + queryJsonStr);

        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR STATE HIT: ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        try {
            JSONArray jsonArray = new JSONArray(queryJsonStr);
            JSONParser JSONParser = new JSONParser();

            // Getting the parsed data as a list construct
            JSONParser.parseChargingLocations(jsonArray);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }

        return queries;
    }


    @Override
    protected void onPostExecute(ArrayList<String> arrayLists) {
        MainActivity.resultList = arrayLists;
        super.onPostExecute(arrayLists);
    }
}
