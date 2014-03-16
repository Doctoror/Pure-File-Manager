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
                                           @NotNull final GenericFile... files) {
        final String[] paths = new String[files.length];
        int i = 0;
        for (final GenericFile file : files) {
            paths[i] = PureFMFileUtils.fullPath(file);
            i++;
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    public static void renameFileOrDirectory(@NotNull final ContentResolver contentResolver,
                                             @NotNull final GenericFile oldFile,
                                             @NotNull final GenericFile newFile) {
        if (oldFile.isDirectory()) {
            moveOrRenameDirectory(contentResolver, oldFile, newFile);
        } else {
            renameFile(contentResolver, oldFile, newFile);
        }
    }

    private static void renameFile(@NotNull final ContentResolver contentResolver,
                                   @NotNull final GenericFile oldFile,
                                   @NotNull final GenericFile newFile) {
        final Uri uri = getContentUri(oldFile);
        final long id = fileId(contentResolver, uri, oldFile.toFile());
        if (id != -1) {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Files.FileColumns.DATA, PureFMFileUtils.fullPath(newFile));
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newFile.getName());
            contentResolver.update(ContentUris.withAppendedId(uri, id),
                    values, null, null);
        }
    }

    private static void moveOrRenameDirectory(@NotNull final ContentResolver contentResolver,
                                              @NotNull final GenericFile oldFile,
                                              @NotNull final GenericFile newFile) {
        final String oldPath = PureFMFileUtils.fullPath(oldFile);
        final String newPath = PureFMFileUtils.fullPath(newFile);
        final Uri uri = getContentUri(oldPath);

        final Pair<String, String[]> selection = listDirectoryRecursiveSelelction(oldFile);
        final Cursor c = contentResolver.query(uri, new String[] {
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DATA},
                selection.first, selection.second, null);
        if (c != null){
            try {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    final long id = c.getLong(0);
                    final String data = c.getString(1);
                    final ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Files.FileColumns.DATA, data.replace(oldPath, newPath));
                    contentResolver.update(ContentUris.withAppendedId(uri, id), values, null, null);
                }
            } finally {
                c.close();
            }
        }
    }

    private static void moveFileInSameVolume(@NotNull final ContentResolver contentResolver,
                                            @NotNull final GenericFile oldFile,
                                            @NotNull final GenericFile newFile) {
        final Uri uri = getContentUri(oldFile);
        final long id = fileId(contentResolver, uri, oldFile.toFile());
        final long parentId;
        final File newFileParent = newFile.toFile().getParentFile();
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

            // Move only if CONTENT_URI is the same and the File is not a picture
            // if you move pictures, the bucket can be updated only from MediaScanner
            if (((firstExternal && secondExternal) ||
                    (!firstExternal && !secondExternal)) &&
                            !MimeTypes.isPicture(pair.second.toFile())) {
                if (pair.first.isDirectory()) {
                    moveOrRenameDirectory(context.getContentResolver(), pair.first, pair.second);
                } else {
                    moveFileInSameVolume(context.getContentResolver(), pair.first, pair.second);
                }
            } else {
                deleteFileOrDirectory(context.getContentResolver(), pair.first);
                requestMediaScanner(context, pair.second);
            }
        }
    }

    public static void copyFiles(@NotNull final Context context,
                                 @NotNull final List<Pair<GenericFile, GenericFile>> files) {
        final int size = files.size();
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            final Pair<GenericFile, GenericFile> pair = files.get(i);
            final String newPath = PureFMFileUtils.fullPath(pair.second);
            paths[i] = newPath;
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    public static void addEmptyFileOrDirectory(final ContentResolver resolver, final GenericFile file) {
        final ContentValues values = new ContentValues(2);
        final String fullPath = PureFMFileUtils.fullPath(file);
        values.put(MediaStore.Files.FileColumns.DATA, fullPath);
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, file.getName());
        final String mimeType = MimeTypes.getMimeType(file.toFile());
        if (mimeType != null) {
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType);
        }
        resolver.insert(getContentUri(file), values);
    }

    public static Uri getContentUri(@NotNull final GenericFile file) {
        return getContentUri(PureFMFileUtils.fullPath(file));
    }

    public static Uri getContentUri(@NotNull final String path) {
        final boolean isExternal = isExternal(path);
        // general file content uri is just fine, so the code below is commented

//        if (mimeType != null) {
//            if (mimeType.startsWith("image/")) {
//                return isExternal ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI :
//                        MediaStore.Images.Media.INTERNAL_CONTENT_URI;
//            } else if (mimeType.startsWith("audio/")) {
//                return isExternal ? MediaStore.Audio.Media.EXTERNAL_CONTENT_URI :
//                        MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
//            } else if (mimeType.startsWith("video/")) {
//                return isExternal ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI :
//                        MediaStore.Video.Media.INTERNAL_CONTENT_URI;
//            }
//        }
        return MediaStore.Files.getContentUri(isExternal ?
                "external" : "internal");
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
                               @NotNull final File file) {
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
    public static Pair<String, String[]> dataSelection(final File file) {
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
        final int result = contentResolver.delete(getContentUri(canonicalPath),
                MediaStore.Files.FileColumns.DATA + "=?", new String[] {canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(getContentUri(absolutePath),
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
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

    @NotNull
    private static Pair<String, String[]> listDirectoryRecursiveSelelction(
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
        return new Pair<>(selection, selectionArgs);
    }

    public static void deleteAllFromDirectory(@NotNull final ContentResolver contentResolver,
                                              @NotNull final GenericFile dir) {

        final String canonicalPath = PureFMFileUtils.fullPath(dir);
        final Uri uri = MediaStoreUtils.getContentUri(canonicalPath);

        final Pair<String, String[]> selection = listDirectoryRecursiveSelelction(dir);
        final Cursor c = contentResolver.query(uri, new String[] {
                        MediaStore.Files.FileColumns._ID},
                                selection.first, selection.second, null);
        if (c != null){
            try {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    final long id = c.getLong(0);
                    contentResolver.delete(ContentUris.withAppendedId(uri, id), null, null);
                }
            } finally {
                c.close();
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
