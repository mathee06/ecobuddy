package com.capstone.ecobuddy;

/**
 * Created by Mathee on 2015-04-07.
 */

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FetchLocationsTask extends AsyncTask<ArrayList<String>, Void, Void> {

    private static String LOG_TAG = FetchLocationsTask.class.getSimpleName();
    ProgressDialog Pd_ring = null;

    @Override
    protected void onPreExecute() {
        MapsFragment.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Pd_ring = new ProgressDialog(MapsFragment.mActivity);
                Pd_ring.setMessage("Fetching Locations...");
                Pd_ring.show();
            }
        });
    }


    @Override
    protected Void doInBackground(ArrayList<String>... input) {
        if (input[0].get(0).equals("stations")) {
            fetchStations(input);
        } else if (input[0].get(0).equals("poi")) {
            fetchPOI(input);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        MapsFragment.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Pd_ring.dismiss();
            }
        });

        super.onPostExecute(aVoid);
    }

    private void fetchStations(ArrayList<String>[] input) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlChargeConnection = null;
        HttpURLConnection urlFuelConnection = null;
        BufferedReader chargeReader = null;
        BufferedReader fuelReader = null;

        // Will contain the raw JSON response as a string.
        String chargeJsonStr = null;
        String fuelJsonStr = null;

        String output_type = "json";
        String countrycode = "CA";
        String distance = input[0].get(1);
        String latitude = input[0].get(2);
        String longitude = input[0].get(3);
        String distanceunit = "km";
        String comments = "true";

        // fuelling location arguments
        String key = DeveloperKey.GOOGLE_MAPS_API_KEY;
        String location = latitude.concat(",").concat(longitude);
        String radius = String.valueOf(Float.valueOf(distance) * 1000);
        String types = "gas_station";
        String rankby = "distance";
        Log.v(LOG_TAG, "RADIUS IN METERES: " + radius);

        try {
            // Construct the URL for the query
            final String BASE_CHARGE_URL = "http://api.openchargemap.io/v2/poi/";
            final String BASE_FUEL_URL = "https://maps.googleapis.com/maps/api/place/radarsearch/json?";
            final String OUTPUT_PARAM = "output";
            final String COUNTRYCODE_PARAM = "countrycode";
            final String LATITUDE_PARAM = "latitude";
            final String LONGITUDE_PARAM = "longitude";
            final String DISTANCE_PARAM = "distance";
            final String DISTANCE_UNIT_PARAM = "distanceunit";
            final String COMMENTS_PARAM = "includecomments";

            final String KEY_PARAM = "key";
            final String LOCATION_PARAM = "location";
            final String RADIUS_PARAM = "radius";
            final String TYPES_PARAM = "types";
            final String RANKBY_PARAM = "rankby";

            Uri builtChargeUri = Uri.parse(BASE_CHARGE_URL).buildUpon()
                    .appendQueryParameter(OUTPUT_PARAM, output_type)
                            //.appendQueryParameter(COUNTRYCODE_PARAM, countrycode)
                    .appendQueryParameter(LATITUDE_PARAM, latitude)
                    .appendQueryParameter(LONGITUDE_PARAM, longitude)
                    .appendQueryParameter(DISTANCE_PARAM, distance)
                    .appendQueryParameter(DISTANCE_UNIT_PARAM, distanceunit)
                    .appendQueryParameter(COMMENTS_PARAM, comments)
                    .build();

            Uri builtFuelUri = Uri.parse(BASE_FUEL_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, key)
                    .appendQueryParameter(LOCATION_PARAM, location)
                    .appendQueryParameter(RADIUS_PARAM, radius)
                    .appendQueryParameter(TYPES_PARAM, types)
                    .appendQueryParameter(RANKBY_PARAM, rankby)
                    .build();

            // Create requests to CHARGE API, and open the connection
            Log.v(LOG_TAG, "BUILT URI FOR NEARBY CHARGE LOCATIONS: " + builtChargeUri.toString());
            URL chargeUrl = new URL(builtChargeUri.toString());
            urlChargeConnection = (HttpURLConnection) chargeUrl.openConnection();
            urlChargeConnection.setRequestMethod("GET");
            urlChargeConnection.connect();

            // Create requests to FUEL API, and open the connection
            Log.v(LOG_TAG, "BUILT URI FOR NEARBY FUEL LOCATIONS: " + builtFuelUri.toString());
            URL fuelUrl = new URL(builtFuelUri.toString());
            urlFuelConnection = (HttpURLConnection) fuelUrl.openConnection();
            urlFuelConnection.setRequestMethod("GET");
            urlFuelConnection.connect();

            StringBuffer chargeBuffer = new StringBuffer();
            StringBuffer fuelBuffer = new StringBuffer();
            try {
                // Read the input stream into a String
                InputStream chargeStream = urlChargeConnection.getInputStream();
                InputStream fuelStream = urlFuelConnection.getInputStream();

                if (chargeStream == null || fuelStream == null) {
                    // Nothing to do.
                    //return null;
                }
                chargeReader = new BufferedReader(new InputStreamReader(chargeStream));
                fuelReader = new BufferedReader(new InputStreamReader(fuelStream));
            } catch (FileNotFoundException fe) {
                Log.v(LOG_TAG, "FILE NOT FOUND EXCEPTION CAUGHT...");
            } catch (Exception e) {
                Log.v(LOG_TAG, "EXCEPTION CAUGHT: " + e);
            }

            String line;
            while ((line = chargeReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                chargeBuffer.append(line + "\n");
            }

            while ((line = fuelReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                fuelBuffer.append(line + "\n");
            }

            if (chargeBuffer.length() == 0 || fuelBuffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                //return null;
            }

            chargeJsonStr = chargeBuffer.toString();
            fuelJsonStr = fuelBuffer.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR STATE HIT: ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            //return null;

        } finally {
            if (urlChargeConnection != null) {
                urlChargeConnection.disconnect();
            }
            if (urlFuelConnection != null) {
                urlFuelConnection.disconnect();
            }
            if (chargeReader != null) {
                try {
                    chargeReader.close();
                } catch (final IOException e) {
                    Log.e("FETCH_LOCATIONS_TASK", "Error closing stream", e);
                }
            }
            if (fuelReader != null) {
                try {
                    fuelReader.close();
                } catch (final IOException e) {
                    Log.e("FETCH_LOCATIONS_TASK", "Error closing stream", e);
                }
            }
        }
        try {
            JSONParser JSONParser = new JSONParser();

            // Getting the parsed data as a list construct
            JSONArray jsonArray = new JSONArray(chargeJsonStr);
            JSONParser.parseChargingLocations(jsonArray);

            JSONObject jsonObject = new JSONObject(fuelJsonStr);
            JSONParser.parseFuelingLocations(jsonObject);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    private void fetchPOI(ArrayList<String>[] input) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader bufferReader = null;

        // Will contain the raw JSON response as a string.
        String inputJsonStr = null;

        // fuelling location arguments
        String key = DeveloperKey.GOOGLE_MAPS_API_KEY;
        String location = input[0].get(1);
        String keyword = input[0].get(2);
        String rankby = "distance";

        try {
            // Construct the URL for the query
            final String BASE_POI_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            final String KEY_PARAM = "key";
            final String LOCATION_PARAM = "location";
            final String QUERY_PARAM = "name";
            final String RANKBY_PARAM = "rankby";

            Uri builtUri = Uri.parse(BASE_POI_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, key)
                    .appendQueryParameter(LOCATION_PARAM, location)
                    .appendQueryParameter(QUERY_PARAM, keyword)
                    .appendQueryParameter(RANKBY_PARAM, rankby)
                    .build();

            // Create requests to FUEL API, and open the connection
            Log.v(LOG_TAG, "BUILT URI FOR NEARBY POI LOCATIONS: " + builtUri.toString());
            URL Url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) Url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            StringBuffer stringBuffer = new StringBuffer();
            try {
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream == null) {
                    // Nothing to do.
                    //return null;
                }
                bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            } catch (FileNotFoundException fe) {
                Log.v(LOG_TAG, "FILE NOT FOUND EXCEPTION CAUGHT...");
            } catch (Exception e) {
                Log.v(LOG_TAG, "EXCEPTION CAUGHT: " + e);
            }

            String line;
            while ((line = bufferReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                stringBuffer.append(line + "\n");
            }

            if (stringBuffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                //return null;
            }

            inputJsonStr = stringBuffer.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR STATE HIT: " + e.toString());
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            //return null;

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferReader != null) {
                try {
                    bufferReader.close();
                } catch (final IOException e) {
                    Log.e("FETCH_LOCATIONS_TASK", "Error closing stream", e);
                }
            }
        }
        try {
            JSONParser JSONParser = new JSONParser();
            JSONObject jsonObject = new JSONObject(inputJsonStr);
            JSONParser.parsePOILocations(jsonObject);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }
}
