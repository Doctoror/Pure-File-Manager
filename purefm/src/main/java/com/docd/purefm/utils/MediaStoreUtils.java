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
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

import org.jetbrains.annotations.NotNull;

public final class MediaStoreUtils {

    private MediaStoreUtils() {}

    private static final Uri EXTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("external");
    
    private static final String[] FIELDS = {
            MediaStore.Files.FileColumns._ID
    };

    public static void deleteFile(final ContentResolver contentResolver, final File file) {
        final Cursor c = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                FIELDS,
                MediaStore.Files.FileColumns.DATA + "=?",
                new String[]{file.getAbsolutePath()},
                null);
        if (c != null) {
            final long id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID));
            contentResolver.delete(ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id), null, null);
            c.close();
        }
    }

    public static void deleteFiles(final ContentResolver contentResolver, final List<File> files) {
        final Pair<String, String[]> selectionAndPaths = filesToSelectionAndPaths(files);
        final Cursor c = contentResolver.query(EXTERNAL_CONTENT_URI, FIELDS, selectionAndPaths.first, selectionAndPaths.second, null);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final long id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID));
                contentResolver.delete(ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id), null, null);
            }
            c.close();
        }
    }

    @NotNull
    private static Pair<String, String[]> filesToSelectionAndPaths(final List<File> files) {
        final StringBuilder selection = new StringBuilder();
        final int size = files.size();
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            paths[i] = files.get(i).getAbsolutePath();
            if (selection.length() != 0) {
                selection.append(" OR ");
            }
            selection.append(MediaStore.Files.FileColumns.DATA).append("=?");
        }
        return new Pair<String, String[]>(selection.toString(), paths);
    }
    
}
