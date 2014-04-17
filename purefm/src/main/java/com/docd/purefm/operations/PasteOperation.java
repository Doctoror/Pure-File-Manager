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
package com.docd.purefm.operations;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.docd.purefm.Environment;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandCopyRecursively;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandMove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ArrayUtils;
import com.docd.purefm.utils.MediaStoreUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs paste operation
 */
final class PasteOperation extends Operation<GenericFile, ArrayList<GenericFile>> {

    @NonNull
    private final Context mContext;

    @NonNull
    private final GenericFile mTarget;

    private final boolean mIsMove;

    PasteOperation(@NonNull final Context context,
                   @NonNull final GenericFile target,
                   final boolean isMove) {
        mContext = context;
        mTarget = target;
        mIsMove = isMove;
    }

    @Override
    protected ArrayList<GenericFile> doInBackground(GenericFile... files) {
        final LinkedList<Pair<GenericFile, GenericFile>> filesAffected =
                new LinkedList<>();

        final ArrayList<GenericFile> failed;

        if (mTarget instanceof JavaFile) {
            failed = processJavaFiles(files, mTarget, mIsMove, filesAffected);
        } else {
            failed = processCommandLineFiles(files, mTarget, mIsMove, filesAffected);
        }

        if (!filesAffected.isEmpty()) {
            if (mIsMove) {
                MediaStoreUtils.moveFiles(mContext, filesAffected);
                for (Pair<GenericFile, GenericFile> filesPair : filesAffected) {
                    FileObserverNotifier.notifyDeleted(filesPair.first);
                    FileObserverNotifier.notifyCreated(filesPair.second);
                }
            } else {
                MediaStoreUtils.copyFiles(mContext, filesAffected);
                for (Pair<GenericFile, GenericFile> filesPair : filesAffected) {
                    FileObserverNotifier.notifyCreated(filesPair.second);
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
     * @param filesAffected Pair of removed and created files
     * @return List of files that were failed to process
     */
    @NonNull
    private ArrayList<GenericFile> processJavaFiles(
            @NonNull final GenericFile[] contents,
            @NonNull final GenericFile target,
            final boolean isMove,
            @NonNull final List<Pair<GenericFile, GenericFile>> filesAffected) {
        final ArrayList<GenericFile> failed = new ArrayList<>();
        for (final GenericFile current : contents) {
            if (isCanceled()) {
                return failed;
            }

            if (current != null && current.exists()) {

                if (isMove) {
                    if (current.move(target)) {
                        filesAffected.add(new Pair<>(current,
                                FileFactory.newFile(target.toFile(), current.getName())));
                    } else {
                        failed.add(current);
                    }
                } else {
                    if (current.copy(target)) {
                        filesAffected.add(new Pair<>(current,
                                FileFactory.newFile(target.toFile(), current.getName())));
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
     * @param filesAffected Pair of removed and created files
     * @return List of files that were failed to process
     */
    @NonNull
    private static ArrayList<GenericFile> processCommandLineFiles(
            @NonNull final GenericFile[] contents,
            @NonNull final GenericFile target,
            final boolean isMove,
            @NonNull final List<Pair<GenericFile, GenericFile>> filesAffected) {
        final ArrayList<GenericFile> failed = new ArrayList<>();
        final Shell shell = ShellHolder.getShell();
        if (shell == null) {
            Log.w("PasteOperation", "shell is null, aborting");
            failed.addAll(Arrays.asList(contents));
            return failed;
        }

        final CommandLineFile[] cont = new CommandLineFile[contents.length];
        ArrayUtils.copyArrayAndCast(contents, cont);
        final CommandLineFile t = (CommandLineFile) target;
        final String targetPath = target.getAbsolutePath();
        final boolean wasRemounted;
        if (Environment.needsRemount(targetPath)) {
            RootTools.remount(targetPath, "RW");
            wasRemounted = true;
        } else {
            wasRemounted = false;
        }

        for (final CommandLineFile current : cont) {
            final Command command = (isMove ? new CommandMove(current, t) :
                    new CommandCopyRecursively(current, t));

            final boolean res = CommandLine.execute(shell, command);
            if (res) {
                filesAffected.add(new Pair<GenericFile, GenericFile>(current,
                        FileFactory.newFile(t.toFile(), current.getName())));
            } else {
                failed.add(current);
            }

        }
        if (wasRemounted) {
            RootTools.remount(targetPath, "RO");
        }
        return failed;
    }
}
