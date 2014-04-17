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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Holds preview cache and provides a method for loading previews
 *
 * @author Doctoror
 */
public final class PreviewHolder {

    private static PreviewHolder sInstance;

    public static PreviewHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PreviewHolder(context);
        }
        return sInstance;
    }

    /**
     * Recommended width for previews
     */
    private final int mWidth;

    /**
     * Application's package manager
     */
    @NonNull
    private final PackageManager mPackageManager;

    /**
     * Preview cache
     */
    @NonNull
    private final ReusableBitmapLruCache<File> mPreviews = new ReusableBitmapLruCache<>();

    /**
     * Initializes PreviewHolder
     *
     * @param context Application's Context
     */
    private PreviewHolder(@NonNull final Context context) {
        mWidth = (int) context.getResources().getDimension(R.dimen.preview_width);
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            throw new IllegalArgumentException("No PackageManager for context");
        }
        mPackageManager = packageManager;
    }

    /**
     * Loads preview from the cache
     *
     * @param file File to get preview for
     * @return preview of the File, if exists in cache. Null otherwise.
     */
    @Nullable
    public Bitmap getCached(@NonNull final File file) {
        return mPreviews.get(file);
    }

    /**
     * Marks all previews as removed
     */
    public void recycle() {
        mPreviews.evictAll();
    }

    /**
     * Loads preview and puts to cache
     *
     * @param file File to load preview for
     * @return preview of the File, if exists. Null otherwise
     */
    @Nullable
    public Bitmap loadPreview(@NonNull final File file) {
        final boolean isImage = MimeTypes.isPicture(file);
        final boolean isVideo = MimeTypes.isVideo(file);
        final boolean isApk = file.getName().endsWith(".apk");

        final Bitmap result;
        if (isImage) {
            result = PFMThumbnailUtils.createPictureThumbnail(file, mWidth);
        } else if (isVideo) {
            result = PFMThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        } else if (isApk) {
            result = PFMThumbnailUtils.extractApkIcon(mPackageManager, file);
        } else {
            result = null;
        }

        if (result != null) {
            mPreviews.put(file, result);
        }

        return result;
    }
}
