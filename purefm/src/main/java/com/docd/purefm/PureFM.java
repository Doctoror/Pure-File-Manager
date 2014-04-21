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
import com.docd.purefm.utils.PFMTextUtils;
import com.stericson.RootTools.RootTools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

public final class PureFM extends Application implements
        Application.ActivityLifecycleCallbacks {

    public static final int THEME_ID_LIGHT = 1;
    public static final int THEME_ID_DARK = 2;

    private int mActivityStartedCount;

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                     .detectAll()
                     .penaltyLog()
                     .build());
            StrictMode.setVmPolicy(buildStrictModeVmPolicy().build());
        }
        super.onCreate();
        ActivityMonitor.init(this);
        RootTools.handlerEnabled = false;
        RootTools.debugMode = BuildConfig.DEBUG;
        Environment.init(this);
        PFMTextUtils.init(this);
        ensureNoShellUsedIfNoBusybox();
        registerActivityLifecycleCallbacks(this);
    }

    private void ensureNoShellUsedIfNoBusybox() {
        final Settings settings = Settings.getInstance(this);
        if (settings.useCommandLine()) {
            if (!Environment.hasBusybox()) {
                settings.setUseCommandLine(false, true);
                if (settings.isSuEnabled()) {
                    settings.setSuEnabled(false, true);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++mActivityStartedCount == 1) {
            //rescan for environment changes
            ensureNoShellUsedIfNoBusybox();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityStartedCount--;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private static StrictMode.VmPolicy.Builder buildStrictModeVmPolicy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return StrictModeVmPolicyBuilder_JB_MR2.build();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return StrictModeVmPolicyBuilder_JB.build();
        }
        return StrictModeVmPolicyBuilder.build();
    }

    private static final class StrictModeVmPolicyBuilder {
        static StrictMode.VmPolicy.Builder build() {
            return new StrictMode.VmPolicy.Builder()
                    .penaltyLog()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static final class StrictModeVmPolicyBuilder_JB {
        static StrictMode.VmPolicy.Builder build() {
            return StrictModeVmPolicyBuilder.build().detectLeakedRegistrationObjects();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static final class StrictModeVmPolicyBuilder_JB_MR2 {
        static StrictMode.VmPolicy.Builder build() {
            return StrictModeVmPolicyBuilder_JB.build().detectFileUriExposure();
        }
    }
}
