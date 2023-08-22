package com.jelled.controller.Alert;

import android.app.AlertDialog;
import android.content.Context;

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

    public static AlertDialog createOkDialog(final Context context, final DialogType type,
                                             final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(type.getValue());
        builder.setPositiveButton("OK", (dialog, id) -> {
            // User clicked OK button
        });
        return builder.create();
    }
}
