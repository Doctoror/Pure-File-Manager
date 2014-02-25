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
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandRemove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import java.io.File;
import java.util.ArrayList;
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
    protected ArrayList<GenericFile> execute(GenericFile... files) {
        final ArrayList<GenericFile> failed = new ArrayList<GenericFile>();
        final List<File> filesAffected = new LinkedList<File>();

        if (files[0] instanceof CommandLineFile) {

            final HashSet<String> remountPaths = new HashSet<String>();
            for (final GenericFile file : files) {
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

            final Shell shell = ShellHolder.getShell();
            for (final GenericFile file : files) {
                final File fileFile = file.toFile();
                if (CommandLine.execute(shell, new CommandRemove(fileFile))) {
                    filesAffected.add(fileFile);
                } else {
                    failed.add(file);
                }
            }

            for (final String remountPath : remountPaths) {
                RootTools.remount(remountPath, "RO");
            }

        } else {
            for (final GenericFile file : files) {
                if (file.delete()) {
                    final File fileFile = file.toFile();
                    filesAffected.add(fileFile);
                } else {
                    failed.add(file);
                }
            }
        }

        if (!filesAffected.isEmpty()) {
            MediaStoreUtils.deleteFiles(mContext.getContentResolver(), filesAffected);
            PureFMFileUtils.requestMediaScanner(mContext, filesAffected);
        }

        return failed;
    }

}
