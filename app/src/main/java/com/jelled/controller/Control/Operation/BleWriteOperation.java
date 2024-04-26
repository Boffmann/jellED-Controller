package com.jelled.controller.Control.Operation;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.jelled.controller.Exception.JellEDBluetoothException;

public class BleWriteOperation extends BleOperation {

    private final BluetoothGatt bluetoothGatt;
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;

    public BleWriteOperation(final BluetoothDevice bluetoothDevice,
                             final BluetoothGatt bluetoothGatt,
                             final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super(bluetoothDevice);
        this.bluetoothGatt = bluetoothGatt;
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    @Override
    public void execute() throws JellEDBluetoothException {
        try {
            bluetoothGatt.writeCharacteristic(
                    bluetoothGattCharacteristic,
                    "Test".getBytes(),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            );
        } catch (final SecurityException securityException) {
            throw new JellEDBluetoothException("Unable to connect to device " + bluetoothDevice.getAddress(), securityException);
        }
    }
}
