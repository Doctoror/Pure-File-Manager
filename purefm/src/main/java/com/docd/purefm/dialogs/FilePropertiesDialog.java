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
import android.widget.ViewFlipper;

public final class FilePropertiesDialog extends DialogFragment {
    
    public static final DialogFragment instantiate(GenericFile f) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, f);
        
        final FilePropertiesDialog fpd = new FilePropertiesDialog();
        fpd.setArguments(extras);
        return fpd;
    }
    
    private GenericFile file;
    
    @SuppressWarnings("unused")
    private FilePropertiesController fpc;
    private FilePermissionsController fpermc;
    
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
        builder.setIcon(R.drawable.action_info);
        builder.setTitle(file.getName());
        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fpermc.applyPermissions(getActivity());
                dialog.dismiss();
            }
        });
        final View content = this.getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_properties_container, null);
        this.initView(content);
        builder.setView(content);
        return builder.create();
    }
    
    private void initView(View view) {
        final ViewFlipper flipper = (ViewFlipper) view.findViewById(R.id.flipper);
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
        
        this.fpc = new FilePropertiesController(flipper.getChildAt(0), this.file);
        this.fpermc = new FilePermissionsController(flipper.getChildAt(1), this.file);
    }
}
