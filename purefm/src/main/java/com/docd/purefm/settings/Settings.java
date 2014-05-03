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
package com.docd.purefm.settings;

import java.util.HashSet;
import java.util.Set;

import com.docd.purefm.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

public final class Settings {
    
    private static final String KEY_BOOKMARKS = "purefm.settings.keys.bookmarks";

    public enum ListAppearance {
        LIST, GRID
    }

    public enum Theme {
        DARK(R.style.ThemeDark), LIGHT(R.style.ThemeLight);

        @StyleRes
        public final int resId;

        private Theme(final int resId) {
            this.resId = resId;
        }
    }

    private static final Object LOCK = new Object();

    private static Settings sInstance;

    public static synchronized Settings getInstance(@NonNull final Context context) {
        synchronized (LOCK) {
            if (sInstance == null) {
                //noinspection ConstantConditions
                sInstance = new Settings(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    public static Settings getInstance() {
        synchronized (LOCK) {
            if (sInstance == null) {
                throw new IllegalStateException(
                        "Settings was not yet initialized. Call getInstance(Context) first");
            }
            return sInstance;
        }
    }

    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    private Theme mTheme;
    private ListAppearance mListAppearance;
    private boolean mListShowFileSize;
    private boolean mListShowHiddenFiles;
    private boolean mListShowPermissions;
    private boolean mListShowPreviews;
    private boolean mListShowModifiedDate;
    private boolean mUseCommandLine;
    private boolean mSuEnabled;
    private String mHomeDirectory;
    private Set<String> mBookmarks;
    
    private Settings(@NonNull final Context context) {
        final Resources res = context.getResources();
        mResources = res;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mTheme = Theme.valueOf(mSharedPreferences.getString(
                res.getString(R.string.key_preference_theme), Theme.DARK.name()));

        mListAppearance = ListAppearance.valueOf(mSharedPreferences.getString(
                res.getString(R.string.key_preference_list_appearance), ListAppearance.LIST.name()));

        mListShowHiddenFiles = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_list_show_hidden_files), false);

        mListShowFileSize = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_list_show_size), true);

        mListShowPermissions = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_list_show_permissions), true);

        mListShowPreviews = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_list_show_preview), true);

        mListShowModifiedDate = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_list_show_modified_date), true);

        mUseCommandLine = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_use_commandline), false);

        mSuEnabled = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_allow_superuser), false);

        mHomeDirectory = mSharedPreferences.getString(
                res.getString(R.string.key_preference_home_directory),
                        android.os.Environment.getExternalStorageDirectory().getAbsolutePath());

        mBookmarks = mSharedPreferences.getStringSet(KEY_BOOKMARKS, new HashSet<String>());
    }
    
    public void setBookmarks(@NonNull final Set<String> bookmarks) {
        mBookmarks = bookmarks;
        mSharedPreferences.edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply();
    }

    public void setTheme(final Theme theme, final boolean update) {
        mTheme = theme;
        if (update) {
            mSharedPreferences.edit().putString(
                    mResources.getString(R.string.key_preference_theme), theme.name()).apply();
        }
    }

    public Theme getTheme() {
        return mTheme;
    }

    public void setListAppearance(final ListAppearance appearance) {
        mListAppearance = appearance;
        mSharedPreferences.edit().putString(
                mResources.getString(R.string.key_preference_list_appearance),
                appearance.name()).apply();
    }

    public ListAppearance getListAppearance() {
        return mListAppearance;
    }

    public void setListShowFileSize(final boolean show, final boolean update) {
        mListShowFileSize = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_list_show_size), show).apply();
        }
    }

    public boolean listShowFileSizeEnabled() {
        return mListShowFileSize;
    }

    public void setListShowHiddenFiles(final boolean show, final boolean update) {
        mListShowHiddenFiles = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_list_show_hidden_files),
                            show).apply();
        }
    }

    public boolean listShowHiddenFilesEnabled() {
        return mListShowHiddenFiles;
    }

    public void setListShowPermissions(final boolean show, final boolean update) {
        mListShowPermissions = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_list_show_permissions),
                            show).apply();
        }
    }

    public boolean listShowPermissionsEnabled() {
        return mListShowPermissions;
    }

    public void setListShowPreviews(final boolean show, final boolean update) {
        mListShowPreviews = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_list_show_preview),
                            show).apply();
        }
    }

    public boolean listShowPreviewsEnabled() {
        return mListShowPreviews;
    }

    public void setListShowModifiedDate(final boolean show, final boolean update) {
        mListShowModifiedDate = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_list_show_modified_date),
                            show).apply();
        }
    }

    public boolean listShowModifiedDateEnabled() {
        return mListShowModifiedDate;
    }

    public void setUseCommandLine(final boolean use, final boolean update) {
        mUseCommandLine = use;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_use_commandline), use).apply();
        }
    }

    public boolean useCommandLine() {
        return mUseCommandLine;
    }

    public void setSuEnabled(final boolean enabled, final boolean update) {
        mSuEnabled = enabled;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_allow_superuser), enabled).apply();
        }
    }

    public boolean isSuEnabled() {
        return mSuEnabled;
    }

    public void setHomeDirectory(final String path, final boolean update) {
        mHomeDirectory = path;
        if (update) {
            mSharedPreferences.edit().putString(
                    mResources.getString(R.string.key_preference_home_directory), path).apply();
        }
    }

    public String getHomeDirectory() {
        return mHomeDirectory;
    }

    public Set<String> getBookmarks() {
        return mBookmarks;
    }
}
