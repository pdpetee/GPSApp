package com.kamzs.gpsapp;

import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateAppointmentActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;
    private EditText clientNameET, clientContactET, appointmentDetailsET, locationET;
    private CalendarView appointmentDateCV;
    private TimePicker appointmentTimeTP;
    private Button saveButton;
    private ImageButton searchLocationButton;
    private CheckBox setReminderCB;

    private boolean isMapReady = false;
    private boolean createNew;
    private boolean isDataAdded;

    //Calendar
    Calendar cal;

    //Date
    Date date;

    //Address stuff
    Address address;

    //Appointment class
    Appointment appointment;

    //Appointment information
    LatLng appointmentLocation;
    String appointmentDate;
    String appointmentTime;
    String appointmentDetails;
    String clientName;
    String clientContact;

    //Database stuff
    DatabaseHelper dbHelper;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String TAG = "onCreate";
        Intent intent = getIntent();
        cal = Calendar.getInstance();
        createNew = intent.getBooleanExtra("createNew", true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        dbHelper = new DatabaseHelper(this);

        setReminderCB = findViewById(R.id.set_reminder_checkbox);
        setReminderCB.setChecked(true);

        clientNameET = findViewById(R.id.client_name_ET);
        clientContactET = findViewById(R.id.client_contact_ET);
        appointmentDetailsET = findViewById(R.id.description_ET);
        locationET = findViewById(R.id.appointment_location_ET);

        searchLocationButton = findViewById(R.id.search_location_button);
        searchLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = locationET.getText().toString().trim();
                if (locationName.matches("")){
                    locationET.setError("Field cannot be blank");
                }
                else{
                    if (isMapReady){
                        try{
                            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
                            Log.d(TAG, "Address successfully retrieved");
                            address = addressList.get(0);
                            Log.d(TAG, "Address entered by user is " + address);
                            appointmentLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            MarkerOptions locationMarker = new MarkerOptions()
                                    .position(appointmentLocation)
                                    .title(address.getAddressLine(0));
                            mMap.addMarker(locationMarker);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(appointmentLocation, 15));

                        } catch (IOException e) {
                            Log.d(TAG, String.valueOf(e));
                            locationET.setError("Please enter a valid location");
                            e.printStackTrace();
                        }
                        catch (Exception e){
                            Log.d(TAG, String.valueOf(e));
                            locationET.setError("Please enter a valid location");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        appointmentDateCV = findViewById(R.id.appointment_date_CV);
        appointmentDateCV.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                appointmentDate = dayOfMonth + " " + (month+1) + " " + year;
            }
        });

        appointmentTimeTP = findViewById(R.id.appointment_time_TP);
        appointmentTime = appointmentTimeTP.getHour() + ":" + appointmentTimeTP.getMinute();
        appointmentTimeTP.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (minute < 10){
                    appointmentTime = hourOfDay + "-0" + minute;
                }
                else{
                    appointmentTime = hourOfDay + ":" + minute;
                }
            }
        });

        if (createNew){
            appointment = new Appointment();
        }
        else{
            appointment = intent.getParcelableExtra("appointment");
            clientNameET.setText(appointment.getClientName());
            clientContactET.setText(appointment.getClientContact());
            appointmentDetailsET.setText(appointment.getDescription());

            SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy hh:mm");
            String dateString = appointment.getAppointmentDate() + " " + appointment.getAppointmentTime();
            try {
                date = df.parse(dateString);
                long datelong = date.getTime();
                int hour = date.getHours();
                int min = date.getMinutes();
                appointmentDate = appointment.getAppointmentDate();
                appointmentDateCV.setDate(datelong);
                appointmentTimeTP.setHour(hour);
                appointmentTimeTP.setMinute(min);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientName = clientNameET.getText().toString().trim();
                if (clientName.matches("")){
                    clientNameET.setError("Name cannot be blank");
                    return;
                }
                else if (!clientName.matches("^[a-z A-Z ,.'-]+$")){
                    clientNameET.setError("Name cannot contain symbols or numbers");
                    return;
                }
                appointment.setClientName(clientName);

                clientContact = clientContactET.getText().toString().trim();
                if (clientContact.matches("")){
                    clientContactET.setError("Contact cannot be blank");
                    return;
                }
                else if (clientContact.matches("^[\\D]+$")){
                    clientContactET.setError("Contact cannot contain letters or symbols");
                    return;
                }
                appointment.setClientContact(clientContact);

                appointmentDetails = appointmentDetailsET.getText().toString();
                if (appointmentDetails.matches("")){
                    appointmentDetailsET.setError("Details cannot be blank");
                    return;
                }
                appointment.setDescription(appointmentDetails);

                if(createNew){
                    if (appointmentLocation == null){
                        locationET.setError("Appointment location cannot be empty");
                        return;
                    }
                }
                else{
                    appointmentLocation = new LatLng(appointment.getAppointmentLat(), appointment.getAppointmentLong());
                }

                appointment.setAppointmentLat(appointmentLocation.latitude);
                appointment.setAppointmentLong(appointmentLocation.longitude);

                appointment.setAppointmentTime(appointmentTime);
                appointment.setAppointmentDate(appointmentDate);

                if (createNew){
                    appointment.setCompleted(false);
                    isDataAdded = dbHelper.addData(appointment);
                }
                else{
                    dbHelper.deleteData(appointment.getAppointmentID());
                    isDataAdded = dbHelper.addData(appointment);
                }

                if (isDataAdded){
                    if (createNew){
                        Toast.makeText(CreateAppointmentActivity.this, "Appointment created", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(CreateAppointmentActivity.this, "Appointment updated", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    if (setReminderCB.isChecked()){
                        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy hh:mm");
                        String dateString = appointment.getAppointmentDate() + " " + appointment.getAppointmentTime();
                        try {
                            date = df.parse(dateString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        cal.setTime(date);
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setType("vnd.android.cursor.item/event");
                        intent.putExtra("beginTime", cal.getTimeInMillis());
                        intent.putExtra("allDay", false);
                        intent.putExtra("rrule", "FREQ=DAILY");
                        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
                        intent.putExtra("title", appointment.getDescription());
                        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, address.getAddressLine(0));
                        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Client name: " + appointment.getClientName());
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(CreateAppointmentActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                else{
                    Toast.makeText(CreateAppointmentActivity.this, "Cannot add data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!createNew){
            LatLng oldLocation = new LatLng(appointment.getAppointmentLat(), appointment.getAppointmentLong());
            MarkerOptions oldMarkerOptions = new MarkerOptions().position(oldLocation);
            mMap.addMarker(oldMarkerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oldLocation, 15));
        }
        isMapReady = true;
    }
}