package com.docd.purefm.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

/**
 * Refer to http://developer.android.com/training/displaying-bitmaps/manage-memory.html
 */
public final class RecyclingBitmapDrawable extends BitmapDrawable {

    private int mCacheRefCount;
    private int mDisplayRefCount;
    private boolean mHasBeenDisplayed;

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap, boolean cached) {
        super(res, bitmap);
        if (cached) {
            this.mCacheRefCount = 1;
        }
    }

    public RecyclingBitmapDrawable(Resources res, String filePath) {
        super(res, filePath);
    }

    public RecyclingBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    // Notify the drawable that the displayed state has changed.
    // Keep a count to determine when the drawable is no longer displayed.
    public void setIsDisplayed(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState();
    }

    // Notify the drawable that the cache state has changed.
    // Keep a count to determine when the drawable is no longer being cached.
    public void setIsCached(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState();
    }

    private synchronized void checkState() {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle.
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                && hasValidBitmap()) {
            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        final Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }
}