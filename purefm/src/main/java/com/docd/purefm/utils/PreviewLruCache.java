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
import com.docd.purefm.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore.Video.Thumbnails;

import org.jetbrains.annotations.Nullable;

public final class PreviewLruCache
        extends ReusableBitmapLruCache<File>
{
    private final int width;
    private final PackageManager packageManager;

    public PreviewLruCache(final Context context)
    {
        this.width = (int) context.getResources().getDimension(R.dimen.preview_width);
        this.packageManager = context.getPackageManager();
    }

    @Nullable
    @Override
    protected Bitmap create(final File key)
    {
        final boolean isImage = MimeTypes.isPicture(key);
        final boolean isVideo = MimeTypes.isVideo(key);
        final boolean isApk = key.getName().endsWith(".apk");
                
        if (isImage) {
            return PureFMThumbnailUtils.createPictureThumbnail(key, this.width);
        } else if (isVideo) {
            return PureFMThumbnailUtils.createVideoThumbnail(key.getAbsolutePath(), Thumbnails.MICRO_KIND);
        } else if (isApk) {
            return PureFMThumbnailUtils.extractApkIcon(packageManager, key);
        }
        
        return null;
    }
}
