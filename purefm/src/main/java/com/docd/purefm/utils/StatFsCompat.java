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
