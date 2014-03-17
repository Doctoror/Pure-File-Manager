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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public final class FileExistsDialog extends Dialog {
    
    public FileExistsDialog(
            @NotNull final Context context,
            @NotNull final String source,
            @NotNull final String target,
            @NotNull final View.OnClickListener abortAction,
            @NotNull final View.OnClickListener skipAction,
            @NotNull final View.OnClickListener skipAllAction,
            @NotNull final View.OnClickListener replaceAction,
            @NotNull final View.OnClickListener replaceAllAction) {
        super(context);
        
        this.setTitle(R.string.dialog_overwrite_title);
        this.setContentView(R.layout.dialog_exists);
        
        this.initView(source, target, abortAction, skipAction, skipAllAction, replaceAction, replaceAllAction);
    }
    
    private void initView(
            @NotNull final String sourcePath,
            @NotNull final String targetPath,
            @NotNull final View.OnClickListener abortAction,
            @NotNull final View.OnClickListener skipAction,
            @NotNull final View.OnClickListener skipAllAction,
            @NotNull final View.OnClickListener replaceAction,
            @NotNull final View.OnClickListener replaceAllAction) {
        
        final TextView source = (TextView) this.findViewById(android.R.id.text1);
        source.setText(sourcePath);
        
        final TextView target = (TextView) this.findViewById(android.R.id.text2);
        target.setText(targetPath);
        
        this.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                replaceAction.onClick(v);
            }
        });
        this.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                replaceAllAction.onClick(v);
            }
        });
        this.findViewById(android.R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                skipAction.onClick(v);
            }
        });
        this.findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                skipAllAction.onClick(v);
            }
        });
        this.findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                abortAction.onClick(v);
            }
        });
        this.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                skipAllAction.onClick(findViewById(R.id.button5));
            }
        });
    }

}
