package com.example.bluetoothapp;


import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;
import static com.example.bluetoothapp.MainActivity.WHICH_BLUETOOTH;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class AllDevicesActivity extends AppCompatActivity {

    private RecyclerView devicesRecView;
    private DeviceRecViewAdapter adapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private String whichBluetooth;
    private boolean scanning;
    private Handler handler = new Handler();
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    Set<BluetoothDevice> pairedDevices;
    ArrayList<Device> devices = new ArrayList<>();
    public static int BLUETOOTH_PERMISSION = 1;
    public static int LOCATION_PERMISSION = 2;
    public static int BLUETOOTH_ADMIN_PERMISSION = 3;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_devices);


        Intent intent = getIntent();
        if (intent != null) {
            whichBluetooth = intent.getStringExtra(WHICH_BLUETOOTH);
        } else {
            finish();
        }

        adapter = new DeviceRecViewAdapter(this);
        devicesRecView = findViewById(R.id.devicesRecyclerView);
        devicesRecView.setAdapter(adapter);
        devicesRecView.setLayoutManager(new LinearLayoutManager(this));
        init();
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        if (whichBluetooth.equals("BleScan")) {
            Toast.makeText(this, "Scanning for BLE devices", Toast.LENGTH_SHORT).show();
            scanLeDevice();
        } else {
            retrieveBondedDevices();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH_ADMIN_PERMISSION);
            }
            bluetoothAdapter.startDiscovery();
        }
    }

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(AllDevicesActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        requestPermission(Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH_ADMIN_PERMISSION);
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (ActivityCompat.checkSelfPermission(AllDevicesActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
                    }
                    // TODO : fix a name returning null
                    if(result.getDevice().getName() == null) {
                        adapter.setOneDevice(new Device("unknown", result.getDevice().getAddress(), "BLE"));
                    } else {
                        adapter.setOneDevice(new Device(result.getDevice().getName(), result.getDevice().getAddress(), "BLE"));
                    }
                }
            };

    private void retrieveBondedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                devices.add(new Device(device.getName(), device.getAddress(), "NOTBLE"));
            }
            adapter.setDevices(devices);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(context, "Discovering", Toast.LENGTH_SHORT).show();
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (ActivityCompat.checkSelfPermission(AllDevicesActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
                }
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.setOneDevice(new Device(device.getName(), device.getAddress(), "NOTBLE"));
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
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

                    Toast.makeText(AllDevicesActivity.this, "Bluetooth access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AllDevicesActivity.this, "Bluetooth access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(AllDevicesActivity.this, "Location access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AllDevicesActivity.this, "Location access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(AllDevicesActivity.this, "Bluetooth admin access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AllDevicesActivity.this, "Bluetooth admin access denied!!!", Toast.LENGTH_SHORT).show();
                }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}