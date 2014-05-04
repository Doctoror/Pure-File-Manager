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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.ui.dialogs.FileExistsDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PFMFileUtils;
import com.docd.purefm.utils.StatFsCompat;
import com.stericson.RootTools.RootTools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.Toast;

public final class PasteTaskExecutor implements FileExistsDialog.FileExistsDialogListener {

    @NonNull
    private final WeakReference<Activity> mActivityReference;

    @NonNull
    private final Settings mSettings;

    @NonNull
    private final GenericFile mTargetFile;

    @NonNull
    private final List<GenericFile> mToProcess = new LinkedList<>();

    @NonNull
    private final Map<GenericFile, GenericFile> mExisting = new HashMap<>();

    @NonNull
    private final Map<GenericFile, GenericFile> mWriteInto = new HashMap<>();

    @NonNull
    private final List<GenericFile> mProcessedWriteInto = new LinkedList<>();

    private FreeSpaceTask mFreeSpaceTask;

    @SuppressWarnings("FieldCanBeLocal")
    private StartThread mStartThread;

    @SuppressWarnings("FieldCanBeLocal")
    private WriteIntoTask mWriteIntoTask;

    @SuppressWarnings("FieldCanBeLocal")
    private DeleteProcessedWriteIntoThread mDeleteProcessedWriteIntoThread;

    private boolean mWriteIntoAll;

    private Pair<GenericFile, GenericFile> mCurrentPair;
    
    public PasteTaskExecutor(@NonNull final Activity activity,
                             @NonNull final GenericFile targetFile) {
        mActivityReference = new WeakReference<>(activity);
        mSettings = Settings.getInstance(activity);
        mTargetFile = targetFile;
    }
    
    public void start() {
        if (mFreeSpaceTask == null) {
            mFreeSpaceTask = new FreeSpaceTask(mTargetFile, mToProcess);
            mFreeSpaceTask.execute();
        }
    }

    @Override
    public void onActionSkip(final boolean all) {
        if (all) {
            mExisting.clear();
        }
        next();
    }

