/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.docd.purefm.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProgressAlertDialogBuilder {

    private ProgressAlertDialogBuilder() {
        //empty
    }

    /**
     * Creates a non-cancelable {@link AlertDialog} with progress bar and Cancel button
     *
     * @param context Current Context
     * @param messageResId Message text res id
     * @param cancelListener Cancel button listener
     * @return non-cancelable {@link AlertDialog} with progress bar and Cancel button
     */
    public static Dialog create(@NotNull final Context context,
                                final int messageResId,
                                @Nullable final View.OnClickListener cancelListener) {
        return create(context, messageResId <= 0 ? null :
                context.getText(messageResId),cancelListener);
    }

    /**
     * Creates a non-cancelable {@link AlertDialog} with progress bar and Cancel button
     *
     * @param context Current Context
     * @param message Message text
     * @param cancelListener Cancel button listener
     * @return non-cancelable {@link AlertDialog} with progress bar and Cancel button
     */
    public static Dialog create(@NotNull final Context context,
                                @Nullable final CharSequence message,
                                @Nullable final View.OnClickListener cancelListener) {
        final CancelableProgressDialog dialog = new CancelableProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelButton(android.R.string.cancel, cancelListener);
        return dialog;
    }

    private static final class CancelableProgressDialog extends Dialog
            implements View.OnClickListener {

        private boolean mClickPressed;
        private CharSequence mMessage;

        private int mCancelTextId = -1;
        private View.OnClickListener mCancelListener;


        CancelableProgressDialog(final Context context) {
            super(context, false, null);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(LayoutInflater.from(getContext()).inflate(
                    R.layout.dialog_progress, null));
            final TextView messageView = (TextView) findViewById(android.R.id.message);
            messageView.setText(mMessage);
            messageView.setVisibility(mMessage == null ? View.GONE : View.VISIBLE);

            final TextView button = (TextView) findViewById(R.id.button);
            button.setOnClickListener(this);
            if (mCancelTextId != -1) {
                button.setText(mCancelTextId);
            }
        }

        public void setMessage(@Nullable final CharSequence message) {
            final TextView messageView = (TextView) findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setText(message);
                messageView.setVisibility(message == null ? View.GONE : View.VISIBLE);
            } else {
                mMessage = message;
            }
        }

        public void setCancelButton(final int text, @Nullable final View.OnClickListener l) {
            mCancelListener = l;

            final TextView button = (TextView) findViewById(R.id.button);
            if (button != null) {
                button.setText(text);
            } else {
                mCancelTextId = text;
            }
        }

        @Override
        public void onClick(final View v) {
            if (!mClickPressed) {
                final TextView textView = (TextView) findViewById(android.R.id.message);
                textView.setText(TextUtils.concat(textView.getText(),
                        textView.getResources().getText(R.string.canceling_suffix)));
            }
            mClickPressed = true;
            if (mCancelListener != null) {
                mCancelListener.onClick(v);
            }
        }
    }
}
