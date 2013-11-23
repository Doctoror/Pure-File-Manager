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
    
    public static Bitmap extractApkIcon(PackageManager pm, File file) {
        final String filePath = file.getPath();
        final PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if(packageInfo != null) {
            final ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = filePath;
            appInfo.publicSourceDir = filePath;
            final Drawable icon = appInfo.loadIcon(pm);
            return ((BitmapDrawable) icon).getBitmap();
        }
        return null;
    }
}
