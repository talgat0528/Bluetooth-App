package com.example.bluetoothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class DeviceActivity extends AppCompatActivity {

    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_OBJECT = "deviceObject";
    UUID uuid = UUID.fromString("31385f4a-83c1-4970-8436-09d8a132774c");
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_CONNECTION_FAILED = 2;
    private Button btnConnectToDevice, btnDiscFromDevice;
    private TextView txtDeviceTitle;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothDevice btDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        initViews();

        Intent intent = getIntent();
        if (intent != null) {
            Device deviceObj = (Device) intent.getExtras().getSerializable(DEVICE_OBJECT);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            txtDeviceTitle.setText(deviceObj.getName());
            btDevice = deviceObj.getBtdevice();
        }
        btnConnectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectThread connectThread = new ConnectThread(btDevice);
                connectThread.start();
            }
        });

    }

    private void initViews() {
        txtDeviceTitle = findViewById(R.id.txtDeviceTitle);
        btnConnectToDevice = findViewById(R.id.btnConnectToDevcie);
        btnDiscFromDevice = findViewById(R.id.btnDiscFromDevice);
    }

    Handler btHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTED:
                    //set the text
                    Toast.makeText(DeviceActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                    break;
                case STATE_CONNECTION_FAILED:
                    //set the text
                    Toast.makeText(DeviceActivity.this, "CONNECTION FAILED", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return true;
        }
    });
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

                    Toast.makeText(DeviceActivity.this, "access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeviceActivity.this, "access denied!!!", Toast.LENGTH_SHORT).show();
                }
        }
    }
    private class ConnectThread extends Thread {
        private BluetoothDevice device1;
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {

            device1 = device;
            try {
                if (ActivityCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details
                    requestPermission(Manifest.permission.BLUETOOTH, 1);
                }

                socket = device1.createRfcommSocketToServiceRecord(uuid);
                //socket = device1.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                btHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                btHandler.sendMessage(message);
            }
        }
    }
}