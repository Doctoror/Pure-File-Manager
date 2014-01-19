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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import org.jetbrains.annotations.Nullable;

public final class PureFMThumbnailUtils extends ThumbnailUtils {
    
    private PureFMThumbnailUtils() {}

    public static Bitmap createPictureThumbnail(final File target, final int w) {
        
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        
        BitmapFactory.decodeFile(target.getAbsolutePath(), o);
        o.inJustDecodeBounds = false;
        
        if (o.outWidth != -1 && o.outHeight != -1) {
            final int originalSize = (o.outHeight > o.outWidth) ? o.outWidth
                    : o.outHeight;
            o.inSampleSize = originalSize / w;
        }
        
        return BitmapFactory.decodeFile(target.getAbsolutePath(), o);
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
}
