package com.jelled.controller;

import static com.jelled.controller.BluetoothLeService.ACTION_GATT_CONNECTED;
import static com.jelled.controller.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static com.jelled.controller.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JellEDControlActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    public static final String EXTRAS_DEVICE_ADDRESS = "deviceAddress";

    private BluetoothLeService bluetoothService;

    private String deviceAddress = "";
    private boolean connected = false;

    private final ServiceConnection serviceconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            bluetoothService = ((BluetoothLeService.LocalBinder) binder).getService();
            if (bluetoothService != null) {
                if (!bluetoothService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                bluetoothService.connect(deviceAddress);
                // TODO call function on service to check connection and connect to device
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // TODO Handle discovered
                //displayGattServices(bluetoothService.getSupportedGattServices());
            }
        }
    };

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceStage) {
        super.onCreate(savedInstanceStage);
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            deviceAddress = extras.getString(EXTRAS_DEVICE_ADDRESS);
        }
        // TODO View
        //setContentView(R.layout.);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceconnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            final boolean result = bluetoothService.connect(deviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }
}
