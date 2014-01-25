package com.docd.purefm.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

public abstract class ReusableBitmapLruCache<T> extends LruCache<T, Bitmap> {

    protected ReusableBitmapLruCache() {
        super(1024 * 1024);
    }

    @Override
    protected int sizeOf(final T key, final Bitmap value) {
        return value.getByteCount() / 1024;
    }

    @Override
    protected final void entryRemoved(boolean evicted, T key,
           Bitmap oldValue, Bitmap newValue) {
        if (oldValue.isMutable()) {
            PureFMThumbnailUtils.addToReusableCache(oldValue);
        }
    }
}
