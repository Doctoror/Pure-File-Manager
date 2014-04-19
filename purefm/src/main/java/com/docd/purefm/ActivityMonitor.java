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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public final class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    private static ActivityMonitor sInstance;

    static void init(@NonNull final Application application) {
        if (sInstance != null) {
            throw new IllegalStateException("ActivityMonitor is already initialized");
        }
        sInstance = new ActivityMonitor(application);
    }

    @NonNull
    public static ActivityMonitor getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        return sInstance;
    }

    public interface ActivityMonitorListener {
        
        void onAtLeastOneActivityStarted();

        void onAllActivitiesStopped();
    }

    private final Collection<WeakReference<ActivityMonitorListener>>
            mListeners = new LinkedList<>();

    private int mActivityStartedCount;

    public ActivityMonitor(@NonNull final Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    public synchronized void registerActivityMonitorListener(@NonNull final ActivityMonitorListener l) {
        final Collection<WeakReference<ActivityMonitorListener>> toRemove = new HashSet<>();
        try {
            for (final WeakReference<ActivityMonitorListener> registeredRef : mListeners) {
                final ActivityMonitorListener registered = registeredRef.get();
                if (registered == null) {
                    toRemove.add(registeredRef);
                } else if (registered == l) {
                    //already registered
                    return;
                }
            }
        } finally {
            mListeners.removeAll(toRemove);
        }
        mListeners.add(new WeakReference<>(l));
    }

    public synchronized void unregisterActivityMonitorListener(@NonNull final ActivityMonitorListener l) {
        final Collection<WeakReference<ActivityMonitorListener>> toRemove = new HashSet<>();
        for (final WeakReference<ActivityMonitorListener> registeredRef : mListeners) {
            final ActivityMonitorListener registered = registeredRef.get();
            if (registered == null || registered == l) {
                toRemove.add(registeredRef);
            }
        }
        mListeners.removeAll(toRemove);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++mActivityStartedCount == 1) {
            final Collection<WeakReference<ActivityMonitorListener>> toRemove = new HashSet<>();
            for (final WeakReference<ActivityMonitorListener> registeredRef : mListeners) {
                final ActivityMonitorListener l = registeredRef.get();
                if (l != null) {
                    l.onAtLeastOneActivityStarted();
                } else {
                    toRemove.add(registeredRef);
                }
            }
            mListeners.removeAll(toRemove);
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
        if (--mActivityStartedCount == 0) {
            final Collection<WeakReference<ActivityMonitorListener>> toRemove = new HashSet<>();
            for (final WeakReference<ActivityMonitorListener> registeredRef : mListeners) {
                final ActivityMonitorListener l = registeredRef.get();
                if (l != null) {
                    l.onAllActivitiesStopped();
                } else {
                    toRemove.add(registeredRef);
                }
            }
            mListeners.removeAll(toRemove);

        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
