package com.docd.purefm.dialogs;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PureFMFileUtils;

public final class CreateFileDialog extends DialogFragment {
    
    public static DialogFragment instantiate(File currentDir) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_CURRENT_DIR, currentDir);
        
        final CreateFileDialog cd = new CreateFileDialog();
        cd.setArguments(extras);
        return cd;
    }

    private EditText filename;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.buildEditText();
        final File current = (File) this.getArguments().getSerializable(Extras.EXTRA_CURRENT_DIR);
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.dialog_new_file_title);
        b.setView(this.filename);
        b.setPositiveButton(R.string.create,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (filename.getText().length() == 0) {
                            Toast.makeText(getActivity(), R.string.name_empty,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final String name = filename.getText().toString();
                        if (!PureFMFileUtils.isValidFileName(name)) {
                            Toast.makeText(getActivity(),
                                    R.string.invalid_filename,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final GenericFile target = FileFactory.newFile(current, name);
                        if (target.exists()) {
                            Toast.makeText(getActivity(), R.string.file_exists,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        createFile(target);
                        dialog.dismiss();
                    }
                });
        b.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return b.create();
    }

    private void buildEditText() {
        this.filename = (EditText) LayoutInflater.from(
                getActivity()).inflate(R.layout.text_field_filename, null);
        this.filename.setHint(R.string.menu_new_file);
    }

    private void createFile(GenericFile target) {
        if (!target.createNewFile()) {
            Toast.makeText(getActivity(), R.string.could_not_create_file,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
