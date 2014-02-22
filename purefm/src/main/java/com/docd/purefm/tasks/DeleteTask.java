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
package com.docd.purefm.tasks;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.docd.purefm.R;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.ui.dialogs.MessageDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PureFMTextUtils;

import org.jetbrains.annotations.NotNull;

/**
 * @author Doctoror
 *
 * Task for deleting files
 */
public final class DeleteTask extends
        OperationTask<GenericFile, ArrayList<GenericFile>> {

    private ProgressDialog mDialog;

    public DeleteTask(final MonitoredActivity activity) {
        super(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(mActivity);
        this.mDialog.setMessage(mActivity.getString(R.string.progress_deleting_files));
        this.mDialog.setCancelable(true); //TODO replace with cancel button
        this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel();
            }
        });
        if (!mActivity.isFinishing()) {
            this.mDialog.show();
        }
    }

    @Override
    protected void startService(@NotNull GenericFile... genericFiles) {
        OperationsService.delete(mActivity, genericFiles);
    }

    @NotNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_DELETE;
    }

    @Override
    protected void cancel() {
        OperationsService.cancelDelete(mActivity);
    }

    @Override
    public void onActivityStop(MonitoredActivity activity) {
        super.onActivityStop(activity);
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onPostExecute(@NotNull final ArrayList<GenericFile> failed) {
        super.onPostExecute(failed);
        this.finish(failed);
    }

    @Override
    protected void onCancelled(@NotNull final ArrayList<GenericFile> failed) {
        super.onCancelled(failed);
        this.finish(failed);
    }

    private void finish(@NotNull final ArrayList<GenericFile> failed) {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        if (!failed.isEmpty()) {
            final Dialog dialog = MessageDialog.create(mActivity, R.string.dialog_delete_failed,
                    PureFMTextUtils.fileListToDashList(failed));
            if (!mActivity.isFinishing()) {
                dialog.show();
            }
        }
    }
}
