package com.jelled.controller.Control;

import static com.jelled.controller.Control.ColorPickerActivity.COLOR_PICKER_RESULT;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jelled.controller.R;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JellEDControlActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    public static final String EXTRAS_BLUETOOTH_DEVICE = "bluetoothDevice";
    public static final String EXTRAS_BUTTON_IDENTIFIER = "buttonIdentifierExtra";
    public static final String EXTRAS_BUTTON_COLOR = "buttonColorExtra";

    private static final int COLOR_1_INPUT_BUTTON_CODE = 1;
    private static final int COLOR_2_INPUT_BUTTON_CODE = 2;
    private static final int COLOR_3_INPUT_BUTTON_CODE = 3;

    private Button onOffButton;
    private Button color1InputButton;
    private Button color2InputButton;
    private Button color3InputButton;

    private Spinner patternType1Spinner;
    private Spinner patternType2Spinner;
    private Spinner patternType3Spinner;
    private Spinner patternType4Spinner;

    private BluetoothLeService bluetoothService;

    private BluetoothDevice bluetoothDevice;

    static int data = 0;

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> colorPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    switch (data.getExtras().getInt(EXTRAS_BUTTON_IDENTIFIER)) {
                        case COLOR_1_INPUT_BUTTON_CODE:
                            color1InputButton.setBackgroundColor(data.getExtras().getInt(COLOR_PICKER_RESULT));
                            break;
                        case COLOR_2_INPUT_BUTTON_CODE:
                            color2InputButton.setBackgroundColor(data.getExtras().getInt(COLOR_PICKER_RESULT));
                            break;
                        case COLOR_3_INPUT_BUTTON_CODE:
                            color3InputButton.setBackgroundColor(data.getExtras().getInt(COLOR_PICKER_RESULT));
                            break;
                    }
                }
            });

    private final ServiceConnection serviceconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            bluetoothService = ((BluetoothLeService.LocalBinder) binder).getService();
            if (bluetoothService != null) {
                bluetoothService.initialize(bluetoothDevice);
                Log.e(TAG, "Bluetooth Service is connected :)");
            } else {
                Log.e(TAG, "Bluetooth Service is null :(");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
        }
    };

    public JellEDControlActivity() {
    }

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceStage) {
        super.onCreate(savedInstanceStage);
        final Bundle extras = getIntent().getExtras();
        // TODO Do not pass device via extras
        if (extras != null) {
            bluetoothDevice = (BluetoothDevice) extras.get(EXTRAS_BLUETOOTH_DEVICE);
        }

        setContentView(R.layout.jelled_control_layout);

        color1InputButton = findViewById(R.id.color_1_input_button);
        color2InputButton = findViewById(R.id.color_2_input_button);
        color3InputButton = findViewById(R.id.color_3_input_button);

        patternType1Spinner = findViewById(R.id.patternTypeSpinner1);
        patternType2Spinner = findViewById(R.id.patternTypeSpinner2);
        patternType3Spinner = findViewById(R.id.patternTypeSpinner3);
        patternType4Spinner = findViewById(R.id.patternTypeSpinner4);

        color1InputButton.setOnClickListener(view -> {
            Intent colorPickerActivityIntent = new Intent(this, ColorPickerActivity.class);
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_COLOR, color1InputButton.getHighlightColor());
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_IDENTIFIER, COLOR_1_INPUT_BUTTON_CODE);
            colorPickerActivityResultLauncher.launch(colorPickerActivityIntent);
        });

        color2InputButton.setOnClickListener(view -> {
            Intent colorPickerActivityIntent = new Intent(this, ColorPickerActivity.class);
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_COLOR, color1InputButton.getHighlightColor());
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_IDENTIFIER, COLOR_2_INPUT_BUTTON_CODE);
            colorPickerActivityResultLauncher.launch(colorPickerActivityIntent);
        });

        color3InputButton.setOnClickListener(view -> {
            Intent colorPickerActivityIntent = new Intent(this, ColorPickerActivity.class);
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_COLOR, color1InputButton.getHighlightColor());
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_IDENTIFIER, COLOR_3_INPUT_BUTTON_CODE);
            colorPickerActivityResultLauncher.launch(colorPickerActivityIntent);
        });

        final List<String> patternTypes =
                Arrays.stream(PatternType.values()).map(PatternType::name).collect(Collectors.toList());

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item,
                        patternTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        patternType1Spinner.setAdapter(spinnerAdapter);
        patternType2Spinner.setAdapter(spinnerAdapter);
        patternType3Spinner.setAdapter(spinnerAdapter);
        patternType4Spinner.setAdapter(spinnerAdapter);

        onOffButton = findViewById(R.id.on_off_button);

        onOffButton.setOnClickListener(view -> {
            bluetoothService.writePackage(new BluetoothPayload("" + data++));
        });

//        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(gattServiceIntent, serviceconnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
