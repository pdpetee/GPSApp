package com.kamzs.gpsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //UI stuff
    TextView travelInfoText;
    Button recenterButton;

    //permission stuff
    AlertDialog.Builder setPermissionsMessage;

    //map stuff
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng myLocationLatLng;
    private LatLng myDestinationLatLng;
    private List<List<HashMap<String, String>>> decodedPolylineList;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private MarkerOptions myMarkerOptions;
    private MarkerOptions locationMarkerOptions;
    private RequestQueue mQueue;

    private ArrayList<String> instructionList = new ArrayList<>();
    private ArrayList<Integer> timeList = new ArrayList<>();
    private ArrayList<LatLng> polyList = new ArrayList<>();
    private ArrayList<LatLng> endLocationList = new ArrayList<>();

    //map variables
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private int locationRequestCode = 9001;

    //variables
    private boolean requestingLocationUpdates = true;
    private boolean isMapReady = false;
    private boolean firstTime = true;
    private boolean secondTime = true;
    private boolean setMapDraggable = false;

    private String REQUESTING_LOCATION_UPDATES_KEY = "Poo";

    int instructionListCounter = 1;
    int currentIndex = 0;
    int totalTime = 0;
    int elapsedTime = 0;
    int endLocationCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String TAG = "onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //UI stuff
        travelInfoText = findViewById(R.id.navigation_info_textview);
        recenterButton = findViewById(R.id.recenter_button);

        //Permissions stuff
        setPermissionsMessage = new AlertDialog.Builder(this)
                .setMessage("Please go to Apps > GPSApp > Permissions > Location > Allow only while using the app")
                .setTitle("Enable location permissions")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                });

        //Location stuff
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);

        Intent intent = getIntent();
        myDestinationLatLng = new LatLng(intent.getDoubleExtra("appointment_lat", 0), intent.getDoubleExtra("appointment_long", 0));

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null){
                    Toast.makeText(MapsActivity.this, "Cannot retrieve location", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Location location : locationResult.getLocations()){
                    Log.d(TAG, "New location = " + location.getLatitude() + ", " + location.getLongitude());
                    myLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (isMapReady){
                        getUrlAndDrawPolyline();
                    }
                }
            }
        };

        recenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMapDraggable = false;
                if (isMapReady && myLocationLatLng != null){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocationLatLng, 15));
                }
            }
        });

        //onSaveInstance
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMapDraggable = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firstTime = true;
        secondTime = true;
        requestingLocationUpdates = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            setPermissionsMessage.create().show();
            requestingLocationUpdates = false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(outState, outPersistentState);
    }


    //==============//
    //custom methods//
    //==============//
    private void startLocationUpdates() {
        final String TAG = "startLocationUpdates";
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, locationRequestCode);
            return;
        }

        if (fusedLocationProviderClient == null){
            Log.d(TAG, "fusedLocationProviderClient is null");
            return;
        }

        if (locationRequest == null){
            Log.d(TAG, "locationRequest is null");
            return;
        }

        if (locationCallback == null){
            Log.d(TAG, "locationCallback is null");
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates(){
        final String TAG = "stopLocationUpdates";
        if (fusedLocationProviderClient == null){
            Log.d(TAG, "fusedLocationProviderClient is null");
        }

        if (locationCallback == null){
            Log.d(TAG, "locationCallback is null");
        }
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState){
        if (savedInstanceState == null){
            return;
        }

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)){
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }
    }

    //TODO: Account for flight mode

    public String getRouteURL (LatLng origin, LatLng destination, String apiKey){
        final String TAG = "getRouteURL";
        String originText = origin.latitude + ","+ origin.longitude;
        String destinationText = destination.latitude + "," + destination.longitude;
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originText + "&destination=" + destinationText + "&key=" + apiKey;
        Log.d(TAG, "Your URL is " + url);
        return url;
    }

    public void getUrlAndDrawPolyline(){
        if (firstTime || !bdccGeoDistanceAlgorithm.bdccGeoDistanceCheckWithRadius(polyList, myLocationLatLng, 30)){
            mQueue = Volley.newRequestQueue(this);
            if (!secondTime && !bdccGeoDistanceAlgorithm.bdccGeoDistanceCheckWithRadius(polyList, myLocationLatLng, 50)){
                Toast.makeText(this, "Recalculating...", Toast.LENGTH_SHORT).show();
            }

            // This thing is the dumbest thing i have ever done lord forgive me
            if (!firstTime){
                secondTime = false;
            }
            // If this line doesnt exist the toast in line 228 will show when the activity has just started up

            Log.d("getUrlAndDrawPolyline", "Url has changed. Requesting new google directions api call");
            Log.d("getUrlAndDrawPolyline", "Value of myLocationLatLng: " + myLocationLatLng);
            String url = getRouteURL(myLocationLatLng, myDestinationLatLng, getString(R.string.google_maps_key));
            Log.d("onSuccess.getRouteURL", "Your url is " + url);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    instructionListCounter = 0;
                    currentIndex = 0;
                    if (polyList != null){
                        polyList.clear();
                    }
                    if (decodedPolylineList != null){
                        decodedPolylineList.clear();
                    }
                    if (instructionList != null){
                        instructionList.clear();
                    }
                    if (endLocationList != null){
                        endLocationList.clear();
                    }
                    totalTime = 0;
                    elapsedTime = 0;
                    decodedPolylineList = new ArrayList<>();
                    Log.d("onResponse", "Starting");
                    DirectionsJSONParser djp = new DirectionsJSONParser();
                    decodedPolylineList = djp.parse(response);

                    instructionList = djp.getInstructions(response);
                    endLocationList = djp.getendLocationList(response);
                    timeList = djp.getTime(response);
                    for (int time : timeList){
                        totalTime += time;
                    }
                    firstTime = false;
                    Log.d("onResponse", "JSON Response is: " + response);
                    Log.d("onResponse", "decodedPolylineList: " + decodedPolylineList);

                    for (List<HashMap<String,String>> i : decodedPolylineList){
                        for (HashMap<String,String> j: i){
                            double latitude = Double.parseDouble(j.get("lat"));
                            double longitude = Double.parseDouble(j.get("lng"));
                            LatLng position = new LatLng(latitude, longitude);
                            polyList.add(position);
                        }
                    }
                    if (polyline != null){
                        polyline.remove();
                    }

                    mMap.clear();
                    try{
                        myMarkerOptions = new MarkerOptions().position(polyList.get(0)).title("Your location");
                        locationMarkerOptions = new MarkerOptions().position(polyList.get(polyList.size()-1)).title("Your destination");
                        mMap.addMarker(myMarkerOptions);
                        mMap.addMarker(locationMarkerOptions);
                        if (!setMapDraggable){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocationLatLng, 15));
                        }
                        Log.d("onResponse", "Markers added");

                        polylineOptions = new PolylineOptions();
                        polyline = mMap.addPolyline(polylineOptions.addAll(polyList).width(15).color(Color.BLUE));
                        Log.d("onResponse", "Polyline added");
                    }catch (IndexOutOfBoundsException e){
                        Toast.makeText(MapsActivity.this, "Attempting to retrieve navigation information", Toast.LENGTH_SHORT).show();
                    }}
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

                mQueue.add(jsonObjectRequest);
        }

        else{
            Log.d("getUrlAndDrawPolyline", "Url has not changed. Updating current location but keeping original polyline same");
            mMap.clear();
            myMarkerOptions = new MarkerOptions().position(myLocationLatLng).title("Your location");
            locationMarkerOptions = new MarkerOptions().position(polyList.get(polyList.size()-1)).title("Your destination");
            mMap.addMarker(myMarkerOptions);
            mMap.addMarker(locationMarkerOptions);
            if (!setMapDraggable){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocationLatLng, 15));
            }
            Log.d("onResponse", "Markers added");

            polylineOptions = new PolylineOptions();
            polyline = mMap.addPolyline(polylineOptions.addAll(polyList).width(15).color(Color.BLUE));
            Log.d("onResponse", "Polyline added");
        }

        if (polyList.size() > 1){
            //get starting point and destination point
            LatLng firstPolylineCoordinate = polyList.get(0);
            LatLng lastPoylineCoordinate = polyList.get(polyList.size() - 1);


            float[] distance = new float[1];
            Location.distanceBetween(myLocationLatLng.latitude, myLocationLatLng.longitude, firstPolylineCoordinate.latitude, firstPolylineCoordinate.longitude, distance);

            distance = new float[1];
            Location.distanceBetween(myLocationLatLng.latitude, myLocationLatLng.longitude, lastPoylineCoordinate.latitude, lastPoylineCoordinate.longitude, distance);
            if (distance[0] <= 20){
                Toast.makeText(this, "You have reached your destination", Toast.LENGTH_SHORT).show();
                Log.d("PooPee", "Destination reached");
            }

            distance = new float[1];
            Location.distanceBetween(myLocationLatLng.latitude, myLocationLatLng.longitude,
                    endLocationList.get(endLocationCounter).latitude, endLocationList.get(endLocationCounter).longitude, distance);
            if (distance[0] <= 50){
                if (endLocationList.size() > 1 && instructionList.size() > 1){
                    Log.d("PooPee", "new end location and instructions list");
                    if (endLocationCounter < endLocationList.size() - 1){
                        for (int i = currentIndex; i < polyList.size(); i++){
                            distance = new float[1];
                            Location.distanceBetween(endLocationList.get(endLocationCounter).latitude,
                                    endLocationList.get(endLocationCounter).longitude, polyList.get(i).latitude, polyList.get(i).longitude, distance);
                            if (distance[0] <= 5){
                                currentIndex = i + 1;
                                Log.d("PooPee", "current index has been updated: " + currentIndex);
                                break;
                            }
                        }
                        endLocationCounter += 1;

                    }
                    if (instructionListCounter < instructionList.size() - 1){
                        instructionListCounter += 1;
                    }
                    elapsedTime += timeList.get(endLocationCounter);
                    int time = (totalTime - elapsedTime)/60;
                    if (time <= 0){
                        time = 0;
                    }

                    travelInfoText.setText("Estimated travel time = " + time + " min" + "\nInstructions = " + instructionList.get(instructionListCounter));
                    Log.d("PooPee", "New end location: " + endLocationList.get(endLocationCounter));
                    Log.d("PooPee", "New instructions" + instructionList.get(instructionListCounter));
                }
            }

        }
    }
}