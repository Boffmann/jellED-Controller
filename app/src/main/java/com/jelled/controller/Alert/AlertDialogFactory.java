package com.jelled.controller.Alert;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class AlertDialogFactory {

    public enum DialogType {

        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR");

        private final String value;

        DialogType(final String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    public static AlertDialog createInfoDialog(final Context context, final DialogType type,
                                             final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(type.getValue());
        builder.setPositiveButton("OK", (dialog, id) -> {
            // User clicked OK button
        });
        return builder.create();
    }

    public static AlertDialog.Builder newDefaultBuilder(final Context context, final DialogType type, final String message) {
        return new AlertDialog.Builder(context)
            .setMessage(message)
            .setTitle(type.getValue());
    }

    public static void setOnClickListener(final AlertDialog alertDialog, View.OnClickListener l) {
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(l);
    }
}
