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
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

public final class Settings {
    
    private static final String KEY_BOOKMARKS = "purefm.settings.keys.bookmarks";
    
    public static final int APPEARANCE_LIST = 0;
    public static final int APPEARANCE_GRID = 1;

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

    private int mTheme;
    private int mAppearance;
    private boolean mShowSize;
    private boolean mShowHidden;
    private boolean mShowPermissions;
    private boolean mShowPreviews;
    private boolean mShowLastModified;
    private boolean mUseCommandLine;
    private boolean mSuEnabled;
    private String mHomeDirectory;
    private Set<String> mBookmarks;
    
    private Settings(@NonNull final Context context) {
        final Resources res = context.getResources();
        mResources = res;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mTheme = Integer.parseInt(mSharedPreferences.getString(
                res.getString(R.string.key_preference_theme),
                Integer.toString(R.style.ThemeDark)));

        mAppearance = Integer.parseInt(mSharedPreferences.getString(
                res.getString(R.string.key_preference_appearance),
                Integer.toString(APPEARANCE_LIST)));

        mShowHidden = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_show_hidden), false);

        mShowSize = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_show_size), true);

        mShowPermissions = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_show_permissions), true);

        mShowPreviews = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_show_preview), true);

        mShowLastModified = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_show_modified), true);

        mUseCommandLine = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_use_commandline), false);

        mSuEnabled = mSharedPreferences.getBoolean(
                res.getString(R.string.key_preference_allow_root), false);

        mHomeDirectory = mSharedPreferences.getString(
                res.getString(R.string.key_preference_home_directory),
                        android.os.Environment.getExternalStorageDirectory().getAbsolutePath());

        mBookmarks = mSharedPreferences.getStringSet(KEY_BOOKMARKS, new HashSet<String>());
    }
    
    public void setBookmarks(@NonNull final Set<String> bookmarks) {
        mBookmarks = bookmarks;
        final Editor e = mSharedPreferences.edit();
        e.putStringSet(KEY_BOOKMARKS, bookmarks);
        e.apply();
    }

    public void setTheme(final int theme, final boolean update) {
        mTheme = theme;
        if (update) {
            mSharedPreferences.edit().putInt(
                    mResources.getString(R.string.key_preference_theme), theme).apply();
        }
    }

    @StyleRes
    public int getTheme() {
        return mTheme;
    }

    public void setAppearance(final int appearance) {
        mAppearance = appearance;
        mSharedPreferences.edit().putInt(
                mResources.getString(R.string.key_preference_appearance), appearance).apply();
    }

    public int getAppearance() {
        return mAppearance;
    }

    public void setShowSize(final boolean show, final boolean update) {
        mShowSize = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_show_size), show).apply();
        }
    }

    public boolean showSize() {
        return mShowSize;
    }

    public void setShowHiddenFiles(final boolean show, final boolean update) {
        mShowHidden = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_show_hidden), show).apply();
        }
    }

    public boolean showHidden() {
        return mShowHidden;
    }

    public void setShowPermissions(final boolean show, final boolean update) {
        mShowPermissions = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_show_permissions), show).apply();
        }
    }

    public boolean showPermissions() {
        return mShowPermissions;
    }

    public void setShowPreviews(final boolean show, final boolean update) {
        mShowPreviews = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_show_preview), show).apply();
        }
    }

    public boolean showPreviews() {
        return mShowPreviews;
    }

    public void setShowLastModified(final boolean show, final boolean update) {
        mShowLastModified = show;
        if (update) {
            mSharedPreferences.edit().putBoolean(
                    mResources.getString(R.string.key_preference_show_modified), show).apply();
        }
    }

    public boolean showLastModified() {
        return mShowLastModified;
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
                    mResources.getString(R.string.key_preference_allow_root), enabled).apply();
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
