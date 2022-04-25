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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_devices);

        Intent intent = getIntent();
        if (intent != null) {
            whichBluetooth = intent.getStringExtra(WHICH_BLUETOOTH);
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

            //bluetoothAdapter.startDiscovery();
        }






    }
    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
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
                    devices.add(new Device(result.getDevice().getName(),result.getDevice().getAddress()));
                    adapter.setDevices(devices);
                }
            };
    private void retrieveBondedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                devices.add(new Device(device.getName(), device.getAddress()));
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestPermission(Manifest.permission.BLUETOOTH, 1);
                    //return;
                }
                //String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
                /*if (device1.getBondState()!=BluetoothDevice.BOND_BONDED)
                {

                }*/
                Device item = new Device(device.getName(), device.getAddress());
                item.setBtdevice(device);
                boolean containsItem = false;
                for(Device dev : devices) {
                    if(dev.getName().equals(item.getName())) {
                        containsItem = true;
                        break;
                    }
                }
                if(!containsItem) {

                    devices.add(item);
                    item.setBtdevice(device);
                    adapter.setDevices(devices);

                }


            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
        bluetoothManager = getSystemService(BluetoothManager.class);
        //bluetoothAdapter = bluetoothManager.getAdapter();
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

                    //Toast.makeText(AllDevicesActivity.this, "access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AllDevicesActivity.this, "access denied!!!", Toast.LENGTH_SHORT).show();
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