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

import java.io.File;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PureFMFileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public final class CreateDirectoryDialog extends DialogFragment {
    
    public static DialogFragment instantiate(final File currentDir) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_CURRENT_DIR, currentDir);
        
        final CreateDirectoryDialog cd = new CreateDirectoryDialog();
        cd.setArguments(extras);
        return cd;
    }
    
    private EditText filename;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = this.getActivity();
        this.filename = (EditText) activity.getLayoutInflater().inflate(R.layout.text_field_filename, null);
        this.filename.setHint(R.string.menu_new_folder);
        final File current = (File) this.getArguments().getSerializable(Extras.EXTRA_CURRENT_DIR);
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.dialog_new_folder_title);
        b.setView(this.filename);
        b.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CharSequence text = filename.getText();
                if (text == null || text.length() == 0) {
                    Toast.makeText(getActivity(), R.string.name_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String name = text.toString();
                if (!PureFMFileUtils.isValidFileName(name)) {
                    Toast.makeText(getActivity(), R.string.invalid_filename, Toast.LENGTH_SHORT).show();
                    return;
                }
                final GenericFile target = FileFactory.newFile(current, name);
                if (target.exists()) {
                    Toast.makeText(getActivity(), R.string.file_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
                createDirectory(target);
                dialog.dismiss();
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return b.create();
    }
    
    private void createDirectory(GenericFile target) {
        if (!target.mkdir()) {
            Toast.makeText(getActivity(), R.string.could_not_create_dir, Toast.LENGTH_SHORT).show();
        }
    }
}
