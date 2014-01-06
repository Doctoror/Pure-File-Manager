package com.docd.purefm.utils;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

public final class PreviewHolder {

    public static void initialize(Context context) {
        previews = new PreviewLruCache(context);
    }
    
    private static PreviewLruCache previews;
    
    public static Bitmap get(File key) {
        return previews.get(key);
    }
    
    public static void recycle() {
        previews.evictAll();
    }
    
}
