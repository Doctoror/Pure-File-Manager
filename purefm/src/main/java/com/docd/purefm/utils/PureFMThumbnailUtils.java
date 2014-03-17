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
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;

import org.jetbrains.annotations.Nullable;

public final class PureFMThumbnailUtils extends ThumbnailUtils {
    
    private PureFMThumbnailUtils() {}

    private static final Set<SoftReference<Bitmap>> sReusableBitmaps =
            Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());

    public static void addToReusableCache(final Bitmap value) {
        sReusableBitmaps.add(new SoftReference<>(value));
    }

    public static Bitmap createPictureThumbnail(final File target, final int w) {
        return decodeSampledBitmap(target, w);
    }

    @Nullable
    public static Bitmap extractApkIcon(PackageManager pm, File file) {
        final String filePath = file.getPath();
        final PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if(packageInfo != null) {
            final ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (appInfo != null) {
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
                final Drawable icon = appInfo.loadIcon(pm);
                if (icon != null) {
                    return ((BitmapDrawable) icon).getBitmap();
                }
            }
        }
        return null;
    }

    private static Bitmap decodeSampledBitmap(final File file, final int reqWidth) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inJustDecodeBounds = false;
        options.inMutable = true;

        if (options.outWidth != -1 && options.outHeight != -1) {
            final int originalSize = (options.outHeight > options.outWidth) ? options.outWidth
                    : options.outHeight;
            options.inSampleSize = originalSize / reqWidth;
        }

        final Bitmap inBitmap = getBitmapFromReusableSet(options);
        if (inBitmap != null) {
            options.inBitmap = inBitmap;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * Returns reusable bitmap, if exists
     *
     * @param options Options to look against
     * @return reusable bitmap, if exists
     */
    private static Bitmap getBitmapFromReusableSet(final BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (sReusableBitmaps != null && sReusableBitmaps.isEmpty()) {
            synchronized (sReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator
                        = sReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (item != null && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @SuppressLint("NewApi")
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            final int width = targetOptions.outWidth / targetOptions.inSampleSize;
            final int height = targetOptions.outHeight / targetOptions.inSampleSize;
            final int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * Returns the byte usage per pixel of a bitmap based on its configuration.
     *
     * @return byte usage per pixel
     */
    private static int getBytesPerPixel(final Bitmap.Config config) {
        switch (config) {
            case ARGB_8888:
                return 4;

            case RGB_565:
            case ARGB_4444:
                return 2;

            default:
                return 1;
        }
    }
}
