package com.jelled.controller.Control.Operation;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.util.Log;

import com.jelled.controller.Exception.JellEDBluetoothException;

public class BleConnectOperation extends BleOperation {

    private static final String TAG = "ConnectBle";
    private final Context context;
    private final BluetoothGattCallback bluetoothGattCallback;

    public BleConnectOperation(final BluetoothDevice bluetoothDevice,
                               final Context context,
                               final BluetoothGattCallback bluetoothGattCallback) {
        super(bluetoothDevice);
        this.context = context;
        this.bluetoothGattCallback = bluetoothGattCallback;
    }

    @Override
    public void execute() throws JellEDBluetoothException {
        try {
            bluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        } catch (final IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address " + bluetoothDevice.getAddress());
            return;
        } catch (final SecurityException securityException) {
            throw new JellEDBluetoothException("Unable to connect to device " + bluetoothDevice.getAddress(), securityException);
        }
    }
}
