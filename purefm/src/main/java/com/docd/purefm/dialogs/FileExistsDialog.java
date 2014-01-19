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
package com.docd.purefm.dialogs;

import com.docd.purefm.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

public final class FileExistsDialog extends Dialog {
    
    public FileExistsDialog(Context context, String source, String target,
            final View.OnClickListener abortAction,
            final View.OnClickListener skipAction,
            final View.OnClickListener skipAllAction,
            final View.OnClickListener replaceAction,
            final View.OnClickListener replaceAllAction) {
        super(context);
        
        this.setTitle(R.string.dialog_overwrite_title);
        this.setContentView(R.layout.dialog_exists);
        
        this.initView(context, source, target, abortAction, skipAction, skipAllAction, replaceAction, replaceAllAction);
    }
    
    private void initView(
            final Context context,
            final String sourcePath,
            final String targetPath,
            final View.OnClickListener abortAction,
            final View.OnClickListener skipAction,
            final View.OnClickListener skipAllAction,
            final View.OnClickListener replaceAction,
            final View.OnClickListener replaceAllAction) {
        
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
                skipAllAction.onClick(v);
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
