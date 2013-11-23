package com.docd.purefm.utils;

import java.io.File;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

public final class MediaStoreUtils {

    private MediaStoreUtils() {}
    
    private static final String[] FIELDS = {
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.TITLE
    };

    public static void deleteFiles(final Context context, final List<File> files) {
        final Pair<String, String[]> selectionAndPaths = filesToSelectionAndPaths(files);
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor c = context.getContentResolver().query(uri, FIELDS, selectionAndPaths.first, selectionAndPaths.second, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            final long id = c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID));
            final Uri tmpUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            context.getContentResolver().delete(tmpUri, null, null);
        }
        c.close();
    }
    
    private static Pair<String, String[]> filesToSelectionAndPaths(final List<File> files) {
        final StringBuilder selection = new StringBuilder();
        final int size = files.size();
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++)
        {
            paths[i] = files.get(i).getAbsolutePath();
            if (selection.length() != 0) {
                selection.append(" OR ");
            }
            selection.append(MediaStore.MediaColumns.DATA).append("=?");
        }
        return new Pair<String, String[]>(selection.toString(), paths);
    }
    
}
