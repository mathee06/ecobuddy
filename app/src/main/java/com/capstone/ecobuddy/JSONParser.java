package com.capstone.ecobuddy;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    final String OWM_ADDRESSLINE = "AddressLine1";
    final String OWM_TOWN = "Town";
    final String OWM_STATEORPROVINCE = "StateOrProvince";
    final String OWM_LATITUDE = "Latitude";
    final String OWM_LONGITUDE = "Longitude";
    final String OWM_CONTACTNUM = "ContactTelephone1";

    // JSON objects for fueling locations
    final String OWM_RESULTS = "results";
    final String OWM_GEOMETRY = "geometry";
    final String OWM_LOCATION = "location";
    final String OWM_PLACEID = "place_id";
    final String OWM_NAME = "name";
    final String OWM_ADDRESS = "vicinity";
    final String OWM_PRICELEVEL = "price_level";
    final String OWM_LAT = "lat";
    final String OWM_LONG = "lng";

    private List<String> shades = Arrays.asList("270.0", "60.0", "330.0", "180.0", "300.0", "240.0", "30.0");
    private static ArrayList<String> shadesTaken = new ArrayList<String>();

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

        try {
            /** Taking each entry, parses and adds to list object */
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject addressOBJ = jsonArray.getJSONObject(i).getJSONObject(OWM_ADDRESS_INFO);
                chargingMap.put(jsonArray.getJSONObject(i).getString(OWM_ID),
                        new ChargeLocations(
                                jsonArray.getJSONObject(i).getString(OWM_ID),
                                jsonArray.getJSONObject(i).getString(OWM_UUID),
                                addressOBJ.getString(OWM_ADDRESSLINE),
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

    public void parseFuelingLocations(JSONObject jsonObject){

        HashMap<String, FuelLocations> fuelMap = new HashMap<String, FuelLocations>();

        try {
            JSONArray resultsArray = jsonObject.getJSONArray(OWM_RESULTS);

            /** Taking each entry, parses and adds to list object */
            for(int i = 0; i < resultsArray.length(); i++) {

                JSONObject geometryOBJ = resultsArray.getJSONObject(i).getJSONObject(OWM_GEOMETRY);
                JSONObject locationOBJ = geometryOBJ.getJSONObject(OWM_LOCATION);
                fuelMap.put(resultsArray.getJSONObject(i).getString(OWM_PLACEID),
                        new FuelLocations(
                                resultsArray.getJSONObject(i).getString(OWM_PLACEID),
                                locationOBJ.getString(OWM_LAT),
                                locationOBJ.getString(OWM_LONG))
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parsePOILocations(JSONObject jsonObject){

        HashMap<String, POILocations> poiMap = new HashMap<String, POILocations>();

        float shade = getPOIShade();

        try {
            JSONArray resultsArray = jsonObject.getJSONArray(OWM_RESULTS);

            /** Taking each entry, parses and adds to list object */
            for(int i = 0; i < resultsArray.length(); i++) {

                JSONObject geometryOBJ = resultsArray.getJSONObject(i).getJSONObject(OWM_GEOMETRY);
                JSONObject locationOBJ = geometryOBJ.getJSONObject(OWM_LOCATION);
                poiMap.put(resultsArray.getJSONObject(i).getString(OWM_PLACEID),
                        new POILocations(
                                resultsArray.getJSONObject(i).getString(OWM_PLACEID),
                                resultsArray.getJSONObject(i).getString(OWM_NAME),
                                resultsArray.getJSONObject(i).getString(OWM_ADDRESS),
                                locationOBJ.getString(OWM_LAT),
                                locationOBJ.getString(OWM_LONG),
                                shade)
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private float getPOIShade() {
        Iterator<String> iterator = shades.iterator();
        while(iterator.hasNext()) {
            String shade = iterator.next();
            Log.v(LOG_TAG, "CURRENTLY LOOKING AT SHADE: " + shade + " WITH SHADES TAKEN SIZE: " + shadesTaken.size());
            if (shadesTaken.size() == 7) {
                Log.v(LOG_TAG, "ALL SHADES USED... RE-USING SHADES");
                shadesTaken.clear();
            }
            if (!shadesTaken.contains(shade)) {
                shadesTaken.add(shade);
                Log.v(LOG_TAG, "SHADE DOES NOT EXIST IN TAKEN LIST; ADDED SHADE: " + shade + ". NEW SIZE: " + shadesTaken.size());
                return Float.valueOf(shade);
            }
        }
        return Float.valueOf("300.0");
    }
}
