package com.jelled.controller.Control;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jelled.controller.R;

public class JellEDControlActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    public static final String EXTRAS_BLUETOOTH_DEVICE = "bluetoothDevice";

    private Button onOffButton;

    private BluetoothLeService bluetoothService;

    private BluetoothDevice bluetoothDevice;

    private final ServiceConnection serviceconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            bluetoothService = ((BluetoothLeService.LocalBinder) binder).getService();
            if (bluetoothService != null) {
                bluetoothService.initialize(bluetoothDevice);
                Log.e(TAG, "Bluetooth Service is connected :)");
            } else {
                Log.e(TAG, "Bluetooth Service is null :(");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
        }
    };

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceStage) {
        super.onCreate(savedInstanceStage);
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bluetoothDevice = (BluetoothDevice) extras.get(EXTRAS_BLUETOOTH_DEVICE);
        }

        setContentView(R.layout.jelled_control);

        onOffButton = findViewById(R.id.on_off_button);

        onOffButton.setOnClickListener(view -> {
            bluetoothService.writePackage(1);
        });

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceconnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (bluetoothService != null) {
//            final boolean result = bluetoothService.connect(bluetoothDevice);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
