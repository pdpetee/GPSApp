package com.kamzs.gpsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "appointment_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "name";
    private static final String COL3 = "contact";
    private static final String COL4 = "description";
    private static final String COL5 = "date";
    private static final String COL6 = "time";
    private static final String COL7 = "latitude";
    private static final String COL8 = "longitude";
    private static final String COL9 = "status";

    public DatabaseHelper(@Nullable Context context) {
        super(context, TABLE_NAME, null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_NAME
                + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL2 + " TEXT,"
                + COL3 + " TEXT,"
                + COL4 + " TEXT,"
                + COL5 + " TEXT,"
                + COL6 + " TEXT,"
                + COL7 + " REAL,"
                + COL8 + " REAL,"
                + COL9 + " INT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(Appointment item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2, item.getClientName());
        values.put(COL3, item.getClientContact());
        values.put(COL4, item.getDescription());
        values.put(COL5, item.getAppointmentDate());
        values.put(COL6, item.getAppointmentTime());
        values.put(COL7, item.getAppointmentLat());
        values.put(COL8, item.getAppointmentLong());
        if (item.isCompleted()){
            values.put(COL9, 1);
        }
        else{
            values.put(COL9, 0);
        }

        Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, values);

        if (result == 0){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public boolean deleteData(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL1 + " LIKE ?";
        String[] selectionArgs = { String.valueOf(ID) };
        int deletedRows = db.delete(TABLE_NAME, selection, selectionArgs);
        if (deletedRows > 0){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean updateData(int ID, int isCompleted){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL9, isCompleted);

        String selection = COL1 + " LIKE ?";
        String[] selectionArgs = { String.valueOf(ID) };

        int updatedRows = db.update(TABLE_NAME, values, selection, selectionArgs);
        if (updatedRows > 0){
            return true;
        }
        else{
            return false;
        }
    }
}
