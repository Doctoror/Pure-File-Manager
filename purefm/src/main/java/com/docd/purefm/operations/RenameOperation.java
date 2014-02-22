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

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs file renaming
 */
final class RenameOperation extends Operation<Void, Boolean> {

    private final Context mContext;
    private final GenericFile mSource;
    private final GenericFile mTarget;

    RenameOperation(@NotNull final Context context,
                           @NotNull final GenericFile source,
                           @NotNull final GenericFile target) {
        this.mContext = context;
        this.mSource = source;
        this.mTarget = target;
    }

    @Override
    protected Boolean execute(Void... voids) {
        if (mSource.renameTo(mTarget)) {
            final List<File> filesCreated = new ArrayList<File>(1);
            final List<File> filesDeleted = new ArrayList<File>(1);
            filesDeleted.add(mSource.toFile());
            filesCreated.add(mTarget.toFile());
            MediaStoreUtils.deleteFiles(mContext.getContentResolver(), filesDeleted);
            PureFMFileUtils.requestMediaScanner(mContext, filesCreated);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
