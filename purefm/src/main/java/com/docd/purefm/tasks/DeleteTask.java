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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandRemove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.ui.dialogs.MessageDialog;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PureFMTextUtils;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

/**
 * @author Doctoror
 *
 * Task for deleting files
 */
public final class DeleteTask extends
        AsyncTask<GenericFile, Void, List<GenericFile>> {

    private final WeakReference<Activity> activity;

    private ProgressDialog dialog;

    public DeleteTask(final Activity activity) {
        this.activity = new WeakReference<Activity>(activity);
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = this.activity.get();
        if (activity != null) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(activity.getString(R.string.progress_deleting_files));
            this.dialog.setCancelable(true);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(false);
                }
            });
            if (!activity.isFinishing()) {
                this.dialog.show();
            }
        }
    }

    @NotNull
    @Override
    protected List<GenericFile> doInBackground(GenericFile... files) {
        final List<GenericFile> failed = new ArrayList<GenericFile>();
        final List<File> filesAffected = new LinkedList<File>();
        
        if (files[0] instanceof CommandLineFile) {

            final Shell shell = ShellHolder.getShell();
            for (final GenericFile file : files) {
                final File fileFile = file.toFile();
                if (CommandLine.execute(shell, new CommandRemove(fileFile))) {
                    filesAffected.add(fileFile);
                } else {
                    failed.add(file);
                }
            }
        } else {
            for (final GenericFile file : files) {
                if (file.delete()) {
                    filesAffected.add(file.toFile());
                } else {
                    failed.add(file);
                }
            }
        }

        final Activity activity = this.activity.get();
        if (activity != null && !filesAffected.isEmpty()) {
            MediaStoreUtils.deleteFiles(activity.getApplicationContext(), filesAffected);
            PureFMFileUtils.requestMediaScanner(activity, filesAffected);
        }
        
        return failed;
    }

    @Override
    protected void onPostExecute(@NotNull final List<GenericFile> failed) {
        super.onPostExecute(failed);
        this.finish(failed);
    }

    @Override
    protected void onCancelled(@NotNull final List<GenericFile> failed) {
        super.onCancelled(failed);
        this.finish(failed);
    }

    private void finish(@NotNull final List<GenericFile> failed) {
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        final Activity activity = this.activity.get();
        if (activity != null && !failed.isEmpty()) {
            final Dialog dialog = MessageDialog.create(activity, R.string.dialog_delete_failed,
                    PureFMTextUtils.fileListToDashList(failed));
            if (!activity.isFinishing()) {
                dialog.show();
            }
        }
    }
}
