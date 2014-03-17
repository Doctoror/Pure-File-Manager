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

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public final class ActivityMonitor
{
    private static final long DELAY_CLOSED = 300;
    
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static volatile int sCreated;
    private static volatile int sStarted;
    
    public interface OnActivitiesOpenedListener {
        void onActivitiesCreated();
        void onActivitiesStarted();
        void onActivitiesStopped();
        void onActivitiesDestroyed();
    }

    private static final Object LISTENERS_LOCK = new Object();
    private static Set<OnActivitiesOpenedListener> sListeners;
    
    static {
        sListeners = new HashSet<>();
    }
    
    public static void addOnActivitiesOpenedListener(
            @NotNull final OnActivitiesOpenedListener l) {
        synchronized (LISTENERS_LOCK) {
            sListeners.add(l);
        }
    }
    
    public static void removeOnActivitiesOpenedListener(
            @NotNull final OnActivitiesOpenedListener l) {
        synchronized (LISTENERS_LOCK) {
            sListeners.remove(l);
        }
    }
    
    private static final Runnable sOnActivitiesStoppedRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (LISTENERS_LOCK) {
                if (BuildConfig.DEBUG) {
                    Log.d("ActivityMonitor", "Activities stopped");
                }
                for (final OnActivitiesOpenedListener listener : sListeners) {
                    listener.onActivitiesStopped();
                }
            }
        }
    };

    private static final Runnable sOnActivitiesDestroyedRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (LISTENERS_LOCK) {
                if (BuildConfig.DEBUG) {
                    Log.d("ActivityMonitor", "Activities destroyed");
                }
                for (final OnActivitiesOpenedListener listener : sListeners) {
                    listener.onActivitiesDestroyed();
                }
            }
        }
    };
    
    public static synchronized void onStart(Activity a) {
        HANDLER.removeCallbacks(sOnActivitiesStoppedRunnable);
        sStarted++;
        if (sStarted == 1) {
            synchronized (LISTENERS_LOCK) {
                if (BuildConfig.DEBUG) {
                    Log.d("ActivityMonitor", "Activities started");
                }
                for (final OnActivitiesOpenedListener listener : sListeners) {
                    listener.onActivitiesStarted();
                }
            }
        }
    }
    
    public static synchronized void onStop(Activity a) {
        HANDLER.removeCallbacks(sOnActivitiesStoppedRunnable);
        sStarted--;
        if (sStarted < 0) {
            throw new IllegalStateException("Number of opened activities is less than zero. Check whether you call ActivityMonitor.onStart in all activities");
        }
        if (sStarted == 0) {
            HANDLER.postDelayed(sOnActivitiesStoppedRunnable, DELAY_CLOSED);
        }
    }

    public static synchronized void onCreate(Activity a) {
        HANDLER.removeCallbacks(sOnActivitiesDestroyedRunnable);
        sCreated++;
        if (sCreated == 1) {
            synchronized (LISTENERS_LOCK) {
                if (BuildConfig.DEBUG) {
                    Log.d("ActivityMonitor", "Activities created");
                }
                for (final OnActivitiesOpenedListener listener : sListeners) {
                    listener.onActivitiesCreated();
                }
            }
        }
    }

    public static synchronized void onDestroy(Activity a) {
        HANDLER.removeCallbacks(sOnActivitiesDestroyedRunnable);
        sCreated--;
        if (sCreated < 0) {
            throw new IllegalStateException("Number of created activities is less than zero. Check whether you call ActivityMonitor.onCreate in all activities");
        }
        if (sCreated == 0) {
            HANDLER.postDelayed(sOnActivitiesDestroyedRunnable, DELAY_CLOSED);
        }
    }
}