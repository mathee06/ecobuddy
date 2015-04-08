package com.capstone.ecobuddy;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Mathee on 2015-03-25.
 */
public class AutoCompleteTask extends AsyncTask<String, Void, ArrayList<String>> {

    private static String LOG_TAG = AutoCompleteTask.class.getSimpleName();

    @Override
    protected ArrayList<String> doInBackground(String... input) {
        ArrayList<String> queries = null;

        // If there's no query, there is nothing to look up
        if (input.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String queryJsonStr = null;

        String q = input[0];
        String type = "address";
        String key = DeveloperKey.GOOGLE_MAPS_API_KEY;

        try {
            // Construct the URL for the query
            final String BASE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
            final String INPUT_PARAM = "input";
            final String PLACE_TYPE = "types";
            final String KEY_PARAM = "key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(INPUT_PARAM, q)
                    .appendQueryParameter(PLACE_TYPE, type)
                    .appendQueryParameter(KEY_PARAM, key)
                    .build();

            Log.v(LOG_TAG, "BUILT URI FOR DROPDOWN: " + builtUri.toString());

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
            JSONObject jsonObject = new JSONObject(queryJsonStr);
            JSONParser JSONParser = new JSONParser();

            // Getting the parsed data as a list construct
            queries = JSONParser.parseDropdownLocations(jsonObject);

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
