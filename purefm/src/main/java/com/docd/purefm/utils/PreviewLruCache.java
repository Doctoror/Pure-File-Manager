package com.docd.purefm.utils;

import java.io.File;

import com.docd.purefm.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.LruCache;

public final class PreviewLruCache
        extends LruCache<File, Bitmap>
{
    private final int width;
    private final PackageManager packageManager;

    public PreviewLruCache(Context context)
    {
        super(1024 * 1024);
        this.width = (int) context.getResources().getDimension(R.dimen.preview_width);
        this.packageManager = context.getPackageManager();
    }

    @Override
    protected Bitmap create(File key)
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

    @Override
    protected void entryRemoved(boolean evicted, File key, Bitmap oldValue,
            Bitmap newValue)
    {
        if (!oldValue.isRecycled()) {
            oldValue.recycle();
        }
    }

}
