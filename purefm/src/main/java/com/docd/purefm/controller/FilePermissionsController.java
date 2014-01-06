package com.docd.purefm.controller;

import com.cyanogenmod.filemanager.util.AIDHelper;
import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.Permissions;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public final class FilePermissionsController {
    
    private GenericFile file;
    
    private CompoundButton ur;
    private CompoundButton uw;
    private CompoundButton ux;
    private CompoundButton gr;
    private CompoundButton gw;
    private CompoundButton gx;
    private CompoundButton or;
    private CompoundButton ow;
    private CompoundButton ox;
    
    public FilePermissionsController(View table, GenericFile file) {
        this.file = file;
        final TextView owner = (TextView) table.findViewById(R.id.owner);
        final TextView group = (TextView) table.findViewById(R.id.group);
        
        final Permissions p = file.getPermissions();
        this.ur = (CompoundButton) table.findViewById(R.id.uread);
        this.uw = (CompoundButton) table.findViewById(R.id.uwrite);
        this.ux = (CompoundButton) table.findViewById(R.id.uexecute);
        this.gr = (CompoundButton) table.findViewById(R.id.gread);
        this.gw = (CompoundButton) table.findViewById(R.id.gwrite);
        this.gx = (CompoundButton) table.findViewById(R.id.gexecute);
        this.or = (CompoundButton) table.findViewById(R.id.oread);
        this.ow = (CompoundButton) table.findViewById(R.id.owrite);
        this.ox = (CompoundButton) table.findViewById(R.id.oexecute);
        this.ur.setChecked(p.ur);
        this.uw.setChecked(p.uw);
        this.ux.setChecked(p.ux);
        this.gr.setChecked(p.gr);
        this.gw.setChecked(p.gw);
        this.gx.setChecked(p.gx);
        this.or.setChecked(p.or);
        this.ow.setChecked(p.ow);
        this.ox.setChecked(p.ox);

        if (file instanceof CommandLineFile) {
            final CommandLineFile f = (CommandLineFile) file;
            String name = AIDHelper.getAIDs(table.getContext(), false).get(f.getOwner());
            owner.setText(name);
            
            name = AIDHelper.getAIDs(table.getContext(), false).get(f.getGroup());
            group.setText(name);
            
            if (CommandLineUtils.isMSDOSFS(f.toFile())) {
                this.disableBoxes();
            }
        } else {
            this.disableBoxes();
        }
    }
    
    private void disableBoxes() {
        this.ur.setEnabled(false);
        this.uw.setEnabled(false);
        this.ux.setEnabled(false);
        this.gr.setEnabled(false);
        this.gw.setEnabled(false);
        this.gx.setEnabled(false);
        this.or.setEnabled(false);
        this.ow.setEnabled(false);
        this.ox.setEnabled(false);
    }
    
    public void applyPermissions(final Context context) {
        if (this.ur.isEnabled() && this.file instanceof CommandLineFile) {
            final Permissions target = new Permissions(
                    this.ur.isChecked(), this.uw.isChecked(), this.ux.isChecked(),
                    this.gr.isChecked(), this.gw.isChecked(), this.gx.isChecked(),
                    this.or.isChecked(), this.ow.isChecked(), this.ox.isChecked());
            
            if (target.equals(file.getPermissions())) {
                return;
            }
            final ApplyTask task = new ApplyTask(context, target);
            task.execute((CommandLineFile)this.file);
        }
    }
    
    private static final class ApplyTask extends AsyncTask<CommandLineFile, Void, Boolean> {

        private final Context context;
        private final Permissions target;
        
        private ApplyTask(Context context, Permissions target) {
            this.context = context;
            this.target = target;
        }
        
        @Override
        protected Boolean doInBackground(CommandLineFile... params) {
            return params[0].applyPermissions(this.target);
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(this.context, this.context.getString(result ?
                    R.string.permissions_changed : R.string.applying_failed), Toast.LENGTH_SHORT).show();
        }

    }
    
}
