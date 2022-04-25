package com.example.bluetoothapp;

import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {
    public static final String WHICH_BLUETOOTH = "whichBluetooth";
    private static final String TAG = "CHECK";
    // declare
    private Button btnScan, btnHistory, btnLeScan;
    private SwitchMaterial switchBluetooth;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startBluetooth.launch(enableBtIntent);

                } else {
                    if (bluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            requestPermission(Manifest.permission.BLUETOOTH,1);
                        }
                        bluetoothManager.getAdapter().disable();

                    }

                }

            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. need to pass a context as a src. MainActivity is a context here
                // 2. a destination of another activity
                Intent intent = new Intent(MainActivity.this, AllDevicesActivity.class);
                intent.putExtra(WHICH_BLUETOOTH, "notBleScan");
                // inner method every activity has
                startActivity(intent);
            }
        });
        btnLeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AllDevicesActivity.class);
                intent.putExtra(WHICH_BLUETOOTH, "BleScan");
                startActivity(intent);
            }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }
    ActivityResultLauncher<Intent> startBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result != null && result.getResultCode() == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                switchBluetooth.setText("On");
            }
            else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    });
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initViews() {
        //inner method
        btnScan = findViewById(R.id.btnScan);
        btnLeScan = findViewById(R.id.btnLeScan);
        btnHistory = findViewById(R.id.btnHistory);
        switchBluetooth = findViewById(R.id.switchBluetooth);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null) {
            Log.d(TAG, "your device has no bluetooth");
            Toast.makeText(this, "Your device does not support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            switchBluetooth.setChecked(bluetoothAdapter.isEnabled());
            if(switchBluetooth.isChecked()) {
                switchBluetooth.setText("On");
            } else {
                switchBluetooth.setText("Off");
            }
        }

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

                    Toast.makeText(MainActivity.this, "Permission to turn off Granted!", Toast.LENGTH_SHORT).show();
                    switchBluetooth.setText("Off");
                } else {
                    Toast.makeText(MainActivity.this, "Permission to turn off Denied!!!", Toast.LENGTH_SHORT).show();
                }
        }
    }
}