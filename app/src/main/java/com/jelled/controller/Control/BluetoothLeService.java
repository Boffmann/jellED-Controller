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
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.jelled.controller.Control.Operation.BleConnectOperation;
import com.jelled.controller.Control.Operation.BleOperation;
import com.jelled.controller.Control.Operation.BleWriteOperation;
import com.jelled.controller.Exception.JellEDBluetoothException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothLeService extends Service {

    private static final String TAG = "BluetoothLeService";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;


    private static final UUID JELLED_SERVICE_UUID = UUID.fromString("665aa768-49c8-11ee-be56-0242ac120002");
    private static final UUID JELLED_CHARACTERISTIC_UUID = UUID.fromString("d0756476-49c8-11ee-be56-0242ac120002");

    private final Binder binder = new LocalBinder();

    private BluetoothDevice bluetoothDevice;

    private BluetoothGatt bluetoothGatt = null;

    private BluetoothGattCharacteristic writeCharacteristic;

    private final CommandQueue commandQueue = new CommandQueue();

    private int connectionState;

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
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

            bluetoothGatt = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                // TODO Call on Main/UI Thread?
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                gatt.close();
            }
            commandQueue.signalOperationCompleted();
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
            commandQueue.signalOperationCompleted();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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
        if (checkPermission()) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
    }

    void initialize(final BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            final BleOperation bleOperation = commandQueue.getNextOperation();
            if (bleOperation != null) {
                try {
                    bleOperation.execute();
                } catch (JellEDBluetoothException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    void writePackage(final int data) throws SecurityException {
        if (connectionState != STATE_CONNECTED || this.bluetoothGatt == null || this.writeCharacteristic == null) {
            Log.e(TAG, "error: Not connected, cannot schedule write operation. " +
            "Will schedule connect instead.");
            connect();
            return;
        }
        this.commandQueue.scheduleOperation(new BleWriteOperation(bluetoothDevice, bluetoothGatt, writeCharacteristic));
    }

    private void connect() throws SecurityException {
        this.commandQueue.scheduleOperation(new BleConnectOperation(bluetoothDevice, this, bluetoothGattCallback));
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
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED;
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private static class CommandQueue {
        private final ConcurrentLinkedQueue<BleOperation> fifo;
        private boolean isOperationPending;

        CommandQueue() {
            fifo = new ConcurrentLinkedQueue<>();
            isOperationPending = false;
        }

        void scheduleOperation(final BleOperation operation) {
            this.fifo.add(operation);
        }

        BleOperation getNextOperation() {
            if (isOperationPending) {
                return null;
            }
            final BleOperation nextOperation = fifo.poll();
            isOperationPending = nextOperation != null;
            return nextOperation;
        }

        void signalOperationCompleted() {
            isOperationPending = false;
        }
    }
}
