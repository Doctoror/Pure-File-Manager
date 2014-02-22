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

import com.docd.purefm.Environment;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandCopyRecursively;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandMove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ArrayUtils;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs paste operation
 */
final class PasteOperation extends Operation<GenericFile, ArrayList<GenericFile>> {

    private final Context mContext;
    private final GenericFile mTarget;
    private final boolean mIsMove;

    public PasteOperation(@NotNull final Context context,
                          @NotNull final GenericFile target,
                          final boolean isMove) {
        mContext = context;
        mTarget = target;
        mIsMove = isMove;
    }

    @Override
    protected ArrayList<GenericFile> execute(GenericFile... files) {
        final List<File> filesDeleted = new LinkedList<File>();
        final List<File> filesCreated = new LinkedList<File>();

        final ArrayList<GenericFile> failed;

        if (mTarget instanceof JavaFile) {
            failed = processJavaFiles(files, mTarget, mIsMove, filesCreated, filesDeleted);
        } else {
            failed = processCommandLineFiles(files, mTarget, mIsMove, filesCreated, filesDeleted);
        }

        if (!filesDeleted.isEmpty()) {
            MediaStoreUtils.deleteFiles(mContext.getContentResolver(), filesDeleted);
        }
        if (!filesCreated.isEmpty()) {
            PureFMFileUtils.requestMediaScanner(mContext, filesCreated);
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
    private ArrayList<GenericFile> processJavaFiles(
            @NotNull final GenericFile[] contents,
            @NotNull final GenericFile target,
            final boolean isMove,
            @NotNull final List<File> filesCreated,
            @NotNull final List<File> filesDeleted) {
        final ArrayList<GenericFile> failed = new ArrayList<GenericFile>();
        for (final GenericFile current : contents) {
            if (isCanceled()) {
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
    private static ArrayList<GenericFile> processCommandLineFiles(
            @NotNull final GenericFile[] contents,
            @NotNull final GenericFile target,
            final boolean isMove,
            @NotNull final List<File> filesCreated,
            @NotNull final List<File> filesDeleted) {
        final ArrayList<GenericFile> failed = new ArrayList<GenericFile>();
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

        final Shell shell = ShellHolder.getShell();
        for (final CommandLineFile current : cont) {
            final Command command = (isMove ? new CommandMove(current, t) :
                    new CommandCopyRecursively(current, t));

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
}
