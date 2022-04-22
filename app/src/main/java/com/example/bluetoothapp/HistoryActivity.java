package com.example.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecView;
    private DeviceRecViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        adapter = new DeviceRecViewAdapter(this);
        historyRecView = findViewById(R.id.historyRecyclerView);
        historyRecView.setAdapter(adapter);
        historyRecView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        ArrayList<Device> allDevices = databaseHelper.getAllDevices();
        adapter.setDevices(allDevices);
        Toast.makeText(this, "Showing previously connected devices", Toast.LENGTH_SHORT).show();
    }
}