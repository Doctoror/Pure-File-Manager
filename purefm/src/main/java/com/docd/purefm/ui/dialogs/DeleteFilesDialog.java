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

import java.util.Arrays;
import java.util.List;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.utils.PFMTextUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.view.ActionMode;
import android.widget.TextView;

public final class DeleteFilesDialog extends DialogFragment {
    
    public static DeleteFilesDialog newInstance(@NonNull final ActionMode mode,
                                                @NonNull final List<GenericFile> files) {
        final GenericFile[] extraFiles = new GenericFile[files.size()];
        files.toArray(extraFiles);
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, extraFiles);
        
        final DeleteFilesDialog dialog = new DeleteFilesDialog();
        dialog.setArguments(extras);
        dialog.mode = mode;
        
        return dialog;
    }
    
    private ActionMode mode;
    private GenericFile[] files;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle extras = this.getArguments();
        if (extras == null) {
            throw new IllegalArgumentException("Arguments not supplied");
        }
        final Object[] o = (Object[]) extras.getSerializable(Extras.EXTRA_FILE);
        this.files = new GenericFile[o.length];
        for (int i = 0; i < o.length; i++) {
            this.files[i] = (GenericFile) o[i];
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle state) {
        final Activity a = getActivity();
        if (a == null || a.isFinishing()) {
            return null;
        }
        final AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(R.string.dialog_delete_title);
        
        final TextView content = (TextView) a.getLayoutInflater()
                .inflate(R.layout.dialog_delete, null);
        content.setMovementMethod(new ScrollingMovementMethod());
        content.setText(PFMTextUtils.fileListToDashList(Arrays.asList(this.files)));
        b.setView(content);
        
        b.setPositiveButton(R.string.menu_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mode != null) {
                    mode.finish();
                }
                OperationsService.delete(a, files);
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        return b.create();
    }
}
