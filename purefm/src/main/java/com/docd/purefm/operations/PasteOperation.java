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
import android.util.Pair;

import com.docd.purefm.Environment;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PFMFileUtils;
import com.stericson.RootTools.RootTools;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Performs paste operation
 */
final class PasteOperation extends Operation<GenericFile, ArrayList<GenericFile>> {

    @NonNull
    private final Context mContext;

    @NonNull
    private final Settings mSettings;

    @NonNull
    private final GenericFile mTarget;

    private final boolean mIsMove;

    PasteOperation(@NonNull final Context context,
                   @NonNull final GenericFile target,
                   final boolean isMove) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mTarget = target;
        mIsMove = isMove;
    }

    @Override
    protected ArrayList<GenericFile> doInBackground(@NonNull final GenericFile... files) {
        ClipBoard.lock();

        final LinkedList<Pair<GenericFile, GenericFile>> filesAffected =
                new LinkedList<>();

        final ArrayList<GenericFile> failed = new ArrayList<>();

        final String targetPath = mTarget.getAbsolutePath();
        final boolean remounted;

        final boolean useCommandLine = mSettings.useCommandLine();
        if (useCommandLine && Environment.needsRemount(targetPath)) {
            RootTools.remount(targetPath, "RW");
            remounted = true;
        } else {
            remounted = false;
        }
        try {
            for (final GenericFile current : files) {
                if (isCanceled()) {
                    return failed;
                }

                if (current != null && current.exists()) {
                    try {
                        if (mIsMove) {
                            PFMFileUtils.moveToDirectory(current, mTarget, useCommandLine, true);
                        } else {
                            if (current.isDirectory()) {
                                PFMFileUtils.copyDirectoryToDirectory(current, mTarget, useCommandLine);
                            } else {
                                PFMFileUtils.copyFileToDirectory(current, mTarget, useCommandLine);
                            }
                        }
                        filesAffected.add(new Pair<>(current, FileFactory.newFile(
                                mSettings, mTarget.toFile(), current.getName())));
                    } catch (IOException e) {
                        failed.add(current);
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            if (remounted) {
                RootTools.remount(targetPath, "RO");
            }

            if (!filesAffected.isEmpty()) {
                ClipBoard.unlock();
                ClipBoard.clear();
                if (mIsMove) {
                    MediaStoreUtils.moveFiles(mContext, filesAffected);
                    for (final Pair<GenericFile, GenericFile> filesPair : filesAffected) {
                        FileObserverNotifier.notifyDeleted(filesPair.first);
                        FileObserverNotifier.notifyCreated(filesPair.second);
                    }
                } else {
                    MediaStoreUtils.copyFiles(mContext, filesAffected);
                    for (final Pair<GenericFile, GenericFile> filesPair : filesAffected) {
                        FileObserverNotifier.notifyCreated(filesPair.second);
                    }
                }
            }
        }

        return failed;
    }
}
