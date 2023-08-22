package com.jelled.controller;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    private final List<String> REQUIRED_PERMISSIONS = List.of(BLUETOOTH_SCAN, BLUETOOTH_CONNECT);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checkPermissions();
        Intent scanBluetoothDevicesIntent = new Intent(this, DeviceScanActivity.class);
        this.startActivity(scanBluetoothDevicesIntent);
    }

    /*private void checkPermissions() {
        final List<String> nonGrantedPermissions = new ArrayList<>();
        REQUIRED_PERMISSIONS.forEach(permission -> {
            final int permissionGrantType = ActivityCompat.checkSelfPermission(this, permission);
            if (permissionGrantType != PackageManager.PERMISSION_GRANTED) {
                nonGrantedPermissions.add(permission);
                //ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
            }
        });
        String[] requiredPermissions = new String[nonGrantedPermissions.size()];

        for (int i = 0; i < nonGrantedPermissions.size(); i++) {
            Log.i(TAG, "Required Permission: " + nonGrantedPermissions.get(i));
            requiredPermissions[i] = nonGrantedPermissions.get(i);
        }
        ActivityCompat.requestPermissions(this, requiredPermissions, 1);
        //ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
    }*/
}