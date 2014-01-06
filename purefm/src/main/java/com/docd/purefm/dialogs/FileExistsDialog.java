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
