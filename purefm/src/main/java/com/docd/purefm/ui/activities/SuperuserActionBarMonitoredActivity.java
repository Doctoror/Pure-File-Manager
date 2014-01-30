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
package com.docd.purefm.ui.activities;

import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.docd.purefm.R;
import com.docd.purefm.settings.Settings;

/**
 * Manages ActionBar icon. If superuser is enabled, the ActionBar icon is ic_superuser, default otherwise
 */
public abstract class SuperuserActionBarMonitoredActivity extends MonitoredActivity {

    /**
     * Current ActionBar
     */
    private ActionBar mActionBar;

    /**
     * Default Activity icon
     */
    private Drawable mDefaultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = this.getActionBar();
        try {
            mDefaultIcon = getPackageManager().getActivityIcon(this.getComponentName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mDefaultIcon = getResources().getDrawable(R.drawable.ic_fso_folder);
        }
        this.invalidateActionBarIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.invalidateActionBarIcon();
    }

    /**
     * Sets ActionBar icon to ic_superuser if superuser enabled.
     */
    protected final void invalidateActionBarIcon() {
        if (Settings.su) {
            mActionBar.setIcon(R.drawable.icon_root);
        } else if (Settings.useCommandLine) {
            mActionBar.setIcon(R.drawable.icon_shell);
        } else {
            mActionBar.setIcon(mDefaultIcon);
        }
    }
}
