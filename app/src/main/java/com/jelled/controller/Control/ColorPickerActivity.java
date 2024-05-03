package com.jelled.controller.Control;

import static com.jelled.controller.Control.JellEDControlActivity.EXTRAS_BUTTON_COLOR;
import static com.jelled.controller.Control.JellEDControlActivity.EXTRAS_BUTTON_IDENTIFIER;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.antonpopoff.colorwheel.ColorWheel;
import com.github.antonpopoff.colorwheel.gradientseekbar.GradientSeekBar;
import com.jelled.controller.R;

import kotlin.Unit;

public class ColorPickerActivity extends AppCompatActivity {

    public static String COLOR_PICKER_RESULT = "colorPickerResult";

    private LinearLayout backgroundView;
    private ColorWheel colorWheel;
    private GradientSeekBar gradientSeekBar;
    private Button confirmButton;

    private int resultColor;
    private int callerIdentifier;

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceStage) {
        super.onCreate(savedInstanceStage);
        final Bundle extras = getIntent().getExtras();

        setContentView(R.layout.color_picker_layout);

        backgroundView = findViewById(R.id.colorPickerViewBackground);
        colorWheel = findViewById(R.id.colorWheel);
        gradientSeekBar = findViewById(R.id.gradientSeekBar);
        confirmButton = findViewById(R.id.colorPickerConfirmButton);

        if (extras != null) {
            backgroundView.setBackgroundColor(extras.getInt(EXTRAS_BUTTON_COLOR));
            callerIdentifier = extras.getInt(EXTRAS_BUTTON_IDENTIFIER);
        }

        gradientSeekBar.setOffset(1.f);

        colorWheel.setColorChangeListener(color -> {
            backgroundView.setBackgroundColor(color);
            gradientSeekBar.setStartColor(0);
            gradientSeekBar.setEndColor(color);
            resultColor = color;
            return Unit.INSTANCE;
        });

        gradientSeekBar.setColorChangeListener((offset, color) -> {
            backgroundView.setBackgroundColor(color);
            resultColor = color;
            return Unit.INSTANCE;
        });

        confirmButton.setOnClickListener(view -> {
            Intent colorPickerActivityIntent = new Intent();
            colorPickerActivityIntent.putExtra(EXTRAS_BUTTON_IDENTIFIER, callerIdentifier);
            colorPickerActivityIntent.putExtra(COLOR_PICKER_RESULT, resultColor);
            this.setResult(Activity.RESULT_OK, colorPickerActivityIntent);
            onBackPressed();
        });
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
