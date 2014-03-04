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
package com.docd.purefm.utils;

import java.io.File;
import java.util.List;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

import org.jetbrains.annotations.NotNull;

public final class MediaStoreUtils {

    private MediaStoreUtils() {}

    private static final Uri INTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("internal");
    private static final Uri EXTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("external");

    public static void deleteFile(final ContentResolver contentResolver, final File file) {
        final String selection = MediaStore.Files.FileColumns.DATA + "=?";
        final String[] selectionArgs = new String[] {'\'' + file.getAbsolutePath() + '\''};
        contentResolver.delete(INTERNAL_CONTENT_URI, selection, selectionArgs);
        contentResolver.delete(EXTERNAL_CONTENT_URI, selection, selectionArgs);
    }

    public static void deleteFiles(final ContentResolver contentResolver, final List<File> files) {
        final String selection = filesToSelection(files);
        contentResolver.delete(INTERNAL_CONTENT_URI, selection, null);
        contentResolver.delete(EXTERNAL_CONTENT_URI, selection, null);
    }

    @NotNull
    private static String filesToSelection(final List<File> files) {
        final StringBuilder selection = new StringBuilder(MediaStore.Files.FileColumns.DATA);
        selection.append(" IN ");
        selection.append('(');
        final int size = files.size();
        for (int i = 0; i < size; i++) {
            selection.append('\'');
            selection.append(files.get(i).getAbsolutePath());
            selection.append('\'');
            if (i != size - 1) {
                selection.append(',');
            }
        }
        selection.append(')');
        return selection.toString();
    }
}
