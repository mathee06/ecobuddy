package com.capstone.ecobuddy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mathee on 2015-02-18.
 */
public class JSONParser {
    private final String LOG_TAG = JSONParser.class.getSimpleName();

    // JSON objects for autocomplete dropdown
    final String OWM_PREDICTIONS = "predictions";
    final String OWM_LOCATION_DESCRIPTION = "description";

    // JSON objects for charging locations
    final String OWM_ID = "ID";
    final String OWM_UUID = "UUID";
    final String OWM_ADDRESS_INFO = "AddressInfo";
    final String OWM_ADDRESS = "AddressLine1";
    final String OWM_TOWN = "Town";
    final String OWM_STATEORPROVINCE = "StateOrProvince";
    final String OWM_LATITUDE = "Latitude";
    final String OWM_LONGITUDE = "Longitude";
    final String OWM_CONTACTNUM = "ContactTelephone1";

    public ArrayList<String> parseDropdownLocations(JSONObject jQueries){
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

    public void parseChargingLocations(JSONArray jsonArray){

        HashMap<String, ChargeLocations> chargingMap = new HashMap<String, ChargeLocations>();

        ArrayList<String> queriesList = new ArrayList<>();
        try {
            /** Taking each entry, parses and adds to list object */
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject addressOBJ = jsonArray.getJSONObject(i).getJSONObject(OWM_ADDRESS_INFO);
                chargingMap.put(jsonArray.getJSONObject(i).getString(OWM_ID),
                        new ChargeLocations(
                                jsonArray.getJSONObject(i).getString(OWM_ID),
                                jsonArray.getJSONObject(i).getString(OWM_UUID),
                                addressOBJ.getString(OWM_ADDRESS),
                                addressOBJ.getString(OWM_TOWN),
                                addressOBJ.getString(OWM_STATEORPROVINCE),
                                addressOBJ.getString(OWM_LATITUDE),
                                addressOBJ.getString(OWM_LONGITUDE),
                                addressOBJ.getString(OWM_CONTACTNUM))
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
