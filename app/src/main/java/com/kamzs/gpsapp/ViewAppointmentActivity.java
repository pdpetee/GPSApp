package com.kamzs.gpsapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewAppointmentActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Appointment appointment;
    private TextView nameTV, contactTV, descriptionTV, dateTV, timeTV, statusTV;
    private boolean isMapReady = false;
    private boolean isMissed;
    private boolean isUpdated;
    private Button callButton, getDirectionsButton;
    private ToggleButton setCompletedButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String TAG = "onCreate";
        Intent intent = getIntent();
        appointment = intent.getParcelableExtra("appointment_class");
        Log.d("POOPEE", "appointment.isCOmpleted = " + appointment.isCompleted());
        dbHelper = new DatabaseHelper(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nameTV = findViewById(R.id.client_name_TV);
        nameTV.setText(appointment.getClientName());

        contactTV = findViewById(R.id.client_contact_TV);
        contactTV.setText(appointment.getClientContact());

        descriptionTV = findViewById(R.id.description_TV);
        descriptionTV.setText(appointment.getDescription());

        dateTV = findViewById(R.id.date_TV);

        dateTV.setText(appointment.getAppointmentDate());

        timeTV = findViewById(R.id.time_TV);
        timeTV.setText(appointment.getAppointmentTime());

        statusTV = findViewById(R.id.status_TV);
        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy hh:mm");
        String dateString = appointment.getAppointmentDate() + " " + appointment.getAppointmentTime();
        try {
            Date appointmentDate = df.parse(dateString);
            Date currentDate = new Date();
            Log.d("Poo", "current date is " + currentDate);
            Log.d("Poo", "app date is " + appointmentDate);
            if (currentDate.after(appointmentDate)){
                isMissed = true;
            }
            else{
                isMissed = false;

            }
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to retrieve date", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if (appointment.isCompleted()){
            statusTV.setText("Completed");
            statusTV.setTextColor(Color.GREEN);
        }
        else{
            if (isMissed){
                statusTV.setText("Missed");
                statusTV.setTextColor(Color.RED);
            }
            else{
                statusTV.setText("Pending");
                statusTV.setTextColor(Color.YELLOW);
            }
        }

        callButton = findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + appointment.getClientContact()));
                startActivity(intent);
            }
        });

        getDirectionsButton = findViewById(R.id.get_directions_button);
        getDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewAppointmentActivity.this, MapsActivity.class);
                intent.putExtra("appointment_lat", appointment.getAppointmentLat());
                intent.putExtra("appointment_long", appointment.getAppointmentLong());
                startActivity(intent);
            }
        });

        setCompletedButton = findViewById(R.id.set_completed_button);
        setCompletedButton.setTextOff("Completed");

        if (isMissed){
            setCompletedButton.setTextOn("Missed");
        }
        else{
            setCompletedButton.setTextOn("Not completed");
            setCompletedButton.setTextColor(Color.YELLOW);
        }

        if (appointment.isCompleted()){
            setCompletedButton.setText("Completed");
            setCompletedButton.setChecked(false);
            setCompletedButton.setTextColor(Color.GREEN);
        }
        else{
            setCompletedButton.setChecked(true);
            if (isMissed){
                setCompletedButton.setTextColor(Color.RED);
                setCompletedButton.setText("Missed");
            }
            else{
                setCompletedButton.setText("Not completed");
                setCompletedButton.setTextColor(Color.YELLOW);
            }
        }

        setCompletedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!appointment.isCompleted()){
                    isUpdated = dbHelper.updateData(appointment.getAppointmentID(), 1);
                    Log.d(TAG, String.valueOf(isUpdated));
                    setCompletedButton.setTextColor(Color.GREEN);
                    appointment.setCompleted(true);

                    statusTV.setText("Completed");
                    statusTV.setTextColor(Color.GREEN);
                }
                else{
                    isUpdated = dbHelper.updateData(appointment.getAppointmentID(), 0);
                    appointment.setCompleted(false);
                    Log.d(TAG, String.valueOf(isUpdated));
                    if (isMissed){
                        setCompletedButton.setTextColor(Color.RED);

                        statusTV.setText("Missed");
                        statusTV.setTextColor(Color.RED);
                    }
                    else{
                        setCompletedButton.setTextColor(Color.YELLOW);

                        statusTV.setText("Pending");
                        statusTV.setTextColor(Color.YELLOW);
                    }
                }
            }
        });

        if (isMapReady){
            LatLng appointmentLocation = new LatLng(appointment.getAppointmentLat(), appointment.getAppointmentLong());
            mMap.addMarker(new MarkerOptions().position(appointmentLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(appointmentLocation, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        LatLng appointmentLocation = new LatLng(appointment.getAppointmentLat(), appointment.getAppointmentLong());
        mMap.addMarker(new MarkerOptions().position(appointmentLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(appointmentLocation, 15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(ViewAppointmentActivity.this, "Click on Get Directions to get navigation information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ViewAppointmentActivity.this, MainActivity.class);
        startActivity(intent);
    }
}