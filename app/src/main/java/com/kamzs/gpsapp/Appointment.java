package com.kamzs.gpsapp;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Appointment implements Parcelable {
    private String clientName;
    private String clientContact;
    private String description;
    private String appointmentDate;
    private String appointmentTime;

    private double appointmentLat;
    private double appointmentLong;

    private boolean isCompleted;

    private int appointmentID;

    public Appointment(){}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Appointment(Parcel in) {
        clientName = in.readString();
        clientContact = in.readString();
        description = in.readString();
        appointmentDate = in.readString();
        appointmentTime = in.readString();
        appointmentLat = in.readDouble();
        appointmentLong = in.readDouble();
        isCompleted = in.readBoolean();
        appointmentID = in.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getClientName());
        dest.writeString(this.getClientContact());
        dest.writeString(this.getDescription());
        dest.writeString(this.getAppointmentDate());
        dest.writeString(this.getAppointmentTime());

        dest.writeDouble(this.getAppointmentLat());
        dest.writeDouble(this.getAppointmentLong());

        dest.writeBoolean(this.isCompleted());
        dest.writeInt(this.getAppointmentID());
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientContact() {
        return clientContact;
    }

    public void setClientContact(String clientContact) {
        this.clientContact = clientContact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public double getAppointmentLat() {
        return appointmentLat;
    }

    public void setAppointmentLat(double appointmentLat) {
        this.appointmentLat = appointmentLat;
    }

    public double getAppointmentLong() {
        return appointmentLong;
    }

    public void setAppointmentLong(double appointmentLong) {
        this.appointmentLong = appointmentLong;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }


}
