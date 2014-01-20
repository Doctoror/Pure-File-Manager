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

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.controller.FilePermissionsController;
import com.docd.purefm.controller.FilePropertiesController;
import com.docd.purefm.file.GenericFile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ViewAnimator;

import org.jetbrains.annotations.NotNull;

public final class FilePropertiesDialog extends DialogFragment {
    
    public static DialogFragment instantiate(GenericFile f) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, f);
        
        final FilePropertiesDialog fpd = new FilePropertiesDialog();
        fpd.setArguments(extras);
        return fpd;
    }
    
    private GenericFile file;
    private View mPermissionsView;
    
    @SuppressWarnings("unused")
    private FilePropertiesController mFilePropertiesController;
    private FilePermissionsController mFilePermissionsController;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        this.file = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setIcon(R.drawable.holo_light_action_info);
        builder.setTitle(file.getName());
        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFilePermissionsController.applyPermissions(getActivity());
                dialog.dismiss();
            }
        });
        final View content = this.getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_properties_container, null);
        this.initView(content);
        builder.setView(content);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mFilePermissionsController = new FilePermissionsController(mPermissionsView, this.file,
                ((AlertDialog) this.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE));
    }

    private void initView(@NotNull final View view) {
        final ViewAnimator flipper = (ViewAnimator) view.findViewById(R.id.tabsContainer);
        final CompoundButton tab1 = (CompoundButton) view.findViewById(R.id.tab1);
        final CompoundButton tab2 = (CompoundButton) view.findViewById(R.id.tab2);
        
        tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab1.setChecked(true);
                tab2.setChecked(false);
                flipper.setDisplayedChild(0);
            }
        });
        
        tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab2.setChecked(true);
                tab1.setChecked(false);
                flipper.setDisplayedChild(1);
            }
        });

        this.mPermissionsView = flipper.getChildAt(1);
        this.mFilePropertiesController = new FilePropertiesController(flipper.getChildAt(0), this.file);
    }
}
