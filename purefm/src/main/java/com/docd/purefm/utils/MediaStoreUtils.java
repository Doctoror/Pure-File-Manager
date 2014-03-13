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
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

import com.docd.purefm.Environment;
import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

public final class MediaStoreUtils {

    private MediaStoreUtils() {}

    public static void requestMediaScanner(@NotNull final Context context,
                                           @NotNull final List<GenericFile> files) {
        final String[] paths = new String[files.size()];
        int i = 0;
        for (final GenericFile file : files) {
            paths[i] = PureFMFileUtils.fullPath(file);
            i++;
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    public static void renameFile(@NotNull final ContentResolver contentResolver,
                                  @NotNull final GenericFile oldFile,
                                  @NotNull final GenericFile newFile) {
        final Uri uri = getContentUri(oldFile);
        final long id = fileId(contentResolver, uri, oldFile);
        if (id != -1) {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Files.FileColumns.DATA, PureFMFileUtils.fullPath(newFile));
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newFile.getName());
            contentResolver.update(ContentUris.withAppendedId(uri, id),
                    values, null, null);
        }
    }

    public static void moveFileInSameVolume(@NotNull final ContentResolver contentResolver,
                                            @NotNull final GenericFile oldFile,
                                            @NotNull final GenericFile newFile) {
        final Uri uri = getContentUri(oldFile);
        final long id = fileId(contentResolver, uri, oldFile);
        final long parentId;
        final GenericFile newFileParent = newFile.getParentFile();
        if (newFileParent != null) {
            parentId = fileId(contentResolver, uri, newFileParent);
        } else {
            parentId = -1;
        }
        if (id != -1) {
            final ContentValues values = new ContentValues(parentId == -1 ? 1 : 2);
            values.put(MediaStore.Files.FileColumns.DATA, PureFMFileUtils.fullPath(newFile));
            if (parentId != -1) {
                values.put(MediaStore.Files.FileColumns.PARENT, parentId);
            }
            contentResolver.update(ContentUris.withAppendedId(uri, id),
                    values, null, null);
        }
    }

    public static void moveFiles(@NotNull final Context context,
                                 @NotNull final List<Pair<GenericFile, GenericFile>> files) {
        for (final Pair<GenericFile, GenericFile> pair : files) {
            final String secondPath = PureFMFileUtils.fullPath(pair.second);
            boolean firstExternal = isExternal(PureFMFileUtils.fullPath(pair.first));
            boolean secondExternal = isExternal(secondPath);
            if ((firstExternal && secondExternal) ||
                    (!firstExternal && !secondExternal)) {
                moveFileInSameVolume(context.getContentResolver(), pair.first, pair.second);
            } else {
                copyFile(context, pair.first, pair.second);
                deleteFile(context.getContentResolver(), pair.first);
            }
        }
    }

