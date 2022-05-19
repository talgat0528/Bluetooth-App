package com.example.bluetoothapp;

import static com.example.bluetoothapp.AllDevicesActivity.BLUETOOTH_PERMISSION;
import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleDeviceActivity extends AppCompatActivity {
    private static final String TAG = "BLUETOOTH";
    private RecyclerView servicesRecView;
    private ServiceRecViewAdapter adapter;
    private BluetoothLeService bluetoothService;
    private String deviceAddress, deviceName;
    private Button btnConnectToDevice, btnDiscFromDevice;
    private TextView txtDeviceTitle;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothDevice btDevice;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService != null) {
                // call functions on service to check connection and connect to devices
                if (bluetoothService != null) {
                    if (!bluetoothService.initialize()) {
                        Log.e(TAG, "Unable to initialize Bluetooth");
                        finish();
                    }
                    // perform device connection

                    if(!bluetoothService.connect(deviceAddress)) {
                        Toast.makeText(bluetoothService, "CANNOT CONNECT", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        initViews();

        Intent intent = getIntent();
        if (intent != null) {
            deviceAddress = intent.getStringExtra(DEVICE_ADDRESS);
        } else {
            finish();
        }
        btDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        deviceName = btDevice.getName();
        txtDeviceTitle.setText(deviceName);
        btnConnectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(BleDeviceActivity.this, "CONNECTING. PLEASE WAIT.", Toast.LENGTH_SHORT).show();
                startConnection();
            }
        });
        btnDiscFromDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothService != null) {
                    bluetoothService.disconnectGatt();
                    unbindService(serviceConnection);
                }
            }
        });
        adapter = new ServiceRecViewAdapter(this);
        servicesRecView = findViewById(R.id.srvcLstView);
        servicesRecView.setAdapter(adapter);
        servicesRecView.setLayoutManager(new LinearLayoutManager(this));
        /*Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        if(bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE) == false) {
            Toast.makeText(this, "SOMETHING WRONG", Toast.LENGTH_SHORT).show();
        }*/

    }
    private void startConnection() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }
    private void initViews() {
        txtDeviceTitle = findViewById(R.id.txtDeviceTitle);
        btnConnectToDevice = findViewById(R.id.btnConnectToDevcie);
        btnDiscFromDevice = findViewById(R.id.btnDiscFromDevice);
    }
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //connected = true;
                //updateConnectionState(R.string.connected);
                Toast.makeText(context, "GATT CONNECTED", Toast.LENGTH_SHORT).show();
                btnConnectToDevice.setVisibility(View.GONE);
                btnDiscFromDevice.setVisibility(View.VISIBLE);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //connected = false;
                //updateConnectionState(R.string.disconnected);
                Toast.makeText(context, "GATT DISCONNECTED", Toast.LENGTH_SHORT).show();
                btnConnectToDevice.setVisibility(View.VISIBLE);
                btnDiscFromDevice.setVisibility(View.GONE);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Toast.makeText(context, "SERVICES DISCOVERED", Toast.LENGTH_SHORT).show();
                displayGattServices(bluetoothService.getSupportedGattServices());
            }
        }
    };
    // TODO: display results properly
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        Device device = new Device(deviceName,deviceAddress);
        ArrayList<BleService> bleServices = new ArrayList<>();
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            BleService bleService = new BleService();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("uuid", uuid);
            bleService.setServiceUuid(currentServiceData);
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<String> charasUuids = new ArrayList<>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                charasUuids.add("\"" + uuid +"\"");
            }
            HashMap<String, ArrayList<String>> charasUuidHash = new HashMap<>();
            charasUuidHash.put("characteristics", charasUuids);
            bleService.setCharacteristicUuids(charasUuidHash);
            bleServices.add(bleService);
        }
        device.setServices(bleServices);
        adapter.setServices(bleServices);
        // save data to local db
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        boolean b = databaseHelper.addDevice(new Device(deviceName, deviceAddress, "BLE"));
        Toast.makeText(this, "Data added to the local database: " + b, Toast.LENGTH_SHORT).show();
        // send data to cloud
        SendToCloudHelper sendToCloudHelper = new SendToCloudHelper(deviceName, deviceAddress, bleServices.toString());
        sendToCloudHelper.sendData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            final boolean result = bluetoothService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }
    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Toast.makeText(BleDeviceActivity.this, "Bluetooth access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Bluetooth access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Toast.makeText(BleDeviceActivity.this, "Location access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Location access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Toast.makeText(BleDeviceActivity.this, "Bluetooth admin access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Bluetooth admin access denied!!!", Toast.LENGTH_SHORT).show();
                }
        }
    }

}