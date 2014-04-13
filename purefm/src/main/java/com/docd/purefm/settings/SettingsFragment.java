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

import org.jetbrains.annotations.NotNull;

public final class SettingsFragment extends PreferenceFragment {

    private static final String[] THEMES_VALUES = new String[] {
            Integer.toString(R.style.ThemeDark),
            Integer.toString(R.style.ThemeLight)
    };

    private boolean wasAllowRoot;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SettingsActivity)) {
            throw new RuntimeException("Should be attached only to SettingsActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.settings);
        this.wasAllowRoot = Settings.su;
        this.init();
    }

    @NotNull
    private SettingsActivity getSettingsActivity() {
        final SettingsActivity parent = (SettingsActivity) getActivity();
        if (parent == null) {
            throw new IllegalStateException("getSettingsActivity() is called when the Fragment is not attached");
        }
        return parent;
    }

    private void init() {
        final Resources res = getSettingsActivity().getResources();
        final Preference perm = findPreference(res.getString(R.string.key_preference_show_permissions));
        if (perm == null) {
            throw new RuntimeException("Show permissions preference not found");
        }
        perm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showPermissions = (Boolean) newValue;
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });
        
        final Preference hid = findPreference(res.getString(R.string.key_preference_show_hidden));
        if (hid == null) {
            throw new RuntimeException("Show hidden files preference not found");
        }
        hid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showHidden = (Boolean) newValue;
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference prev = findPreference(res.getString(R.string.key_preference_show_preview));
        if (prev == null) {
            throw new RuntimeException("Show previews preference not found");
        }
        prev.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showPreviews = (Boolean) newValue;
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference size = findPreference(res.getString(R.string.key_preference_show_size));
        if (size == null) {
            throw new RuntimeException("Show file sizes preference not found");
        }
        size.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showSize = (Boolean) newValue;
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final Preference modif = findPreference(res.getString(R.string.key_preference_show_modified));
        if (modif == null) {
            throw new RuntimeException("Show modified date preference not found");
        }
        modif.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showLastModified = (Boolean) newValue;
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });

        final ListPreference theme = (ListPreference) findPreference(res.getString(R.string.key_preference_theme));
        if (theme == null) {
            throw new RuntimeException("Theme preference not found");
        }
        theme.setEntryValues(THEMES_VALUES);
        theme.setValue(String.valueOf(Settings.theme));
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final int chosenTheme = Integer.parseInt((String) newValue);
                if (chosenTheme != Settings.theme) {
                    Settings.theme = chosenTheme;
                    getSettingsActivity().proxyRestart();
                    return true;
                }
                return false;
            }
        });

        final Preference appear = findPreference(res.getString(R.string.key_preference_appearance));
        if (appear == null) {
            throw new RuntimeException("Appearance preference not found");
        }
        appear.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.appearance = Integer.parseInt((String) newValue);
                perm.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                size.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                modif.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                getSettingsActivity().notifyNeedInvalidate();
                return true;
            }
        });
        
        final CheckBoxPreference command = (CheckBoxPreference) findPreference(
                res.getString(R.string.key_preference_use_commandline));
        if (command == null) {
            throw new RuntimeException("Use command line preference not found");
        }
        command.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.useCommandLine = (Boolean) newValue;
                final SettingsActivity parent = getSettingsActivity();
                if (!Settings.useCommandLine) {
                    if (Settings.su) {
                        final CheckBoxPreference root = (CheckBoxPreference) findPreference(
                                res.getString(R.string.key_preference_allow_root));
                        if (root == null) {
                            throw new RuntimeException("Allow root preference not found");
                        }
                        root.setChecked(false);
                        Settings.su = false;
                        Settings.setAllowRoot(parent, false);
                    }
                }
                parent.proxyInvalidateActionBarIcon();
                parent.notifyNeedInvalidate();
                return true;
            }
        });
        
        final Preference root = findPreference(res.getString(R.string.key_preference_allow_root));
        if (root == null) {
            throw new RuntimeException("Allow root preference not found");
        }
        root.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.su = (Boolean) newValue;
                final SettingsActivity parent = getSettingsActivity();
                if (Settings.su) {
                    if (!Settings.useCommandLine) {
                        command.setChecked(true);
                        Settings.setUseCommandLine(parent, true);
                        Settings.useCommandLine = true;
                    }
                }
                
                command.setEnabled(!Settings.su);
                parent.proxyInvalidateActionBarIcon();
                parent.notifyNeedInvalidate();
                return true;
            }
        });

        perm.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
        size.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
        modif.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
        
        command.setEnabled(Environment.hasBusybox());
        root.setEnabled(Environment.sHasRoot && Environment.hasBusybox());

        final Context appContext = getSettingsActivity().getApplicationContext();
        if (appContext == null) {
            throw new RuntimeException("Application context of attached SettingsActivity is null");
        }

        final Set<String> options = BookmarksHelper.getAllLocations(appContext);
        final String defaultHome = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        options.add(defaultHome);
        
        final ListPreference home = (ListPreference) this.findPreference(
                res.getString(R.string.key_preference_home_directory));
        if (home == null) {
            throw new RuntimeException("Home directory preference not found");
        }
        home.setSummary(Settings.getHomeDirectory(appContext));
        final CharSequence[] opts = new CharSequence[options.size()];
        options.toArray(opts);
        home.setEntries(opts);
        home.setEntryValues(opts);
        home.setDefaultValue(defaultHome);
        home.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((CharSequence) newValue);
                return true;
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!Settings.useCommandLine || this.wasAllowRoot != Settings.su) {
            ShellHolder.releaseShell();
        }
    }
}
