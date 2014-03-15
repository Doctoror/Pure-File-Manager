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
import java.util.Locale;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.docd.purefm.R;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.ui.dialogs.MessageDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.ui.dialogs.ProgressAlertDialogBuilder;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PureFMTextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


final class PasteTask extends OperationTask<GenericFile, ArrayList<GenericFile>> {

    private final GenericFile mTarget;

    private Dialog mDialog;

    protected PasteTask(MonitoredActivity activity, GenericFile target) {
        super(activity);
        this.mTarget = target;
    }

    @Nullable
    private CharSequence getProgressDialogMessage() {
        final GenericFile[] files = ClipBoard.getClipBoardContents();
        if (files != null) {
            final Resources res = mActivity.getResources();
            if (ClipBoard.isMove()) {
                return String.format(Locale.getDefault(), res.getQuantityString(
                        R.plurals.progress_moving_n_files, files.length), files.length);
            }
            return String.format(Locale.getDefault(), res.getQuantityString(
                    R.plurals.progress_copying_n_files, files.length), files.length);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = ProgressAlertDialogBuilder.create(mActivity, getProgressDialogMessage(),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                cancel();
            }
        });
        if (!mActivity.isFinishing()) {
            mDialog.show();
        }
        ClipBoard.lock();
    }

    @NotNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_PASTE;
    }

    @Override
    protected void startService(@NotNull final GenericFile... files) {
        OperationsService.paste(mActivity, mTarget, files, ClipBoard.isMove());
    }

    @Override
    protected void cancel() {
        OperationsService.cancelPaste(mActivity);
    }

    @Override
    public void onActivityStop(MonitoredActivity activity) {
        super.onActivityStop(activity);
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onPostExecute(final ArrayList<GenericFile> failed) {
        this.finish(failed);
    }

    @Override
    protected void onCancelled(final ArrayList<GenericFile> failed) {
        this.finish(failed);
    }

    private void finish(@NotNull final ArrayList<GenericFile> failed) {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        if (!failed.isEmpty()) {
            final Dialog dialog = MessageDialog.create(mActivity, ClipBoard.isMove() ?
                    R.string.dialog_move_failed : R.string.dialog_copy_failed,
                    PureFMTextUtils.fileListToDashList(failed));
            if (!mActivity.isFinishing()) {
                dialog.show();
            }
        }
        ClipBoard.unlock();
        ClipBoard.clear();
        mActivity.invalidateOptionsMenu();
    }
}
