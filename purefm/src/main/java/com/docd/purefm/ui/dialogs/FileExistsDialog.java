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

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import android.support.annotation.NonNull;

public final class FileExistsDialog extends Dialog {

    public interface FileExistsDialogListener {
        void onActionSkip(boolean all);
        void onActionReplace(boolean all);
        void onActionWriteInto(boolean all);
        void onActionAbort();
    }
    
    public FileExistsDialog(
            @NonNull final Context context,
            @NonNull final GenericFile source,
            @NonNull final GenericFile target,
            @NonNull final FileExistsDialogListener listener) {
        super(context);
        
        this.setTitle(R.string.dialog_overwrite_title);
        this.setContentView(R.layout.dialog_exists);
        
        this.initView(source, target, listener);
    }
    
    private void initView(
            @NonNull final GenericFile sourceFile,
            @NonNull final GenericFile targetFile,
            @NonNull final FileExistsDialogListener listener) {
        
        final TextView source = (TextView) findViewById(android.R.id.text1);
        source.setText(sourceFile.getAbsolutePath());
        
        final TextView target = (TextView) findViewById(android.R.id.text2);
        target.setText(targetFile.getAbsolutePath());

        final CompoundButton applyToAll = (CompoundButton) findViewById(android.R.id.checkbox);

        final View writeInto = findViewById(android.R.id.button1);
        if (sourceFile.isDirectory()) {
            writeInto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    listener.onActionWriteInto(applyToAll.isChecked());
                }
            });
        } else {
            writeInto.setVisibility(View.GONE);
        }

        findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onActionReplace(applyToAll.isChecked());
            }
        });

        findViewById(android.R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onActionSkip(applyToAll.isChecked());
            }
        });

        findViewById(R.id.abort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onActionAbort();
            }
        });
    }

}
