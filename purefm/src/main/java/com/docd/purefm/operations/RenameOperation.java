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
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.stericson.RootTools.RootTools;

import org.jetbrains.annotations.NotNull;

/**
 * Performs file renaming
 *
 * {@link #doInBackground(Void...)} returns null if operation completed successfully,
 * or error message if an error occurred
 */
final class RenameOperation extends Operation<Void, CharSequence> {

    private final Context mContext;
    private final GenericFile mSource;
    private final String mTargetName;

    RenameOperation(@NotNull final Context context,
                           @NotNull final GenericFile source,
                           @NotNull final String targetName) {
        this.mContext = context;
        this.mSource = source;
        this.mTargetName = targetName;
    }

    @Override
    protected CharSequence doInBackground(Void... voids) {
        final GenericFile sourceParent = mSource.getParentFile();
        if (sourceParent == null) {
            return "Could not resolve parent directory. Renaming failed.";
        }
        final GenericFile target = FileFactory.newFile(sourceParent.toFile(), mTargetName);
        if (target.exists()) {
            return mContext.getText(R.string.file_exists);
        }
        final String path = target.getAbsolutePath();
        final boolean remount = Environment.needsRemount(path);
        if (remount) {
            RootTools.remount(path, "RW");
        }
        try {
            if (mSource.renameTo(target)) {
                MediaStoreUtils.renameFile(mContext.getContentResolver(), mSource, target);
                return null;
            }
        } finally {
            if (remount) {
                RootTools.remount(path, "RO");
            }
        }
        return mContext.getString(R.string.rename_failed,
                mSource.getName(), target.getName());
    }
}
