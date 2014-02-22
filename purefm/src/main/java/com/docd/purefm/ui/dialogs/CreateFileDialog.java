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

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.tasks.CreateFileTask;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.utils.PureFMFileUtils;

public final class CreateFileDialog extends DialogFragment {
    
    public static DialogFragment instantiate(File currentDir) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_CURRENT_DIR, currentDir);
        
        final CreateFileDialog cd = new CreateFileDialog();
        cd.setArguments(extras);
        return cd;
    }

    private EditText mFileNameInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MonitoredActivity activity = (MonitoredActivity) getActivity();
        this.buildEditText(activity);
        final File current = (File) this.getArguments().getSerializable(Extras.EXTRA_CURRENT_DIR);
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(R.string.dialog_new_file_title);
        b.setView(this.mFileNameInput);
        b.setPositiveButton(R.string.create,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = mFileNameInput.getText().toString().trim();
                        if (newName.isEmpty()) {
                            newName = mFileNameInput.getHint().toString();
                        }
                        final GenericFile target = FileFactory.newFile(current, newName);
                        if (target.exists()) {
                            Toast.makeText(activity, R.string.file_exists,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new CreateFileTask(activity).execute(target);
                    }
                });
        b.setNegativeButton(R.string.cancel, null);
        return b.create();
    }

    private void buildEditText(final Context context) {
        mFileNameInput = (EditText) LayoutInflater.from(
                context).inflate(R.layout.text_field_filename, null);
        mFileNameInput.setHint(R.string.menu_new_file);
        mFileNameInput.setFilters(new InputFilter[] {
                new PureFMFileUtils.FileNameFilter(),
                new InputFilter.LengthFilter(PureFMFileUtils.FileNameFilter.MAX_FILENAME_LENGTH)});
    }
}
