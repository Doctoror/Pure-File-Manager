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

import android.app.Activity;
import android.app.Dialog;

import com.docd.purefm.R;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.dialogs.MessageDialogBuilder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PFMTextUtils;

import android.support.annotation.NonNull;


final class PasteTask extends OperationTask<GenericFile, ArrayList<GenericFile>> {

    @NonNull
    private final GenericFile mTarget;

    protected PasteTask(@NonNull final Activity activity,
                        @NonNull final GenericFile target) {
        super(activity);
        this.mTarget = target;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ClipBoard.lock();
    }

    @NonNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_PASTE;
    }

    @Override
    protected void startService(@NonNull final GenericFile... files) {
        OperationsService.paste(mActivity, mTarget, files, ClipBoard.isMove());
    }

    @Override
    protected void cancel() {
        OperationsService.cancelPaste(mActivity);
    }

    @Override
    protected void onPostExecute(final ArrayList<GenericFile> failed) {
        this.finish(failed);
    }

    @Override
    protected void onCancelled(final ArrayList<GenericFile> failed) {
        this.finish(failed);
    }

    private void finish(@NonNull final ArrayList<GenericFile> failed) {
        if (!failed.isEmpty()) {
            final Dialog dialog = MessageDialogBuilder.create(mActivity, ClipBoard.isMove() ?
                            R.string.dialog_move_failed : R.string.dialog_copy_failed,
                    PFMTextUtils.fileListToDashList(failed)
            );
            if (!mActivity.isFinishing()) {
                dialog.show();
            }
        }
        ClipBoard.unlock();
        ClipBoard.clear();
        mActivity.invalidateOptionsMenu();
    }
}
