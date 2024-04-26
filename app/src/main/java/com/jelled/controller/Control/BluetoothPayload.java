package com.jelled.controller.Control;

public class BluetoothPayload {
    private final String data;

    public BluetoothPayload(final String data) {
        this.data = data;
    }

    public byte[] getAsBytes() {
        return data.getBytes();
    }
}
