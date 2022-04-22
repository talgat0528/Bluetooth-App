package com.example.bluetoothapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DEVICE_TABLE = "DEVICE_TABLE";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "devices.db", null, 1);
    }

    //onCreate is calls the first time a database is accessed.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + DEVICE_TABLE + "(" + DEVICE_NAME + " TEXT, " + DEVICE_ADDRESS + " TEXT)";
        sqLiteDatabase.execSQL(createTableStatement);
    }

    // onUpgrade is called whenever database version changes.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addDevice(Device device) {
        // getWritableDatabase is for insert actions, getReadableDatabase is for read actions
        SQLiteDatabase db = this.getWritableDatabase();
        //ContentValues instances store data in pairs
        ContentValues cv = new ContentValues();
        cv.put(DEVICE_NAME, device.getName());
        cv.put(DEVICE_ADDRESS,device.getAddress());
        long insert = db.insert(DEVICE_TABLE, null, cv);
        if(insert == -1) {
            return false;
        } else {
            return true;
        }
    }
    public ArrayList<Device> getAllDevices() {
        ArrayList<Device> devices = new ArrayList<>();
        // get the data from the database using getReadableDatabase
        String query = "SELECT * FROM " + DEVICE_TABLE;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        //Cursor is the result set from the SQL statement
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        //if moveToFirst evaluates to true, there were results
        if(cursor.moveToFirst()) {
            //loop through the results and create new device object
            do {
                String deviceName = cursor.getString(0);
                String deviceAddress = cursor.getString(1);
                devices.add(new Device(deviceName,deviceAddress));
            } while (cursor.moveToNext());
        }
        // clean up
        cursor.close();
        sqLiteDatabase.close();
        return devices;
    }
}
