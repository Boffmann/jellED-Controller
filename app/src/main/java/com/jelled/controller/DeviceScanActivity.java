package com.jelled.controller;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jelled.controller.Alert.AlertDialogFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeviceScanActivity extends AppCompatActivity {

    private static final String TAG = "DeviceScanActivity";
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

    private static final long SCAN_PERIOD = 2;

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
    private final ScheduledExecutorService scanningScheduler = new ScheduledThreadPoolExecutor(1);

    private ActivityResultLauncher<Intent> bluetoothActivationResultLauncher;

    private boolean scanning;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            deviceListAdapter.addDevice(result.getDevice());
        }

        @Override
        public void onScanFailed (int errorCode) {
            super.onScanFailed(errorCode);
            // TODO
        }
    };

    private final BluetoothDeviceListAdapter deviceListAdapter = new BluetoothDeviceListAdapter(this, new DiffUtil.ItemCallback<BluetoothDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return Objects.equals(oldItem.getAddress(), newItem.getAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem.equals(newItem);
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Todo Error handling in case of crash/exception

        if (!isBluetoothSupported()) {
            AlertDialogFactory.newDefaultBuilder(this, AlertDialogFactory.DialogType.ERROR,
                    "Bluetooth is not supported by this device")
                    .setPositiveButton("OK", (dialog, id) -> {
                        throw new RuntimeException("Bluetooth is not supported");
                    })
                    .create()
                    .show();
        }

        bluetoothActivationResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        failWithBluetoothNotEnabled();
                    } else {
                        scanLeDevices();
                    }
                }
        );

        if (!isBluetoothEnabled()) {
            enableBluetooth();
        }

        setContentView(R.layout.activity_device_scan);

        RecyclerView recyclerView = findViewById(R.id.bluetooth_devices_view);
        recyclerView.setAdapter(deviceListAdapter);

        scanLeDevices();
    }

    /**
        This method is called back after permission request in checkAndRequestPermission
        was answered.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanLeDevices();
                }
            }
        }
    }

    private void failWithBluetoothNotEnabled() {
        AlertDialogFactory.newDefaultBuilder(this, AlertDialogFactory.DialogType.ERROR,
                "Bluetooth is not activated")
                .setPositiveButton("OK", (dialog, id) -> {
                    throw new RuntimeException("Bluetooth is not activated");
                })
                .create()
                .show();
    }

    private boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    private boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    private void enableBluetooth() {
        final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothActivationResultLauncher.launch(enableBtIntent);
    }

    private boolean checkAndRequestPermission(final String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    BLUETOOTH_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void scanLeDevices() {
        if (bluetoothScanner == null) {
            // Bluetooth scanner might be null because of different reasons. For example because
            // bluetooth was not activated once the activity was created
            bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothScanner == null) {
                Log.w(TAG, "Bluetooth Scanner not available");
                return;
            }
        }
        if (!checkAndRequestPermission(BLUETOOTH_SCAN)) {
            return;
        }
        if (!checkAndRequestPermission(BLUETOOTH_CONNECT)) {
            return;
        }
        if (!checkAndRequestPermission(ACCESS_COARSE_LOCATION)) {
            return;
        }
        if (!checkAndRequestPermission(ACCESS_FINE_LOCATION)) {
            return;
        }

        if (!scanning) {
            scanningScheduler.schedule(() -> {
                scanning = false;
                bluetoothScanner.stopScan(scanCallback);
            }, SCAN_PERIOD, TimeUnit.SECONDS);
            scanning = true;
            bluetoothScanner.startScan(scanCallback);
        } else {
            scanning = false;
            bluetoothScanner.stopScan(scanCallback);
        }
    }
}
