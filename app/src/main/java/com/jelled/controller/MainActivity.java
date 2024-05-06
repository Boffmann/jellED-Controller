package com.jelled.controller;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.jelled.controller.Control.JellEDControlActivity;
import com.jelled.controller.Discover.DeviceScanActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    private final List<String> REQUIRED_PERMISSIONS = List.of(BLUETOOTH_SCAN, BLUETOOTH_CONNECT);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent scanBluetoothDevicesIntent = new Intent(this, DeviceScanActivity.class);
        this.startActivity(scanBluetoothDevicesIntent);
    }
}