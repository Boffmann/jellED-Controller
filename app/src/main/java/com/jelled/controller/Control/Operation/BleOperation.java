package com.jelled.controller.Control.Operation;

import android.bluetooth.BluetoothDevice;

import com.jelled.controller.Exception.JellEDBluetoothException;

public abstract class BleOperation {
    protected final BluetoothDevice bluetoothDevice;

    public BleOperation(final BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public abstract void execute() throws JellEDBluetoothException;
}
