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
package com.docd.purefm;

import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.PureFMTextUtils;
import com.stericson.RootTools.RootTools;

import android.app.Application;

public final class PureFM extends Application implements ActivityMonitor.OnActivitiesOpenedListener {

    public static final int THEME_ID_LIGHT = 1;
    public static final int THEME_ID_DARK = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        RootTools.handlerEnabled = false;
        RootTools.debugMode = BuildConfig.DEBUG;
        ActivityMonitor.init(this);
        Environment.init(this);
        Settings.init(this, this.getResources());
        PreviewHolder.initialize(this);
        PureFMTextUtils.init(this);
        ensureNoShellUsedIfNoBusybox();
        ActivityMonitor.addOnActivitiesOpenedListener(this);
    }

    private void ensureNoShellUsedIfNoBusybox() {
        if (Settings.useCommandLine) {
            if (!Environment.hasBusybox()) {
                Settings.setUseCommandLine(this, false);
                if (Settings.su) {
                    Settings.setAllowRoot(this, false);
                }
            }
        }
    }

    @Override
    public void onActivitiesStarted() {
        //rescan for environment changes
        Environment.init(this);
        ensureNoShellUsedIfNoBusybox();
    }

    @Override
    public void onActivitiesStopped() {

    }

    @Override
    public void onActivitiesCreated() {

    }

    @Override
    public void onActivitiesDestroyed() {

    }
}