    @Override
    public void onActionReplace(final boolean all) {
        if (mCurrentPair.second.delete()) {
            mToProcess.add(mCurrentPair.first);
        } else {
            final Context context = mActivityReference.get();
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.dialog_overwrite_replace_failed,
                        mCurrentPair.second.getAbsolutePath()), Toast.LENGTH_LONG).show();
            }
        }
        if (all) {
            //TODO check if it really works
            mToProcess.addAll(mExisting.keySet());
            mExisting.clear();
        }
        next();
    }

    @Override
    public void onActionWriteInto(final boolean all) {
        if (all) {
            mWriteIntoAll = true;
            for (final GenericFile file : mExisting.keySet()) {
                if (file.isDirectory()) {
                    mWriteInto.put(file, mExisting.get(file));
                }
            }
            for (final GenericFile file : mWriteInto.keySet()) {
                mExisting.remove(file);
            }
        }
        mWriteInto.put(mCurrentPair.first, mCurrentPair.second);
        mExisting.remove(mCurrentPair.first);
        next();
    }

    @Override
    public void onActionAbort() {
        mExisting.clear();
        mToProcess.clear();
        mProcessedWriteInto.clear();
    }

    private void next() {
        final Activity activity = this.mActivityReference.get();
        if (activity != null) {
            if (!mExisting.isEmpty()) {
                processNext(activity);
                return;
            }

            if (!mToProcess.isEmpty()) {
                startPasteOperation(activity);
                return;
            }

            if (!mWriteInto.isEmpty()) {
                processWriteInto();
                return;
            }

            ClipBoard.clear();
            deleteProcessedWriteInto();
        }
    }

    private void processNext(@NonNull final Activity activity) {
        final GenericFile key = mExisting.keySet().iterator().next();
        final GenericFile target = mExisting.get(key);
        mCurrentPair = new Pair<>(key, target);
        mExisting.remove(key);

        if (!activity.isFinishing()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                showExistsDialog(activity, key, target);
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showExistsDialog(activity, key, target);
                    }
                });
            }
        }
    }

    private void startPasteOperation(@NonNull final Context context) {
        final GenericFile[] files = new GenericFile[mToProcess.size()];
        mToProcess.toArray(files);
        OperationsService.paste(context, mTargetFile, files, ClipBoard.isMove());
    }

    private void processWriteInto() {
        mWriteIntoTask = new WriteIntoTask();
        mWriteIntoTask.execute();
    }

    private void deleteProcessedWriteInto() {
        mDeleteProcessedWriteIntoThread = new DeleteProcessedWriteIntoThread();
        mDeleteProcessedWriteIntoThread.start();
    }

    private void showExistsDialog(@NonNull final Context context,
                                  @NonNull final GenericFile source,
                                  @NonNull final GenericFile target) {

        new FileExistsDialog(context, source, target, this).show();
    }

    private final class DeleteProcessedWriteIntoThread extends Thread {

        @Override
        public void run() {
            final HashSet<String> remountPaths = new HashSet<>();
            // find paths to remount as read-write
            if (mSettings.useCommandLine() && mSettings.isSuEnabled()) {
                for (GenericFile file : mProcessedWriteInto) {
                    try {
                        file = file.getCanonicalFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String parent = file.getParent();
                    if (parent != null) {
                        if (Environment.needsRemount(parent)) {
                            remountPaths.add(parent);
                        }
                    } else if (Environment.needsRemount(file.getAbsolutePath())) {
                        remountPaths.add(file.getAbsolutePath());
                    }
                }
            }
            for (final String remountPath : remountPaths) {
                RootTools.remount(remountPath, "RW");
            }
            final LinkedList<GenericFile> filesDeleted = new LinkedList<>();
            try {
                for (final GenericFile file : mProcessedWriteInto) {
                    if (file.delete()) {
                        filesDeleted.add(file);
                    }
                }
            } finally {
                for (final String remountPath : remountPaths) {
                    RootTools.remount(remountPath, "RO");
                }
                if (!filesDeleted.isEmpty()) {
                    final Context context = mActivityReference.get();
                    if (context != null) {
                        MediaStoreUtils.deleteFilesOrDirectories(context.getContentResolver(), filesDeleted);
                        FileObserverNotifier.notifyDeleted(filesDeleted);
                    }
                }
                mDeleteProcessedWriteIntoThread = null;
            }
        }
    }

    private final class WriteIntoTask extends AsyncTask<Void, Void, Void> {

        WriteIntoTask() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            mExisting.clear();
            mToProcess.clear();
            final HashMap<GenericFile, GenericFile> mNewWriteInto = new HashMap<>();
            for (final GenericFile file : mWriteInto.keySet()) {
                final GenericFile target = mWriteInto.get(file);
                final GenericFile[] files = file.listFiles();
                if (files != null) {
                    for (final GenericFile childFile : files) {
                        final GenericFile testTarget = FileFactory.newFile(mSettings,
                                target.toFile(), childFile.getName());
                        if (testTarget.exists()) {
                            if (mWriteIntoAll && file.isDirectory()) {
                                mNewWriteInto.put(file, testTarget);
                            } else {
                                mExisting.put(file, testTarget);
                            }
                        } else {
                            mToProcess.add(file);
                        }
                    }
                }
                mProcessedWriteInto.add(file);
            }
            mWriteInto.clear();
            mWriteInto.putAll(mNewWriteInto);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mWriteIntoTask = null;
            next();
        }
    }

    private static final class FreeSpaceTaskResult {

        final long mFreeSpace;
        final long mFilesSize;
        final boolean mHasEnoughFreeSpace;

        FreeSpaceTaskResult(final long freeSpace,
                            final long filesSize) {
            this.mFreeSpace = freeSpace;
            this.mFilesSize = filesSize;
            mHasEnoughFreeSpace = freeSpace > mFilesSize;
        }
    }

    private final class FreeSpaceTask extends AsyncTask<Void, Void, FreeSpaceTaskResult> {

        private final GenericFile mTarget;
        private final Iterable<GenericFile> mFiles;

        FreeSpaceTask(@NonNull final GenericFile target,
                             @NonNull final Iterable<GenericFile> files) {
            this.mTarget = target;
            this.mFiles = files;
        }

        @NonNull
        @Override
        protected FreeSpaceTaskResult doInBackground(Void... params) {
            long total = 0;
            for (final GenericFile file : mFiles) {
                total += file.length();
            }
            final StatFsCompat statFs = new StatFsCompat(PFMFileUtils.fullPath(mTarget));
            return new FreeSpaceTaskResult(statFs.getAvailableBytes(), total);
        }

        @Override
        protected void onPostExecute(@NonNull final FreeSpaceTaskResult freeSpaceTaskResult) {
            mFreeSpaceTask = null;
            if (freeSpaceTaskResult.mHasEnoughFreeSpace) {
                mStartThread = new StartThread();
                mStartThread.start();
            } else {
                final Context context = mActivityReference.get();
                if (context != null) {
                    final String freeSpace = PFMFileUtils.byteCountToDisplaySize(
                            BigInteger.valueOf(freeSpaceTaskResult.mFreeSpace));
                    final String filesSize = PFMFileUtils.byteCountToDisplaySize(
                            BigInteger.valueOf(freeSpaceTaskResult.mFilesSize));
                    Toast.makeText(context, context.getString(R.string.not_enough_space_message,
                            freeSpace, filesSize), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private final class StartThread extends Thread {

        @Override
        public void run() {
            final GenericFile[] contents = ClipBoard.getClipBoardContents();
            if (contents == null) {
                return;
            }

            for (final GenericFile file : contents) {
                if (file != null && file.exists()) {
                    final GenericFile testTarget = FileFactory.newFile(mSettings,
                            mTargetFile.toFile(), file.getName());
                    if (testTarget.exists()) {
                        if (mWriteIntoAll && file.isDirectory()) {
                            mWriteInto.put(file, testTarget);
                        } else {
                            mExisting.put(file, testTarget);
                        }
                    } else {
                        mToProcess.add(file);
                    }
                }
            }

            mStartThread = null;
            next();
        }
    }
}
