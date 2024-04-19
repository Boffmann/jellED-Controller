package com.jelled.controller.Control;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION;
import static android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private static final String TAG = "BluetoothLeService";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;


    private static final UUID JELLED_SERVICE_UUID = UUID.fromString("665aa768-49c8-11ee-be56-0242ac120002");
    private static final UUID JELLED_CHARACTERISTIC_UUID = UUID.fromString("d0756476-49c8-11ee-be56-0242ac120002");

    private final Binder binder = new LocalBinder();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private BluetoothGatt bluetoothGatt = null;

    private BluetoothGattCharacteristic writeCharacteristic;

    private int connectionState;

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) throws SecurityException {
            if (status != GATT_SUCCESS) {
                Log.e(TAG, "Error detected when getting connection stage change to state: " + newState +". Status was: " + status);
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                }
                if (status == GATT_INSUFFICIENT_ENCRYPTION || status == GATT_INSUFFICIENT_AUTHENTICATION) {
                    // TODO Initiate Bond
                    bluetoothDevice.createBond();
                }
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                // TODO Call on Main/UI Thread?
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered received success " + status);
                discoverWriteCharacteristic();
            } else {
                Log.w(TAG, "onServicesDiscovered received " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] value = characteristic.getValue();
            if (status == GATT_SUCCESS) {
                Log.i(TAG, "Successfully wrote to characteristic " + Arrays.toString(value));
            } else if (status == GATT_INVALID_ATTRIBUTE_LENGTH) {
                Log.e(TAG, "Write exceeded connection ATT MTU");
            } else if (status == GATT_WRITE_NOT_PERMITTED) {
                Log.e(TAG, "Write to characteristic not permitted");
            } else {
                Log.e(TAG, "Write failed with error: " + status);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        if (checkPermission()) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
    }

    void initialize(final BluetoothDevice bluetoothDevice) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return;
        }
        this.bluetoothDevice = bluetoothDevice;
    }

    void writePackage(final int data) throws SecurityException {
        if (this.connectionState != STATE_CONNECTED) {
            connect();
        }
        // TODO Schedule write data
    }

    private void writeToCharacteristic(final int data) throws SecurityException {
        bluetoothGatt.writeCharacteristic(
                writeCharacteristic,
                "Test".getBytes(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }

    private boolean connect() throws SecurityException {
        if (bluetoothAdapter == null || bluetoothDevice == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified bluetoothDevice.");
            return false;
        }

//        Log.i(TAG, "Got address: " + address);
//        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
            return true;
        } catch (final IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address " + bluetoothDevice.getAddress());
            return false;
        }
    }

    private void discoverWriteCharacteristic() {
        if (bluetoothGatt.getServices().isEmpty()) {
            Log.e(TAG, "No Service and Characteristics available.");
            return;
        }
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            if (service.getUuid().equals(JELLED_SERVICE_UUID)) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().equals(JELLED_CHARACTERISTIC_UUID)) {
                        Log.i(TAG, "Found Characteristic " + characteristic.getUuid().toString());
                        if ((characteristic.getProperties() & PROPERTY_WRITE) == 0) {
                            throw new RuntimeException("Error: Characteristic is supposed to be writable");
                        } else {
                            this.writeCharacteristic = characteristic;
                            writeToCharacteristic(1);
                            return;
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            return null;
        }
        return bluetoothGatt.getServices();
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
