package com.kamzs.gpsapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {


    public List<List<HashMap<String,String>>> parse (JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        boolean firstStep;

        try {
            jRoutes = jObject.getJSONArray("routes");
            Log.d("DirectionsJSONParser", "Getting routes");

            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();
                Log.d("DirectionsJSONParser", "Getting legs");

                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
                    Log.d("DirectionsJSONParser", "Getting steps");

                    for(int k=0;k<jSteps.length();k++){
                        String polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                    Log.d("DirectionsJSONParser", "Routes that were decoded are:" + routes);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DirectionsJSONParser", "Failed");
        }catch (Exception e){
            Log.d("DirectionsJSONParser", "Failed");
        }
        return routes;
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public ArrayList<String> getInstructions (JSONObject jObject){

        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        ArrayList<String> instructions = new ArrayList<>();

        try {
            jRoutes = jObject.getJSONArray("routes");
            Log.d("DirectionsJSONParser", "Getting routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();
                Log.d("DirectionsJSONParser", "Getting legs");

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");
                    Log.d("DirectionsJSONParser", "Getting steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String instruction = (String)((JSONObject)jSteps.get(k)).get("html_instructions");
                        instruction = instruction.replaceAll("<.*?>", "");
                        instructions.add(instruction);
                        Log.d("DirectionsJSONParser", "instructions = " + instructions);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DirectionsJSONParser", "Failed");
        }catch (Exception e){
            Log.d("DirectionsJSONParser", "Failed");
        }
        return instructions;
    }

    public ArrayList<LatLng> getendLocationList (JSONObject jObject){

        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        ArrayList<LatLng> endLocationList = new ArrayList<>();

        try {
            jRoutes = jObject.getJSONArray("routes");
            Log.d("DirectionsJSONParser", "Getting routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();
                Log.d("DirectionsJSONParser", "Getting legs");

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");
                    Log.d("DirectionsJSONParser", "Getting steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        double lat = (Double)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lat");
                        double lng = (Double)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lng");
                        endLocationList.add(new LatLng(lat, lng));
                        Log.d("DirectionsJSONParser", "added new endLocation: " + lat + ", " + lng);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DirectionsJSONParser", "Failed");
        }catch (Exception e){
            Log.d("DirectionsJSONParser", "Failed");
        }
        return endLocationList;
    }

    public ArrayList<Integer> getTime (JSONObject jObject){

        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        ArrayList<Integer> timeList = new ArrayList<>();

        try {
            jRoutes = jObject.getJSONArray("routes");
            Log.d("DirectionsJSONParser", "Getting routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();
                Log.d("DirectionsJSONParser", "Getting legs");

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");
                    Log.d("DirectionsJSONParser", "Getting steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        int time = (Integer) ((JSONObject)((JSONObject)jSteps.get(k)).get("duration")).get("value");
                        timeList.add(time);
                        Log.d("DirectionsJSONParser", "added new time:" + time + " seconds");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DirectionsJSONParser", "Failed");
        }catch (Exception e){
            Log.d("DirectionsJSONParser", "Failed");
        }
        return timeList;
    }
}