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

import java.util.Set;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.utils.BookmarksHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import android.support.annotation.NonNull;

public final class SettingsFragment extends PreferenceFragment {

    private static final String[] THEMES_VALUES = new String[] {
            Settings.Theme.DARK.name(),
            Settings.Theme.LIGHT.name()
    };

    private Settings mSettings;
    private boolean mWasAllowRoot;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SettingsActivity)) {
            throw new RuntimeException("Should be attached only to SettingsActivity");
        }
        mSettings = Settings.getInstance(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.settings);
        this.mWasAllowRoot = mSettings.isSuEnabled();
        this.init();
    }

    @NonNull
    private SettingsActivity getSettingsActivity() {
        final SettingsActivity parent = (SettingsActivity) getActivity();
        if (parent == null) {
            throw new IllegalStateException("getSettingsActivity() is called when the Fragment is not attached");
        }
        return parent;
    }

    private void init() {
        final Resources res = getSettingsActivity().getResources();
        final Preference prefListShowPermissions = findPreference(res.getString(
                R.string.key_preference_list_show_permissions));
        if (prefListShowPermissions == null) {
            throw new RuntimeException("Show permissions preference not found");
        }
        prefListShowPermissions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                mSettings.setListShowPermissions((Boolean) newValue, false);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });
        
        final Preference prefListShowHiddenFiles = findPreference(res.getString(
                R.string.key_preference_list_show_hidden_files));
        if (prefListShowHiddenFiles == null) {
            throw new RuntimeException("Show hidden files preference not found");
        }
        prefListShowHiddenFiles.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                mSettings.setListShowHiddenFiles((Boolean) newValue, false);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference prefListShowPreviews = findPreference(res.getString(
                R.string.key_preference_list_show_preview));
        if (prefListShowPreviews == null) {
            throw new RuntimeException("Show previews preference not found");
        }
        prefListShowPreviews.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                mSettings.setListShowPreviews((Boolean) newValue, false);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference prefListShowFileSize = findPreference(res.getString(
                R.string.key_preference_list_show_size));
        if (prefListShowFileSize == null) {
            throw new RuntimeException("Show file sizes preference not found");
        }
        prefListShowFileSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                mSettings.setListShowFileSize((Boolean) newValue, false);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference prefListShowModifiedDate = findPreference(res.getString(
                R.string.key_preference_list_show_modified_date));
        if (prefListShowModifiedDate == null) {
            throw new RuntimeException("Show modified date preference not found");
        }
        prefListShowModifiedDate.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                mSettings.setListShowModifiedDate((Boolean) newValue, false);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final ListPreference prefTheme = (ListPreference) findPreference(res.getString(
                R.string.key_preference_theme));
        if (prefTheme == null) {
            throw new RuntimeException("Theme preference not found");
        }
        prefTheme.setEntryValues(THEMES_VALUES);
        prefTheme.setValue(String.valueOf(mSettings.getTheme()));
        prefTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                final Settings.Theme chosenTheme = Settings.Theme.valueOf((String) newValue);
                if (chosenTheme != mSettings.getTheme()) {
                    mSettings.setTheme(chosenTheme, false);
                    getSettingsActivity().proxyRestart();
                    return true;
                }
                return false;
            }
        });
        
        final CheckBoxPreference prefUseCommandline = (CheckBoxPreference) findPreference(
                res.getString(R.string.key_preference_use_commandline));
        if (prefUseCommandline == null) {
            throw new RuntimeException("Use command line preference not found");
        }
        prefUseCommandline.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                final boolean useCommandLine = (Boolean) newValue;
                mSettings.setUseCommandLine(useCommandLine, false);
                final SettingsActivity parent = getSettingsActivity();
                if (!useCommandLine) {
                    if (mSettings.isSuEnabled()) {
                        final CheckBoxPreference prefAllowSuperuser = (CheckBoxPreference) findPreference(
                                res.getString(R.string.key_preference_allow_superuser));
                        if (prefAllowSuperuser == null) {
                            throw new RuntimeException("Allow supersuer preference not found");
                        }
                        prefAllowSuperuser.setChecked(false);
                        mSettings.setSuEnabled(false, true);
                    }
                }
                parent.proxyInvalidateActionBarIcon();
                parent.notifyNeedInvalidate();
                return true;
            }
        });
        
        final Preference prefSuperuser = findPreference(res.getString(
                R.string.key_preference_allow_superuser));
        if (prefSuperuser == null) {
            throw new RuntimeException("Allow root preference not found");
        }
        prefSuperuser.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                final boolean suEnabled = (Boolean) newValue;
                mSettings.setSuEnabled(suEnabled, false);
                final SettingsActivity parent = getSettingsActivity();
                if (suEnabled) {
                    if (!mSettings.useCommandLine()) {
                        prefUseCommandline.setChecked(true);
                        mSettings.setUseCommandLine(true, true);
                    }
                }

                prefUseCommandline.setEnabled(!suEnabled);
                parent.proxyInvalidateActionBarIcon();
                parent.notifyNeedInvalidate();
                return true;
            }
        });
        
        prefUseCommandline.setEnabled(Environment.hasBusybox());
        prefSuperuser.setEnabled(Environment.sHasRoot && Environment.hasBusybox());

        final Context appContext = getSettingsActivity().getApplicationContext();
        if (appContext == null) {
            throw new RuntimeException("Application context of attached SettingsActivity is null");
        }

        final Set<String> options = BookmarksHelper.getAllLocations(appContext);
        final String defaultHome = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        options.add(defaultHome);
        
        final ListPreference prefHomeDirectory = (ListPreference) this.findPreference(
                res.getString(R.string.key_preference_home_directory));
        if (prefHomeDirectory == null) {
            throw new RuntimeException("Home directory preference not found");
        }
        prefHomeDirectory.setSummary(mSettings.getHomeDirectory());
        final CharSequence[] opts = new CharSequence[options.size()];
        options.toArray(opts);
        prefHomeDirectory.setEntries(opts);
        prefHomeDirectory.setEntryValues(opts);
        prefHomeDirectory.setDefaultValue(defaultHome);
        prefHomeDirectory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final CharSequence newPath = (CharSequence) newValue;
                preference.setSummary(newPath);
                mSettings.setHomeDirectory(newPath.toString(), false);
                return true;
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mSettings.useCommandLine() || mWasAllowRoot != mSettings.isSuEnabled()) {
            ShellHolder.releaseShell();
        }
    }
}
