package com.jelled.controller.Discover;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jelled.controller.Alert.AlertDialogFactory;
import com.jelled.controller.Control.JellEDControlActivity;
import com.jelled.controller.R;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class DeviceScanActivity extends AppCompatActivity {

    private static final String TAG = "DeviceScanActivity";
    private static final String JELLED_DEVICE_NAME = "jellED";
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

    private static final long SCAN_PERIOD = 2;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private final ScheduledExecutorService scanningScheduler = new ScheduledThreadPoolExecutor(1);

    private ActivityResultLauncher<Intent> bluetoothActivationResultLauncher;

    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean scanning;

    private final ScanFilter scanFilter = new ScanFilter.Builder()
            .setDeviceName(JELLED_DEVICE_NAME)
            .build();

    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
            .build();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            deviceListAdapter.addDevice(result.getDevice());
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
    }, new BiConsumer<Context, BluetoothDevice>() {
        @Override
        public void accept(Context context, BluetoothDevice bluetoothDevice) {
            Intent controlActivityIntent = new Intent(context, JellEDControlActivity.class);
            controlActivityIntent.putExtra(JellEDControlActivity.EXTRAS_BLUETOOTH_DEVICE, bluetoothDevice);
            context.startActivity(controlActivityIntent);
        }
    } );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

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
                        failWithMessage("Bluetooth is not activated");
                    } else {
                        scanLeDevices();
                    }
                }
        );

        setContentView(R.layout.activity_device_scan);

        swipeRefreshLayout = findViewById(R.id.bluetooth_swipe_refresh_view);
        swipeRefreshLayout.setOnRefreshListener( () -> {
            deviceListAdapter.clear();
            scanLeDevices();
        });

        RecyclerView recyclerView = findViewById(R.id.bluetooth_devices_view);
        recyclerView.setAdapter(deviceListAdapter);

        scanLeDevices();
    }

    /**
        This method is called back after permission request in checkAndRequestPermission
        was answered.
     */
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

    private void failWithMessage(final String message) {
        AlertDialogFactory.newDefaultBuilder(this, AlertDialogFactory.DialogType.ERROR,
                message)
                .setPositiveButton("OK", (dialog, id) -> {
                    throw new RuntimeException(message);
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

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private void enableLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private boolean checkAndRequestPermission(final String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    BLUETOOTH_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void scanLeDevices() {
        if (!isBluetoothEnabled()) {
            enableBluetooth();
        }

        if (!isLocationEnabled()) {
            enableLocationSettings();
        }

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
                swipeRefreshLayout.setRefreshing(false);
            }, SCAN_PERIOD, TimeUnit.SECONDS);
            scanning = true;
            bluetoothScanner.startScan(List.of(scanFilter), scanSettings, scanCallback);
        } else {
            scanning = false;
            bluetoothScanner.stopScan(scanCallback);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
