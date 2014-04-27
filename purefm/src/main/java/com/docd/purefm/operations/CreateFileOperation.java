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
import android.support.annotation.NonNull;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PFMFileUtils;
import com.stericson.RootTools.RootTools;

import java.io.IOException;

/**
 * Creates a single file. Result is null on success, otherwise the result is a error message to
 * display to user
 *
 * @author Doctoror
 */
final class CreateFileOperation extends Operation<GenericFile, CharSequence> {

    @NonNull
    private final Context mContext;

    CreateFileOperation(@NonNull final Context context) {
        mContext = context;
    }

    @Override
    protected CharSequence doInBackground(@NonNull final GenericFile... genericFiles) {
        if (genericFiles.length == 0) {
            throw new IllegalArgumentException("Params not supplied");
        }
        final GenericFile target = genericFiles[0];
        if (target.exists()) {
            return mContext.getText(R.string.file_exists);
        }

        final String path = PFMFileUtils.fullPath(target);
        final Settings settings = Settings.getInstance(mContext);
        boolean remount = false;
        try {
            if (settings.useCommandLine() && settings.isSuEnabled() &&
                    Environment.needsRemount(path)) {
                remount = true;
                    RootTools.remount(path, "RW");
                }
                if (!target.createNewFile()) {
                    return mContext.getText(R.string.could_not_create_file);
                } else {
                    MediaStoreUtils.addEmptyFileOrDirectory(mContext.getContentResolver(), target);
                    FileObserverNotifier.notifyCreated(target);
                }
        } catch (IOException e) {
            return e.getMessage();
        } finally {
            if (remount) {
                RootTools.remount(path, "RO");
            }
        }
        return null;
    }
}
