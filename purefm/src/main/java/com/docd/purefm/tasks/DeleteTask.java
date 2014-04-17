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

import com.docd.purefm.R;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.ui.dialogs.MessageDialogBuilder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PFMTextUtils;

import android.support.annotation.NonNull;

/**
 * @author Doctoror
 *
 * Task for deleting files
 */
public final class DeleteTask extends
        OperationTask<GenericFile, ArrayList<GenericFile>> {

    public DeleteTask(@NonNull final MonitoredActivity activity) {
        super(activity);
    }

    @Override
    protected void startService(@NonNull GenericFile... genericFiles) {
        OperationsService.delete(mActivity, genericFiles);
    }

    @NonNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_DELETE;
    }

    @Override
    protected void cancel() {
        OperationsService.cancelDelete(mActivity);
    }

    @Override
    protected void onPostExecute(@NonNull final ArrayList<GenericFile> failed) {
        super.onPostExecute(failed);
        this.finish(failed);
    }

    @Override
    protected void onCancelled(@NonNull final ArrayList<GenericFile> failed) {
        super.onCancelled(failed);
        this.finish(failed);
    }

    private void finish(@NonNull final ArrayList<GenericFile> failed) {
        if (!failed.isEmpty()) {
            final Dialog dialog = MessageDialogBuilder.create(mActivity, R.string.dialog_delete_failed,
                    PFMTextUtils.fileListToDashList(failed));
            if (!mActivity.isFinishing()) {
                dialog.show();
            }
        }
    }
}
