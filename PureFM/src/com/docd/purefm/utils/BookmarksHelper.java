package com.docd.purefm.utils;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.docd.purefm.Environment;

public final class BookmarksHelper {
    
    public enum Type {
        STORAGE, SDCARD, USB, USER
    }
    
    public static Set<String> getStorageBookmarks() {
        final LinkedHashSet<String> storages = new LinkedHashSet<String>();
        storages.add(Environment.androidRootDirectory.getAbsolutePath());
        storages.add(Environment.externalStorageDirectory.getAbsolutePath());
        final File secondary = Environment.getSecondaryStorageDirectory();
        if (secondary != null) {
            storages.add(secondary.getAbsolutePath());
        }
        return storages;
    }
    
    public static Type getBookmarkType(final String location) {
        if (location.equals(Environment.androidRootDirectory.getName())) {
            return Type.STORAGE;
        }
        
        final File secondary = Environment.getSecondaryStorageDirectory();
        if (secondary != null && location.equals(secondary.getName())) {
            return Type.SDCARD;
        }
        
        if (location.equals(Environment.externalStorageDirectory.getName())) {
            return secondary == null && (location.contains("sd") || location.contains("card")) ? Type.SDCARD : Type.STORAGE;
        }
        
        for (final File usb : Environment.getUsbStorageDirectories()) {
            if (usb.getName().equals(location)) {
                return Type.USB;
            }
        }
        return Type.USER;
    }
}
