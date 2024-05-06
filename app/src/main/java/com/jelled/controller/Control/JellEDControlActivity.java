package com.jelled.controller.Control;

import static com.jelled.controller.Control.ColorPickerActivity.COLOR_PICKER_RESULT;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jelled.controller.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JellEDControlActivity extends AppCompatActivity {

    private static final String TAG = "JellEDControlActivity";

    public static final String EXTRAS_BLUETOOTH_DEVICE = "bluetoothDevice";
    public static final String EXTRAS_BUTTON_IDENTIFIER = "buttonIdentifierExtra";
    public static final String EXTRAS_BUTTON_COLOR = "buttonColorExtra";

    private static final int COLOR_1_INPUT_BUTTON_CODE = 0;
    private static final int COLOR_2_INPUT_BUTTON_CODE = 1;
    private static final int COLOR_3_INPUT_BUTTON_CODE = 2;

    private Button color1InputButton;
    private Button color2InputButton;
    private Button color3InputButton;

    private EditText beatsPerPatternEditText;

    private Spinner patternType1Spinner;
    private Spinner patternType2Spinner;
    private Spinner patternType3Spinner;
    private Spinner patternType4Spinner;
    private Button sendPayloadButton;

    private BluetoothLeService bluetoothService;

    private BluetoothDevice bluetoothDevice;

    private List<Integer> selectedColors;


    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> colorPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    final int selectedColor = data.getExtras().getInt(COLOR_PICKER_RESULT);
                    switch (data.getExtras().getInt(EXTRAS_BUTTON_IDENTIFIER)) {
                        case COLOR_1_INPUT_BUTTON_CODE:
                            color1InputButton.setBackgroundColor(selectedColor);
                            selectedColors.set(COLOR_1_INPUT_BUTTON_CODE, selectedColor);
                            break;
                        case COLOR_2_INPUT_BUTTON_CODE:
                            color2InputButton.setBackgroundColor(selectedColor);
                            selectedColors.set(COLOR_2_INPUT_BUTTON_CODE, selectedColor);
                            break;
                        case COLOR_3_INPUT_BUTTON_CODE:
                            color3InputButton.setBackgroundColor(selectedColor);
                            selectedColors.set(COLOR_3_INPUT_BUTTON_CODE, selectedColor);
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

        int defaultColor = -45108;

        selectedColors = new ArrayList<>(3);
        selectedColors.add(0, defaultColor);
        selectedColors.add(1, defaultColor);
        selectedColors.add(2, defaultColor);

        setContentView(R.layout.jelled_control_layout);

        color1InputButton = findViewById(R.id.color_1_input_button);
        color2InputButton = findViewById(R.id.color_2_input_button);
        color3InputButton = findViewById(R.id.color_3_input_button);

        color1InputButton.setBackgroundColor(defaultColor);
        color2InputButton.setBackgroundColor(defaultColor);
        color3InputButton.setBackgroundColor(defaultColor);

        beatsPerPatternEditText = findViewById(R.id.plain_text_input);

        beatsPerPatternEditText.setText("4");

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

        sendPayloadButton = findViewById(R.id.sendPayloadButton);

        sendPayloadButton.setOnClickListener(view -> {
            final BluetoothPayload payload = new BluetoothPayload.Builder()
                    .withPatternType1(getPatternTypeFromSpinner(patternType1Spinner))
                    .withPatternType2(getPatternTypeFromSpinner(patternType2Spinner))
                    .withPatternType3(getPatternTypeFromSpinner(patternType3Spinner))
                    .withPatternType4(getPatternTypeFromSpinner(patternType4Spinner))
                    .withBeatsPerPattern(Integer.parseInt(beatsPerPatternEditText.getText().toString()))
                    .withColor1(selectedColors.get(COLOR_1_INPUT_BUTTON_CODE))
                    .withColor2(selectedColors.get(COLOR_2_INPUT_BUTTON_CODE))
                    .withColor3(selectedColors.get(COLOR_3_INPUT_BUTTON_CODE))
                    .build();
            bluetoothService.writePackage(payload);
        });

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceconnection, Context.BIND_AUTO_CREATE);
    }

    private PatternType getPatternTypeFromSpinner(final Spinner spinner) {
        return Optional
                .ofNullable(spinner.getSelectedItem())
                .map(Object::toString)
                .map(PatternType::fromString)
                .orElse(PatternType.values()[0]);
    }

    private int getButtonColor(final Button button) {
        final Drawable buttonBackgroud = button.getBackground();
        if (buttonBackgroud instanceof ColorDrawable) {
            return ((ColorDrawable) buttonBackgroud).getColor();
        }
        return 0;
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
