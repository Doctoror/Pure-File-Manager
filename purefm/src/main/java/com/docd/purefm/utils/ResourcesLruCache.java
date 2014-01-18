package com.docd.purefm.utils;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import org.jetbrains.annotations.Nullable;

public final class ResourcesLruCache extends LruCache<Integer, Drawable> {

    private static ResourcesLruCache instance;

    public static synchronized ResourcesLruCache getInstance(final Resources res) {
        if (instance == null) {
            instance = new ResourcesLruCache(res);
        }
        return instance;
    }

    private final Resources mResources;

    private ResourcesLruCache(final Resources res) {
        super(512 * 1024);
        this.mResources = res;
    }

    @Nullable
    @Override
    protected Drawable create(final Integer key) {
        return this.mResources.getDrawable(key);
    }

    @Override
    protected int sizeOf(Integer key, Drawable value) {
        if (value instanceof BitmapDrawable) {
            return ((BitmapDrawable) value).getBitmap().getByteCount() / 1024;
        } else {
            return super.sizeOf(key, value);
        }
    }
}
