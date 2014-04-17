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

import com.docd.purefm.R;

import android.support.annotation.NonNull;

public final class MessageDialogBuilder {

    private MessageDialogBuilder() {
        // empty
    }

    @NonNull
    public static Dialog create(@NonNull final Context context,
                                final int titleRes,
                                @NonNull final CharSequence message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.close, null);
        return builder.create();
    }
}
