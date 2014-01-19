package com.docd.purefm.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.docd.purefm.R;

import org.jetbrains.annotations.NotNull;

public final class MessageDialog {

    @NotNull
    public static Dialog create(final Context context, final int titleRes, final CharSequence message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
