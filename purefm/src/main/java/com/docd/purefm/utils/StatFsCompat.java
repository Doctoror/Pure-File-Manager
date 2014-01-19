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

public final class StatFsCompat {
    private StatFsCompat() {}
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getAvailableBlocksLong(final StatFs statFs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getAvailableBlocksLong();
        } else {
            
            return statFs.getAvailableBlocks();
        }
    }
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getBlockCountLong(final StatFs statFs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getBlockCountLong();
        } else {
            return statFs.getBlockCount();
        }
    }
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getBlockSizeLong(final StatFs statFs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getBlockSizeLong();
        } else {
            return statFs.getBlockSize();
        }
    }
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getFreeBlocksLong(final StatFs statFs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getFreeBlocksLong();
        } else {
            return statFs.getFreeBlocks();
        }
    }
}
