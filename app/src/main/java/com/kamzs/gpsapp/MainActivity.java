package com.kamzs.gpsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Appointment> appointmentList;
    private RecyclerView recyclerView;
    private Appointment appointment;

    //Database stuff
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);

        appointmentList = new ArrayList<>();
        Cursor data = dbHelper.getData();

        while (data.moveToNext()){
            appointment = new Appointment();
            appointment.setAppointmentID(data.getInt(0));
            appointment.setClientName(data.getString(1));
            appointment.setClientContact(data.getString(2));
            appointment.setDescription(data.getString(3));
            appointment.setAppointmentDate(data.getString(4));
            appointment.setAppointmentTime(data.getString(5));
            appointment.setAppointmentLat(data.getDouble(6));
            appointment.setAppointmentLong(data.getDouble(7));
            if (data.getInt(8) == 0){
                appointment.setCompleted(false);
            }
            else{
                appointment.setCompleted(true);
            }
            Log.d("POOPOO", "Appointment.isCompleted = " + appointment.isCompleted());
            appointmentList.add(appointment);
            Log.d("Poo", "Appointment list is : " + appointmentList);
        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AppointmentAdapter(appointmentList));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, CreateAppointmentActivity.class);
                intent.putExtra("createNew", true);
                startActivity(intent);
            }
        });
    }
}