package com.capstone.ecobuddy;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mathee on 2015-02-18.
 */
public class QueryJSONParser {
    private final String LOG_TAG = QueryJSONParser.class.getSimpleName();

    // These are the names of the JSON objects that need to be extracted
    final String OWM_PREDICTIONS = "predictions";
    final String OWM_LOCATION_DESCRIPTION = "description";

    public ArrayList<String> getQueries(JSONObject jQueries){

        ArrayList<String> queriesList = new ArrayList<>();
        try {

            JSONArray jsonArray = jQueries.getJSONArray(OWM_PREDICTIONS);

            /** Taking each entry, parses and adds to list object */
            for(int i = 0; i < jsonArray.length(); i++) {
                /** Call getQuery with query JSON object to parse the query */
                queriesList.add(jsonArray.getJSONObject(i).getString(OWM_LOCATION_DESCRIPTION));
                //Log.v(LOG_TAG, "ADDED LOCATION: " + jsonArray.getJSONObject(i).getString(OWM_LOCATION_DESCRIPTION));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return queriesList;
    }
}
