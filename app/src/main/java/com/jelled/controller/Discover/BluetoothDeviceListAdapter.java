package com.jelled.controller.Discover;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jelled.controller.Alert.AlertDialogFactory;
import com.jelled.controller.Control.JellEDControlActivity;
import com.jelled.controller.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class BluetoothDeviceListAdapter extends ListAdapter<BluetoothDevice, BluetoothDeviceListAdapter.BluetoothDeviceViewHolder> {

    private static final String TAG = "BluetoothDeviceListAdapter";
    private static final String DEVICE_NAME_TEMPLATE = "Name: %s";
    private static final String DEVICE_ADDRESS_TITLE = "Address: ";

    private final Context context;
    private final List<BluetoothDevice> devices;

    private final BiConsumer<Context, BluetoothDevice> onConnectConsumer;

    protected BluetoothDeviceListAdapter(final Context context,
         @NonNull DiffUtil.ItemCallback<BluetoothDevice> diffCallback,
         final BiConsumer<Context, BluetoothDevice> onConnectConsumer) {
        super(diffCallback);
        this.context = context;
        this.onConnectConsumer = onConnectConsumer;
        devices = new ArrayList<>();
    }

    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        final View deviceListView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_layout, parent, false);

        return new BluetoothDeviceViewHolder(context, deviceListView);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceViewHolder holder, int position) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        holder.bind(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    void addDevice(final BluetoothDevice device) {
        if (this.devices.contains(device)) {
            return;
        }
        this.devices.add(device);
        this.notifyItemChanged(devices.size() - 1);
    }

    void clear() {
        final int deviceCount = devices.size();
        this.devices.clear();
        this.notifyItemRangeRemoved(0, deviceCount);
    }

    class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final TextView deviceNameView;
        private final TextView deviceAddressTitleView;
        private final TextView deviceAddressView;
        private final Map<String, BluetoothDevice> devices;

        public BluetoothDeviceViewHolder(@NonNull final Context context, @NonNull final View itemView) {
            super(itemView);
            this.context = context;
            devices = new HashMap<>();
            this.deviceNameView = itemView.findViewById(R.id.device_name_view);
            this.deviceAddressTitleView = itemView.findViewById(R.id.device_address_title_view);
            this.deviceAddressView = itemView.findViewById(R.id.device_address_view);

            itemView.setOnClickListener(view -> {
                final String log = String.format("Device Name: %s Device Address: %s", deviceNameView.getText(), deviceAddressView.getText());
                Log.i(TAG, "On click " + log);
                onConnectConsumer.accept(context, devices.get(deviceAddressView.getText().toString()));
            });
        }

        public void bind(final BluetoothDevice device) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                AlertDialogFactory.createInfoDialog(context, AlertDialogFactory.DialogType.ERROR, "Cannot show name of device. Permission not granted.").show();
                return;
            }
            this.devices.put(device.getAddress(), device);
            this.deviceNameView.setText(String.format(DEVICE_NAME_TEMPLATE, device.getName()));
            this.deviceAddressTitleView.setText(String.format(DEVICE_ADDRESS_TITLE, device.getAddress()));
            this.deviceAddressView.setText(device.getAddress());
        }
    }
}