    public static void copyFiles(@NotNull final Context context,
                                 @NotNull final List<Pair<GenericFile, GenericFile>> files) {

        final int size = files.size();
//        final String[] paths = new String[size];
//        for (int i = 0; i < size; i++) {
//            final Pair<GenericFile, GenericFile> pair = files.get(i);
//            final String newPath = PureFMFileUtils.fullPath(pair.second);
//            paths[i] = newPath;
//        }
//        MediaScannerConnection.scanFile(context, paths, null, null);
        final ContentResolver resolver = context.getContentResolver();
        final String[] allColumns = getCopyColumns();
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            final Pair<GenericFile, GenericFile> pair = files.get(i);
            final String newPath = PureFMFileUtils.fullPath(pair.second);
            paths[i] = newPath;
            final Pair<String, String[]> selection = dataSelection(pair.first);
            final Cursor c = resolver.query(getContentUri(pair.first), allColumns,
                    selection.first, selection.second, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    final ContentValues values = new ContentValues(6);
                    values.put(MediaStore.Files.FileColumns.DATA, newPath);
                    values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, pair.second.getName());
                    values.put(MediaStore.Files.FileColumns.DATE_ADDED,
                            c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                    values.put(MediaStore.Files.FileColumns.DATE_MODIFIED,
                            c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));
                    values.put(MediaStore.Files.FileColumns.MEDIA_TYPE,
                            c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)));
                    values.put(MediaStore.Files.FileColumns.MIME_TYPE,
                            c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)));
                    resolver.insert(getContentUri(pair.second), values);
                }
            }
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    public static void copyFile(@NotNull final Context context,
                                @NotNull final GenericFile source,
                                @NotNull final GenericFile target) {
        final ContentResolver resolver = context.getContentResolver();
        final String[] copyColumns = getCopyColumns();
        final String newPath = PureFMFileUtils.fullPath(target);
        final Pair<String, String[]> selection = dataSelection(source);
        final Cursor c = resolver.query(getContentUri(source), copyColumns,
                selection.first, selection.second, null);
        if (c != null) {
            if (c.moveToFirst()) {
                final ContentValues values = new ContentValues(6);
                values.put(MediaStore.Files.FileColumns.DATA, newPath);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, target.getName());
                values.put(MediaStore.Files.FileColumns.DATE_ADDED,
                        c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                values.put(MediaStore.Files.FileColumns.DATE_MODIFIED,
                        c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));
                values.put(MediaStore.Files.FileColumns.MEDIA_TYPE,
                        c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)));
                values.put(MediaStore.Files.FileColumns.MIME_TYPE,
                        c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)));
                resolver.insert(getContentUri(target), values);
            }
        }
        MediaScannerConnection.scanFile(context, new String[] {newPath}, null, null);
    }

    public static void addFileOrDirectory(final ContentResolver resolver, final GenericFile file) {
        final ContentValues values = new ContentValues(2);
        final String fullPath = PureFMFileUtils.fullPath(file);
        values.put(MediaStore.Files.FileColumns.DATA, fullPath);
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, file.getName());
        final String mimeType = MimeTypes.getMimeType(file.toFile());
        if (mimeType != null) {
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType);
        }
        resolver.insert(getContentUri(fullPath), values);
    }

    private static String[] getCopyColumns() {
            return new String[] {
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE
            };
    }

    public static Uri getContentUri(@NotNull final GenericFile file) {
        return getContentUri(PureFMFileUtils.fullPath(file));
    }

    public static Uri getContentUri(@NotNull final String path) {
        if (isExternal(path)) {
            return MediaStore.Files.getContentUri("external");
        } else {
           return MediaStore.Files.getContentUri("internal");
        }
    }

    public static boolean isExternal(@NotNull final String path) {
        for (final StorageHelper.StorageVolume volume : Environment.getStorageVolumes()) {
            if (path.startsWith(volume.file.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    private static long fileId(@NotNull final ContentResolver contentResolver,
                               @NotNull final Uri uri,
                               @NotNull final GenericFile file) {
        final Pair<String, String[]> selection = dataSelection(file);
        final Cursor c = contentResolver.query(uri,
                new String[] {MediaStore.Files.FileColumns._ID},
                        selection.first, selection.second, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0);
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

    @NotNull
    public static Pair<String, String[]> dataSelection(final GenericFile file) {
        final String canonicalPath = PureFMFileUtils.fullPath(file);
        final String absolutePath = file.getAbsolutePath();
        final String selection;
        final String[] selectionArgs;
        if (canonicalPath.equals(absolutePath)) {
            selection = MediaStore.Files.FileColumns.DATA + "=?";
            selectionArgs = new String[] {canonicalPath};
        } else {
            selection = MediaStore.Files.FileColumns.DATA + "=? OR " +
                    MediaStore.Files.FileColumns.DATA + "=?";
            selectionArgs = new String[] {canonicalPath, absolutePath};
        }
        return new Pair<>(selection, selectionArgs);
    }

    public static void deleteFile(@NotNull final ContentResolver contentResolver,
                                  @NotNull final GenericFile file) {
        final String canonicalPath = PureFMFileUtils.fullPath(file);
        final int result = contentResolver.delete(getContentUri(canonicalPath), MediaStore.Files.FileColumns.DATA + "=?",
                new String[] {canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(getContentUri(absolutePath), MediaStore.Files.FileColumns.DATA + "=?",
                        new String[] {absolutePath});
            }
        }
    }

    public static void deleteFileOrDirectory(@NotNull final ContentResolver contentResolver,
                                             @NotNull final GenericFile file) {
        if (file.isDirectory()) {
            deleteAllFromDirectory(contentResolver, file);
        } else {
            deleteFile(contentResolver, file);
        }
    }

    public static void deleteFilesOrDirectories(@NotNull final ContentResolver contentResolver,
                                                @NotNull final List<GenericFile> files) {
        final List<GenericFile> regular = new ArrayList<>();
        for (final GenericFile file : files) {
            if (file.isDirectory()) {
                deleteAllFromDirectory(contentResolver, file);
            } else {
                regular.add(file);
            }
        }
        if (!regular.isEmpty()) {
            final String selection = filesToSelection(regular);
            contentResolver.delete(getContentUri(regular.get(0)), selection, null);
        }
    }

    public static void deleteAllFromDirectory(@NotNull final ContentResolver contentResolver,
                                              @NotNull final GenericFile dir) {
        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException("\'dir\' is not a directory");
        }

        final String canonicalPath = PureFMFileUtils.fullPath(dir);
        boolean pathEndsWithSeparator = canonicalPath.endsWith(File.separator);
        String pathNoSeparator = pathEndsWithSeparator ? canonicalPath.substring(0, canonicalPath.length() - 1) : canonicalPath;
        String pathWithSeparator = pathEndsWithSeparator ? canonicalPath : canonicalPath.concat(File.separator);

        final String absolutePath = dir.getAbsolutePath();

        final String selection;
        final String[] selectionArgs;
        if (canonicalPath.equals(absolutePath)) {
            selection = MediaStore.Files.FileColumns.DATA + "=? OR " +
                    MediaStore.Files.FileColumns.DATA + " LIKE \'" + pathWithSeparator + "%\'";
            selectionArgs = new String[] {pathNoSeparator};
        } else {
            pathEndsWithSeparator = absolutePath.endsWith(File.separator);
            String absolutePathNoSeparator = pathEndsWithSeparator ? absolutePath.substring(0, absolutePath.length() - 1) : absolutePath;
            String absolutePathWithSeparator = pathEndsWithSeparator ? absolutePath : absolutePath.concat(File.separator);
            selection = MediaStore.Files.FileColumns.DATA + "=? OR " +
                    MediaStore.Files.FileColumns.DATA + "=? OR " +
                    MediaStore.Files.FileColumns.DATA + " LIKE \'" + pathWithSeparator + "%\'" + " OR " +
                    MediaStore.Files.FileColumns.DATA + " LIKE \'" + absolutePathWithSeparator + "%\'";
            selectionArgs = new String[] {pathNoSeparator, absolutePathNoSeparator};
        }
        final Uri uri = MediaStoreUtils.getContentUri(canonicalPath);
        final Cursor c = contentResolver.query(uri, new String[] {MediaStore.Files.FileColumns._ID},
                selection, selectionArgs, null);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final long id = c.getLong(0);
                contentResolver.delete(ContentUris.withAppendedId(uri, id), null, null);
            }
        }
    }

    @NotNull
    private static String filesToSelection(final List<GenericFile> files) {
        final StringBuilder selection = new StringBuilder(MediaStore.Files.FileColumns.DATA);
        selection.append(" IN ");
        selection.append('(');
        final int size = files.size();
        for (int i = 0; i < size; i++) {
            final GenericFile file = files.get(i);
            final String canonicalPath = PureFMFileUtils.fullPath(file);
            final String absolutePath = file.getAbsolutePath();
            selection.append('\'');
            selection.append(canonicalPath);
            selection.append('\'');
            if (!absolutePath.equals(canonicalPath)) {
                selection.append(',');
                selection.append('\'');
                selection.append(absolutePath);
                selection.append('\'');
            }
            if (i != size - 1) {
                selection.append(',');
            }
        }
        selection.append(')');
        return selection.toString();
    }
}
