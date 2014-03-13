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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.settings.Settings;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public final class BookmarksHelper {

    public static final class BookmarkItem {
        Drawable mIcon;
        CharSequence mDisplayName;
        CharSequence mDisplayPath;

        BookmarkItem() {

        }

        public Drawable getIcon() {
            return mIcon;
        }

        public CharSequence getDisplayName() {
            return mDisplayName;
        }

        public CharSequence getDisplayPath() {
            return mDisplayPath;
        }
    }

    @NotNull
    public static BookmarkItem createUserBookmarkItem(
            @NotNull final Activity activity,
            @NotNull final String path) {
        final BookmarkItem item = new BookmarkItem();
        item.mDisplayName = FilenameUtils.getName(path);
        if (item.mDisplayName.equals(Environment.rootDirectory.getAbsolutePath())) {
            item.mDisplayName = activity.getText(R.string.root);
        }
        item.mDisplayPath = path;
        item.mIcon = ThemeUtils.getDrawable(activity.getTheme(), R.attr.ic_bookmark);
        return item;
    }

    public static List<BookmarkItem> getAllBookmarks(@NotNull final Activity activity) {
        final List<BookmarkItem> items = new ArrayList<>();
        int internalCount = 0;
        int externalCount = 0;
        int usbCount = 0;

        final Resources.Theme theme = activity.getTheme();
        final Drawable iconStorage = ThemeUtils.getDrawable(theme, R.attr.ic_storage);
        final Drawable iconSdcard = ThemeUtils.getDrawable(theme, R.attr.ic_sdcard);
        final Drawable iconUsb = ThemeUtils.getDrawable(theme, R.attr.ic_usb);
        final Drawable iconUser = ThemeUtils.getDrawable(theme, R.attr.ic_bookmark);

        for (final StorageHelper.StorageVolume v : Environment.getStorageVolumes()) {
            final BookmarkItem item = new BookmarkItem();
            switch (v.getType()) {
                case EXTERNAL:
                    externalCount++;
                    item.mIcon = iconSdcard;
                    item.mDisplayName = activity.getText(R.string.storage_sdcard);
                    if (externalCount > 1) {
                        item.mDisplayName = TextUtils.concat(item.mDisplayName, " (" + externalCount + ")");
                    }
                    break;

                case USB:
                    usbCount++;
                    item.mIcon = iconUsb;
                    item.mDisplayName = activity.getText(R.string.storage_usb);
                    if (usbCount > 1) {
                        item.mDisplayName = TextUtils.concat(item.mDisplayName, " (" + usbCount + ")");
                    }
                    break;

                case INTERNAL:
                default:
                    internalCount++;
                    item.mIcon = iconStorage;
                    item.mDisplayName = activity.getText(R.string.storage_internal);
                    if (internalCount > 1) {
                        item.mDisplayName = TextUtils.concat(item.mDisplayName, " (" + internalCount + ")");
                    }
                    break;
            }
            item.mDisplayPath = v.file.getAbsolutePath();
            items.add(item);
        }

        for (final String bookmark : Settings.getBookmarks(activity)) {
            final BookmarkItem item = new BookmarkItem();
            item.mDisplayName = FilenameUtils.getName(bookmark);
            if (item.mDisplayName.equals(Environment.rootDirectory.getAbsolutePath())) {
                item.mDisplayName = activity.getText(R.string.root);
            }
            item.mDisplayPath = bookmark;
            item.mIcon = iconUser;
            items.add(item);
        }
        return items;
    }

    /**
     * Returns start of user bookmarks index in List returned by #getAllBookmarks(Activity)
     * 
     * @return start of user bookmarks
     */
    public static int getUserBookmarkOffset() {
        return Environment.getStorageVolumes().size();
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
}
