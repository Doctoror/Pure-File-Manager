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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.docd.purefm.Environment;

import org.jetbrains.annotations.NotNull;

public final class BookmarksHelper {
    
    public enum Type {
        STORAGE, SDCARD, USB, USER
    }

    @NotNull
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

    @NotNull
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
