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

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public final class SettingsFragment extends PreferenceFragment {

    private static final String[] THEMES_VALUES = new String[] {
            Integer.toString(R.style.ThemeLight),
            Integer.toString(R.style.ThemeDark)
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

    private void init() {
        final SettingsActivity parent = (SettingsActivity) getActivity();
        
        final Preference perm = findPreference(getString(R.string.key_preference_show_permissions));
        perm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showPermissions = ((Boolean) newValue).booleanValue();
                parent.notifyNeedInvalidate();
                return true;
            }
        });
        
        final Preference hid = findPreference(getString(R.string.key_preference_show_hidden));
        hid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showHidden = ((Boolean) newValue).booleanValue();
                parent.notifyNeedInvalidate();
                return true;
            }
        });

        final Preference prev = findPreference(getString(R.string.key_preference_show_preview));
        prev.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showPreviews = ((Boolean) newValue).booleanValue();
                parent.notifyNeedInvalidate();
                return true;
            }
        });

        final Preference size = findPreference(getString(R.string.key_preference_show_size));
        size.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showSize = ((Boolean) newValue).booleanValue();
                parent.notifyNeedInvalidate();
                return true;
            }
        });

        final Preference modif = findPreference(getString(R.string.key_preference_show_modified));
        modif.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.showLastModified = ((Boolean) newValue).booleanValue();
                parent.notifyNeedInvalidate();
                return true;
            }
        });

        final ListPreference theme = (ListPreference) findPreference(getString(R.string.key_preference_theme));
        theme.setEntryValues(THEMES_VALUES);
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Settings.theme = Integer.parseInt((String) newValue);
                Toast.makeText(getActivity(), R.string.theme_change_warning, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        final Preference appear = findPreference(getString(R.string.key_preference_appearance));
        appear.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.appearance = Integer.parseInt((String) newValue);
                perm.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                size.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                modif.setEnabled(Settings.appearance == Settings.APPEARANCE_LIST);
                parent.notifyNeedInvalidate();
                return true;
            }
        });
        
        final CheckBoxPreference command = (CheckBoxPreference) findPreference(getString(R.string.key_preference_use_commandline));
        command.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.useCommandLine = ((Boolean) newValue).booleanValue();
                if (!Settings.useCommandLine) {
                    if (Settings.su) {
                        final CheckBoxPreference root = (CheckBoxPreference) findPreference(getString(R.string.key_preference_allow_root));
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
        
        final Preference root = findPreference(getString(R.string.key_preference_allow_root));
        root.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                Settings.su = ((Boolean) newValue).booleanValue();
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
        root.setEnabled(Environment.hasRoot && Environment.hasBusybox());
        
        final Set<String> options = Settings.getBookmarks(parent.getApplicationContext());
        final String defaultHome = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        options.add(defaultHome);
        
        final ListPreference home = (ListPreference) this.findPreference(getString(R.string.key_preference_home_directory));
        home.setSummary(Settings.getHomeDirectory(parent));
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
        
        
//        if (!options.isEmpty()) {
//            final PreferenceCategory startup = new PreferenceCategory(parent);
//            startup.setTitle(R.string.preference_category_startup);
//            
//            final PreferenceScreen ps = this.getPreferenceScreen();
//            ps.addPreference(startup);
//            
//            final String defaultHome = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//            options.add(defaultHome);
//
//            final ListPreference home = new ListPreference(parent);
//            home.setTitle(R.string.preference_home_directory);
//            home.setKey(getString(R.string.key_preference_home_directory));
//            home.setDialogTitle(R.string.menu_bookmarks);
//            home.setSummary(Settings.getHomeDirectory(parent));
//            
//            final CharSequence[] opts = new CharSequence[options.size()];
//            options.toArray(opts);
//            home.setEntries(opts);
//            home.setEntryValues(opts);
//            home.setDefaultValue(defaultHome);
//            home.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    preference.setSummary((CharSequence) newValue);
//                    return true;
//                }
//            });
//            
//            startup.addPreference(home);
//
//        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!Settings.useCommandLine || this.wasAllowRoot != Settings.su) {
            ShellHolder.releaseShell();
        }
    }
}
