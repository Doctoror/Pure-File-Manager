/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Modified by Yaroslav Mytkalyk 2014
 *
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

package com.cyanogenmod.filemanager.ui.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.cyanogenmod.filemanager.util.MediaHelper;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MimeTypes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class with the convenience methods for resolve intents related actions
 */
public final class IntentsActionPolicy {


    /**
     * Creates a chooser intent to share the files
     *
     * @param ctx         The current context
     * @param genericFile The file system object
     * @return chooser intent or null if no application to handle
     */
    @Nullable
    public static Intent createShareIntent(
            @NonNull final Context ctx, @NonNull final GenericFile genericFile) {
        try {
            // Create the intent to
            final Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(MimeTypes.getMimeType(genericFile.toFile()));
            final Uri uri = getUriFromFile(ctx, genericFile.toFile());
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            // Resolve the intent
            return resolveIntent(ctx, intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a chooser intent to share the files
     *
     * @param ctx          The current context
     * @param genericFiles The file system objects
     * @return chooser intent or null if no application to handle
     */
    @Nullable
    public static Intent createShareIntent(
            @NonNull final Context ctx,
            @NonNull final List<GenericFile> genericFiles) {
        try {
            // Create the intent to
            final Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create an array list of the uris to send
            final int cc = genericFiles.size();
            final ArrayList<Uri> uris = new ArrayList<Uri>(cc);
            String lastMimeType = null;
            boolean sameMimeType = true;
            for (int i = 0; i < cc; i++) {
                final GenericFile fso = genericFiles.get(i);

                // Folders are not allowed
                if (fso.isDirectory()) continue;

                // Check if we can use a unique mime/type
                String mimeType = MimeTypes.getMimeType(fso.toFile());
                if (mimeType == null) {
                    sameMimeType = false;
                } else if (sameMimeType &&
                        (lastMimeType != null &&
                                mimeType.compareTo(lastMimeType) != 0)) {
                    sameMimeType = false;
                }
                lastMimeType = mimeType;

                // Add the uri
                uris.add(getUriFromFile(ctx, fso.toFile()));
            }
            if (sameMimeType) {
                intent.setType(lastMimeType);
            } else {
                intent.setType(MimeTypes.ALL_MIME_TYPES);
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            // Resolve the intent
            return resolveIntent(ctx, intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates chooser intent
     *
     * @param ctx    The current context
     * @param intent The intent to resolve
     * @return Chooser intent to open or null if no applications to handle
     */
    @Nullable
    private static Intent resolveIntent(
            @NonNull Context ctx, @NonNull Intent intent) {
        //Retrieve the activities that can handle the file
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> info = packageManager.queryIntentActivities(intent, 0);
        if (info.isEmpty()) {
            // No registered applications, try open with wildcard mime type
            intent.setType(MimeTypes.ALL_MIME_TYPES);
            info = packageManager.queryIntentActivities(intent, 0);
            if (info.isEmpty()) {
                // No registered applications at all
                return null;
            }
        }

        return Intent.createChooser(intent, ctx.getString(R.string.menu_share));
    }

    /**
     * Returns the best Uri for the file (content uri, file uri, ...)
     *
     * @param ctx  The current context
     * @param file The file to resolve
     * @return Uri for the file
     */
    private static Uri getUriFromFile(Context ctx, File file) {
        ContentResolver cr = ctx.getContentResolver();
        Uri uri = MediaHelper.fileToContentUri(cr, file);
        if (uri == null) {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
