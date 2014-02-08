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
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public final class ActivityMonitor
{
    private static final long DELAY_CLOSED = 300;
    
    private static Handler sHandler;
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
        sListeners = new HashSet<OnActivitiesOpenedListener>();
    }
    
    public static void addOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        synchronized (LISTENERS_LOCK) {
            sListeners.add(l);
        }
    }
    
    public static void removeOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        synchronized (LISTENERS_LOCK) {
            sListeners.remove(l);
        }
    }
    
    public static void init(final Context context) {
        sHandler = new Handler(context.getMainLooper());
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
        if (sHandler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        sHandler.removeCallbacks(sOnActivitiesStoppedRunnable);
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
        if (sHandler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        sHandler.removeCallbacks(sOnActivitiesStoppedRunnable);
        sStarted--;
        if (sStarted < 0) {
            throw new IllegalStateException("Number of opened activities is less than zero. Check whether you call ActivityMonitor.onStart in all activities");
        }
        if (sStarted == 0) {
            sHandler.postDelayed(sOnActivitiesStoppedRunnable, DELAY_CLOSED);
        }
    }

    public static synchronized void onCreate(Activity a) {
        if (sHandler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        sHandler.removeCallbacks(sOnActivitiesDestroyedRunnable);
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
        if (sHandler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        sHandler.removeCallbacks(sOnActivitiesDestroyedRunnable);
        sCreated--;
        if (sCreated < 0) {
            throw new IllegalStateException("Number of created activities is less than zero. Check whether you call ActivityMonitor.onCreate in all activities");
        }
        if (sCreated == 0) {
            sHandler.postDelayed(sOnActivitiesDestroyedRunnable, DELAY_CLOSED);
        }
    }
}