package com.docd.purefm.utils;

import java.io.File;

import android.content.Context;

import com.docd.purefm.drawable.RecyclingBitmapDrawable;

import org.jetbrains.annotations.Nullable;

public final class PreviewHolder {

    public static void initialize(Context context) {
        previews = new PreviewLruCache(context);
    }
    
    private static PreviewLruCache previews;

    @Nullable
    public static RecyclingBitmapDrawable get(final File key) {
        return previews.get(key);
    }
    
    public static void recycle() {
        previews.evictAll();
    }
    
}
