package com.jelled.controller.Control.Operation;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.google.gson.Gson;
import com.jelled.controller.Control.BluetoothPayload;
import com.jelled.controller.Exception.JellEDBluetoothException;

public class BleWriteOperation extends BleOperation {

    private final BluetoothGatt bluetoothGatt;
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private final BluetoothPayload payload;

    public BleWriteOperation(final BluetoothDevice bluetoothDevice,
                             final BluetoothGatt bluetoothGatt,
                             final BluetoothGattCharacteristic bluetoothGattCharacteristic,
                             final BluetoothPayload payload) {
        super(bluetoothDevice);
        this.bluetoothGatt = bluetoothGatt;
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        this.payload = payload;
    }

    @Override
    public void execute() throws JellEDBluetoothException {
        Gson gson = new Gson();
        final byte[] payloadJsonBytes = gson.toJson(payload)
                .getBytes();
        try {
            bluetoothGatt.writeCharacteristic(
                    bluetoothGattCharacteristic,
                    payloadJsonBytes,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            );
        } catch (final SecurityException securityException) {
            throw new JellEDBluetoothException("Unable to connect to device " + bluetoothDevice.getAddress(), securityException);
        }
    }
}
