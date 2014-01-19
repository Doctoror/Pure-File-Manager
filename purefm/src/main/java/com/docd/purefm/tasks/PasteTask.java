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
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CopyCommand;
import com.docd.purefm.commandline.MoveCommand;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.dialogs.MessageDialog;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ArrayUtils;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.TextUtil;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

final class PasteTask extends AsyncTask<GenericFile, Void, List<GenericFile>> {

    private final WeakReference<Activity> activity;
    private final GenericFile targetPath;

    private ProgressDialog dialog;

    protected PasteTask(Activity activity, GenericFile targetPath) {
        this.activity = new WeakReference<Activity>(activity);
        this.targetPath = targetPath;
    }

    @NotNull
    private CharSequence getTitle() {
        final Activity activity = this.activity.get();
        final GenericFile[] files = ClipBoard.getClipBoardContents();
        if (activity != null && files != null) {
            final StringBuilder title = new StringBuilder();
            if (ClipBoard.isMove()) {
                title.append(activity.getString(R.string.progress_moving));
            } else {
                title.append(activity.getString(R.string.progress_copying));
            }
            title.append(files.length);
            title.append(activity.getString(R.string._files));
            return title.toString();
        }
        return "";
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = this.activity.get();
        if (activity != null) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(this.getTitle());
            this.dialog.setCancelable(true);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            if (!activity.isFinishing()) {
                this.dialog.show();
            }
        }
        ClipBoard.lock();
    }

    @Override
    protected List<GenericFile> doInBackground(GenericFile... contents) {
        final List<File> filesDeleted = new LinkedList<File>();
        final List<File> filesCreated = new LinkedList<File>();
        
        final boolean isMove = ClipBoard.isMove();
        final GenericFile target = this.targetPath;
        final List<GenericFile> failed;

        if (target instanceof JavaFile) {
            failed = processJavaFiles(contents, target, isMove, filesCreated, filesDeleted);
        } else {
            failed = processCommandLineFiles(contents, target, isMove, filesCreated, filesDeleted);
        }

        final Activity activity = this.activity.get();
        if (activity != null) {
            final Context context = activity.getApplicationContext();
            if (!filesDeleted.isEmpty()) {
                MediaStoreUtils.deleteFiles(context, filesDeleted);
            }
            if (!filesCreated.isEmpty()) {
                PureFMFileUtils.requestMediaScanner(context, filesCreated);
            }
        }

        return failed;
    }

    /**
     * Copies or moves the files and returns list of files that failed.
     *
     * @param contents files to process
     * @param target target directory to move to
     * @param isMove if true, means the files should be moved, otherwise they will be copied
     * @param filesCreated the List to fill with successfully created files
     * @param filesDeleted file List will be filled with successfully deleted files
     * @return List of files that were failed to process
     */
    @NotNull
    private List<GenericFile> processJavaFiles(GenericFile[] contents, GenericFile target, boolean isMove, List<File> filesCreated, List<File> filesDeleted) {
        final List<GenericFile> failed = new ArrayList<GenericFile>();
        for (final GenericFile current : contents) {
            if (this.isCancelled()) {
                return failed;
            }

            if (current != null && current.exists()) {
                
                if (isMove) {
                    if (current.move(target)) {
                        filesDeleted.add(current.toFile());
                        filesCreated.add(target.toFile());
                    } else {
                        failed.add(current);
                    }
                } else {
                    if (current.copy(target)) {
                        filesCreated.add(target.toFile());
                    } else {
                        failed.add(current);
                    }
                }
            }
        }
        return failed;
    }

    /**
     * Copies or moves the files and returns list of files that failed.
     *
     * @param contents files to process
     * @param target target directory to move to
     * @param isMove if true, means the files should be moved, otherwise they will be copied
     * @param filesCreated the List to fill with successfully created files
     * @param filesDeleted file List will be filled with successfully deleted files
     * @return List of files that were failed to process
     */
    @NotNull
    private List<GenericFile> processCommandLineFiles(GenericFile[] contents, GenericFile target, boolean isMove, List<File> filesCreated, List<File> filesDeleted) {
        final List<GenericFile> failed = new ArrayList<GenericFile>();
        final CommandLineFile[] cont = new CommandLineFile[contents.length];
        ArrayUtils.copyArrayAndCast(contents, cont);
        final CommandLineFile t = (CommandLineFile) target;
        final String targetPath = target.getAbsolutePath();
        final boolean wasRemounted;
        if (targetPath.startsWith(Environment.androidRootDirectory.getAbsolutePath())) {
            RootTools.remount(targetPath, "RW");
            wasRemounted = true;
        } else {
            wasRemounted = false;
        }

        final Shell shell = ShellHolder.getShell();
        for (final CommandLineFile current : cont) {
            final Command command = (isMove ? new MoveCommand(current, t) :
                    new CopyCommand(current, t));

            final boolean res = CommandLine.execute(shell, command);
            if (res) {
                if (isMove) {
                    filesDeleted.add(current.toFile());
                }
                filesCreated.add(current.toFile());
            } else {
                failed.add(current);
            }

        }
        if (wasRemounted) {
            RootTools.remount(targetPath, "RO");
        }
        return failed;
    }

    @Override
    protected void onPostExecute(final List<GenericFile> failed) {
        this.finish(failed);
    }

    @Override
    protected void onCancelled(final List<GenericFile> failed) {
        this.finish(failed);
    }

    private void finish(@NotNull final List<GenericFile> failed) {
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        final Activity activity = this.activity.get();
        if (activity != null) {
            if (!failed.isEmpty()) {
                final Dialog dialog = MessageDialog.create(activity, ClipBoard.isMove() ?
                        R.string.dialog_move_failed : R.string.dialog_copy_failed,
                        TextUtil.fileListToDashList(failed));
                if (!activity.isFinishing()) {
                    dialog.show();
                }
            }
            ClipBoard.unlock();
            ClipBoard.clear();
            activity.invalidateOptionsMenu();
        }
    }
}
