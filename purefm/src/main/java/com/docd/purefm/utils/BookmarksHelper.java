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

import android.content.Context;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.docd.purefm.Environment;
import com.docd.purefm.settings.Settings;

import org.jetbrains.annotations.NotNull;

public final class BookmarksHelper {
    
    public enum Type {
        STORAGE, SDCARD, USB, USER
    }

    public static Set<String> getAllLocations(final Context context) {
        final Set<String> result = new TreeSet<>();
        result.addAll(BookmarksHelper.getStorageBookmarks());
        result.addAll(Settings.getBookmarks(context));
        return result;
    }

    @NotNull
    public static Set<String> getStorageBookmarks() {
        final LinkedHashSet<String> storages = new LinkedHashSet<>();
        for (final StorageHelper.StorageVolume v : Environment.getStorageVolumes()) {
            storages.add(v.file.getAbsolutePath());
        }
        return storages;
    }

    @NotNull
    public static Type getBookmarkType(final String location) {
        for (final StorageHelper.StorageVolume v : Environment.getStorageVolumes()) {
            if (location.equals(v.file.getAbsolutePath())) {
                switch (v.getType()) {
                    case EXTERNAL:
                        return Type.SDCARD;

                    case USB:
                        return Type.USB;

                    case INTERNAL:
                    default:
                        return Type.STORAGE;
                }
            }
        }
        return Type.USER;
    }
}
