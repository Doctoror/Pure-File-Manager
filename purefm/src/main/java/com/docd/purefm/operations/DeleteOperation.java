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

import com.docd.purefm.Environment;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandRemove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Operation that performs files deletion
 *
 * @author Doctoror
 */
final class DeleteOperation extends Operation<GenericFile, ArrayList<GenericFile>> {

    private final Context mContext;

    protected DeleteOperation(final Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<GenericFile> doInBackground(GenericFile... files) {
        final ArrayList<GenericFile> failed = new ArrayList<>();
        final List<GenericFile> filesAffected = new LinkedList<>();

        if (files[0] instanceof CommandLineFile) {
            final Shell shell = ShellHolder.getShell();
            if (shell == null) {
                Log.w("DeleteOperation", "shell is null, aborting");
                failed.addAll(Arrays.asList(files));
                return failed;
            }

            final HashSet<String> remountPaths = new HashSet<>();
            for (GenericFile file : files) {
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
            for (final String remountPath : remountPaths) {
                RootTools.remount(remountPath, "RW");
            }

            for (final GenericFile file : files) {
                if (isCanceled()) {
                    break;
                }
                final File fileFile = file.toFile();
                if (CommandLine.execute(shell, new CommandRemove(fileFile))) {
                    filesAffected.add(file);
                } else {
                    failed.add(file);
                }
            }

            for (final String remountPath : remountPaths) {
                RootTools.remount(remountPath, "RO");
            }

        } else {
            for (final GenericFile file : files) {
                if (isCanceled()) {
                    break;
                }
                if (file.delete()) {
                    filesAffected.add(file);
                } else {
                    failed.add(file);
                }
            }
        }

        if (!filesAffected.isEmpty()) {
            MediaStoreUtils.deleteFilesOrDirectories(mContext.getContentResolver(), filesAffected);
            FileObserverNotifier.notifyDeleted(filesAffected);
        }

        return failed;
    }

}
