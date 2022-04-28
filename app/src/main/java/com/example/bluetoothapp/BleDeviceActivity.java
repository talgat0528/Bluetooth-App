package com.example.bluetoothapp;

import static com.example.bluetoothapp.AllDevicesActivity.BLUETOOTH_PERMISSION;
import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleDeviceActivity extends AppCompatActivity {
    private static final String TAG = "BLUETOOTH";
    private BluetoothLeService bluetoothService;
    private String deviceAddress;
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
                    bluetoothService.connect(deviceAddress);
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
        setContentView(R.layout.activity_ble_devices);
        Intent intent = getIntent();
        if (intent != null) {
            deviceAddress = intent.getStringExtra(DEVICE_ADDRESS);
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    class BluetoothLeService extends Service {
        public static final String TAG = "BluetoothLeService";
        public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
        public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
        public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
        public static final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
        ;
        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTED = 2;


        private int connectionState;
        private BluetoothAdapter bluetoothAdapter;
        private BluetoothGatt bluetoothGatt;

        public List<BluetoothGattService> getSupportedGattServices() {
            if (bluetoothGatt == null) return null;
            return bluetoothGatt.getServices();
        }

        public boolean initialize() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }
            return true;
        }

        private Binder binder = new LocalBinder();

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }

        class LocalBinder extends Binder {
            public BluetoothLeService getService() {
                return BluetoothLeService.this;
            }
        }

        @Override
        public boolean onUnbind(Intent intent) {
            close();
            return super.onUnbind(intent);
        }

        private void close() {
            if (bluetoothGatt == null) {
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        public boolean connect(final String address) {
            if (bluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }

            try {
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
                }
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
                return true;
            } catch (IllegalArgumentException exception) {
                Log.w(TAG, "Device not found with provided address.");
                return false;
            }
            // connect to the GATT server on the device
        }

        private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    connectionState = STATE_CONNECTED;
                    broadcastUpdate(ACTION_GATT_CONNECTED);
                    // Attempts to discover services after successful connection.
                    if (ActivityCompat.checkSelfPermission(BleDeviceActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
                    }
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = STATE_DISCONNECTED;
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        };

        private void broadcastUpdate(final String action) {
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }
        public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (bluetoothGatt == null) {
                Log.w(TAG, "BluetoothGatt not initialized");
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION);
            }
            bluetoothGatt.readCharacteristic(characteristic);
        }
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //connected = true;
                //updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //connected = false;
                //updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(bluetoothService.getSupportedGattServices());
            }
        }
    };
    // TODO: display results properly
    private void displayGattServices(List<BluetoothGattService> gattServices) {

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

                    Toast.makeText(BleDeviceActivity.this, "Bluetooth access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Bluetooth access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(BleDeviceActivity.this, "Location access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Location access denied!!!", Toast.LENGTH_SHORT).show();
                }
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(BleDeviceActivity.this, "Bluetooth admin access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleDeviceActivity.this, "Bluetooth admin access denied!!!", Toast.LENGTH_SHORT).show();
                }
        }
    }
}