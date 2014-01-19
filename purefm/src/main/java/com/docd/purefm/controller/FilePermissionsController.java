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
package com.docd.purefm.controller;

import com.cyanogenmod.filemanager.util.AIDHelper;
import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.Permissions;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public final class FilePermissionsController implements CompoundButton.OnCheckedChangeListener {

    /**
     * Permissions that file had when FilePermissionsController was created
     */
    private final Permissions mInputPermissions;

    /**
     * Currently modified permissions
     */
    private Permissions mModifiedPermissions;

    /**
     * Target file
     */
    private final GenericFile mFile;

    /**
     * Dialog "Apply" button
     */
    private final View mApplyButton;

    /**
     * User: read
     */
    private CompoundButton ur;

    /**
     * User: write
     */
    private CompoundButton uw;

    /**
     * User: execute
     */
    private CompoundButton ux;

    /**
     * Group: read
     */
    private CompoundButton gr;

    /**
     * Group: write
     */
    private CompoundButton gw;

    /**
     * Group: execute
     */
    private CompoundButton gx;

    /**
     * Others: read
     */
    private CompoundButton or;

    /**
     * Others: write
     */
    private CompoundButton ow;

    /**
     * Others: execute
     */
    private CompoundButton ox;
    
    public FilePermissionsController(View table, GenericFile file, final View applyButton) {
        this.mFile = file;
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

        this.ur.setOnCheckedChangeListener(this);
        this.uw.setOnCheckedChangeListener(this);
        this.ux.setOnCheckedChangeListener(this);
        this.gr.setOnCheckedChangeListener(this);
        this.gw.setOnCheckedChangeListener(this);
        this.gx.setOnCheckedChangeListener(this);
        this.or.setOnCheckedChangeListener(this);
        this.ow.setOnCheckedChangeListener(this);
        this.ox.setOnCheckedChangeListener(this);

        if (file instanceof CommandLineFile) {
            final CommandLineFile f = (CommandLineFile) file;
            final SparseArray<String> aids = AIDHelper.getAIDs(table.getContext(), false);
            owner.setText(aids.get(f.getOwner()));
            group.setText(aids.get(f.getGroup()));
            
            if (CommandLineUtils.isMSDOSFS(f.toFile())) {
                this.disableBoxes();
            }
        } else {
            this.disableBoxes();
        }

        applyButton.setVisibility(View.GONE);
        this.mInputPermissions = p;
        this.mModifiedPermissions = p;
        this.mApplyButton = applyButton;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.mModifiedPermissions = new Permissions(
                this.ur.isChecked(), this.uw.isChecked(), this.ux.isChecked(),
                this.gr.isChecked(), this.gw.isChecked(), this.gx.isChecked(),
                this.or.isChecked(), this.ow.isChecked(), this.ox.isChecked());
        mApplyButton.setVisibility(mInputPermissions.equals(mModifiedPermissions) ?
                View.GONE : View.VISIBLE);
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
        if (!mInputPermissions.equals(mModifiedPermissions) && mFile instanceof CommandLineFile) {
            final ApplyTask task = new ApplyTask(context, mModifiedPermissions);
            task.execute((CommandLineFile)this.mFile);
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
