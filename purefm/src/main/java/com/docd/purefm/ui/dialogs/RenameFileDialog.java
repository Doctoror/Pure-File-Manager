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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.utils.PFMFileUtils;

public final class RenameFileDialog extends DialogFragment {
    
    /**
     * Instantiates the rename dialog
     * 
     * @param f File to rename
     * @param mode Current ActionMode instance
     * @return Dialog for renaming the file
     */
    public static RenameFileDialog instantiate(ActionMode mode, GenericFile f) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, f);

        final RenameFileDialog rd = new RenameFileDialog();
        rd.setArguments(extras);
        rd.mActionMode = mode;
        return rd;
    }
    
    private ActionMode mActionMode;
    private GenericFile mSource;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        if (args == null) {
            throw new RuntimeException("Arguments must be supplied!");
        }
        this.mSource = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_name);

        //noinspection InflateParams
        final EditText text = (EditText) LayoutInflater.from(
                getActivity()).inflate(R.layout.text_field_filename, null);
        text.setFilters(new InputFilter[] {
                new PFMFileUtils.FileNameFilter(),
                new InputFilter.LengthFilter(PFMFileUtils.FileNameFilter.MAX_FILENAME_LENGTH)});
        text.setText(mSource.getName());
        text.setHint(mSource.getName());
        builder.setView(text);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener()
        {            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                final Activity a = getActivity();
                if (a != null) {
                    CharSequence newNameField = text.getText();
                    String newName = newNameField.toString().trim();
                    if (newName.isEmpty()) {
                        newName = text.getHint().toString();
                    }
                    if (!newName.equals(mSource.getName())) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }

                        OperationsService.rename(a, mSource, newName);
                    }
                }
            }
        });
        return builder.create();
    }

}