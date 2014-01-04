package com.docd.purefm.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;

public final class RenameFileDialog extends DialogFragment {
    
    /**
     * Instantiates the rename dialog
     * 
     * @param f File to rename
     * @param mode Current ActionMode instance
     * @return Dialog for renaming the file
     */
    public static DialogFragment instantiate(ActionMode mode, GenericFile f) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, f);

        final RenameFileDialog rd = new RenameFileDialog();
        rd.setArguments(extras);
        rd.mode = mode;
        return rd;
    }
    
    private ActionMode mode;
    
    private GenericFile file;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        if (args == null) {
            throw new RuntimeException("Arguments must be supplied!");
        }
        this.file = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_name);
        final EditText text = (EditText) LayoutInflater.from(
                getActivity()).inflate(R.layout.text_field_filename, null);
        text.setText(file.getName());
        text.setFilters(PureFMFileUtils.FILENAME_FILTERS);
        builder.setView(text);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener()
        {            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                final Activity a = getActivity();
                final CharSequence newNameField = text.getText();
                final String newName;
                if (newNameField != null && PureFMFileUtils.isValidFileName(
                        newName = newNameField.toString())) {

                    final GenericFile fileDir = file.getParentFile();
                    final File target = new File(fileDir.toFile(), newName);
                    final File source = file.toFile();
                    if (file.renameTo(target)) {
                        final List<File> filesCreated = new ArrayList<File>(1);
                        final List<File> filesDeleted = new ArrayList<File>(1);
                        filesDeleted.add(source);
                        filesCreated.add(target);
                        MediaStoreUtils.deleteFiles(a.getApplicationContext(), filesDeleted);
                        PureFMFileUtils.requestMediaScanner(a, filesCreated);
                    }
                    if (mode != null) {
                        mode.finish();
                    }
                    dialog.dismiss();
                } else {
                   Toast.makeText(a, R.string.invalid_filename, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return builder.create();
    }
}