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

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.tasks.CreateDirectoryTask;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.utils.PureFMFileUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;

public final class CreateDirectoryDialog extends DialogFragment {
    
    public static DialogFragment instantiate(final File currentDir) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_CURRENT_DIR, currentDir);
        
        final CreateDirectoryDialog cd = new CreateDirectoryDialog();
        cd.setArguments(extras);
        return cd;
    }
    
    private EditText mFileNameInput;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MonitoredActivity activity = (MonitoredActivity) this.getActivity();
        mFileNameInput = (EditText) activity.getLayoutInflater().inflate(
                R.layout.text_field_filename, null);
        mFileNameInput.setHint(R.string.menu_new_folder);
        mFileNameInput.setFilters(new InputFilter[] {
                new PureFMFileUtils.FileNameFilter(),
                new InputFilter.LengthFilter(PureFMFileUtils.FileNameFilter.MAX_FILENAME_LENGTH)});
        final File current = (File) this.getArguments().getSerializable(Extras.EXTRA_CURRENT_DIR);
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(R.string.dialog_new_folder_title);
        b.setView(this.mFileNameInput);
        b.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = mFileNameInput.getText().toString().trim();
                if (newName.isEmpty()) {
                    newName = mFileNameInput.getHint().toString();
                }
                new CreateDirectoryTask(activity, current).execute(newName);
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        return b.create();
    }
}
