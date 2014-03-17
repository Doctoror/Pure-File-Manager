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

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.docd.purefm.R;

import org.jetbrains.annotations.Nullable;

/**
 * Holds preview cache and provides a method for loading previews
 *
 * @author Doctoror
 */
public final class PreviewHolder {

    /**
     * Recommended width for previews
     */
    private static int sWidth;

    /**
     * Application's package manager
     */
    private static PackageManager sPackageManager;

    /**
     * Preview cache
     */
    private static ReusableBitmapLruCache<File> sPreviews;

    /**
     * Initialized state flag
     */
    private static boolean sIsInitialized;

    /**
     * Initializes PreviewHolder
     *
     * @param context Application's Context
     */
    public static void initialize(Context context) {
        sPreviews = new ReusableBitmapLruCache<>();
        sWidth = (int) context.getResources().getDimension(R.dimen.preview_width);
        sPackageManager = context.getPackageManager();
        sIsInitialized = true;
    }

    /**
     * Loads preview from the cache
     *
     * @param file File to get preview for
     * @return preview of the File, if exists in cache. Null otherwise.
     */
    @Nullable
    public static Bitmap getCached(final File file) {
        if (!sIsInitialized) {
            throw new IllegalStateException("PreviewHolder is not initialized");
        }
        return sPreviews.get(file);
    }

    /**
     * Marks all previews as removed
     */
    public static void recycle() {
        if (!sIsInitialized) {
            throw new IllegalStateException("PreviewHolder is not initialized");
        }
        sPreviews.evictAll();
    }

    /**
     * Loads preview and puts to cache
     *
     * @param file File to load preview for
     * @return preview of the File, if exists. Null otherwise
     */
    @Nullable
    public static Bitmap loadPreview(final File file) {
        if (!sIsInitialized) {
            throw new IllegalStateException("PreviewHolder is not initialized");
        }
        final boolean isImage = MimeTypes.isPicture(file);
        final boolean isVideo = MimeTypes.isVideo(file);
        final boolean isApk = file.getName().endsWith(".apk");

        final Bitmap result;
        if (isImage) {
            result = PureFMThumbnailUtils.createPictureThumbnail(file, sWidth);
        } else if (isVideo) {
            result = PureFMThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        } else if (isApk) {
            result = PureFMThumbnailUtils.extractApkIcon(sPackageManager, file);
        } else {
            result = null;
        }

        if (result != null) {
            sPreviews.put(file, result);
        }

        return result;
    }
    
}
