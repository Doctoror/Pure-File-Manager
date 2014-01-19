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