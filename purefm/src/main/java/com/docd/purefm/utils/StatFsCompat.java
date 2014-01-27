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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.StatFs;

/**
 * Backward compatible version of {@link StatFs}
 */
public final class StatFsCompat {

    private final StatFs mStatFs;

    public StatFsCompat(final String path) {
        this.mStatFs = new StatFs(path);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public long getAvailableBlocksLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mStatFs.getAvailableBlocksLong();
        } else {
            return mStatFs.getAvailableBlocks();
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public long getBlockCountLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mStatFs.getBlockCountLong();
        } else {
            return mStatFs.getBlockCount();
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public long getBlockSizeLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mStatFs.getBlockSizeLong();
        } else {
            return mStatFs.getBlockSize();
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public long getFreeBlocksLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mStatFs.getFreeBlocksLong();
        } else {
            return mStatFs.getFreeBlocks();
        }
    }
}