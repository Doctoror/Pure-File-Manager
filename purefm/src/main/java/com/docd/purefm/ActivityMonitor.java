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

public final class ActivityMonitor
{
    private static final long DELAY_CLOSED = 300;
    
    private static Handler handler;
    private static volatile int opened;
    
    public interface OnActivitiesOpenedListener {
        void onActivitiesOpen();
        void onActivitiesClosed();
    }

    private static final Object listenersLock = new Object();
    private static Set<OnActivitiesOpenedListener> listeners;
    
    static {
        listeners = new HashSet<OnActivitiesOpenedListener>();
    }
    
    public static void addOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        synchronized (listenersLock) {
            listeners.add(l);
        }
    }
    
    public static void removeOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        synchronized (listenersLock) {
            listeners.remove(l);
        }
    }
    
    public static void init(final Context context) {
        handler = new Handler();
    }
    
    public static boolean areActivitiesOpened() {
        return opened != 0;
    }
    
    private static final Runnable onActivitiesClosedRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (listenersLock) {
                for (final OnActivitiesOpenedListener listener : listeners) {
                    listener.onActivitiesClosed();
                }
            }
        }
    };
    
    public static synchronized void onStart(Activity a) {
        if (handler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        handler.removeCallbacks(onActivitiesClosedRunnable);
        opened++;
        if (opened == 1) {
            synchronized (listenersLock) {
                for (final OnActivitiesOpenedListener listener : listeners) {
                    listener.onActivitiesOpen();
                }
            }
        }
    }
    
    public static synchronized void onStop(Activity a) {
        if (handler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        handler.removeCallbacks(onActivitiesClosedRunnable);
        opened--;
        if (opened < 0) {
            throw new IllegalStateException("Number of opened activities is less than zero. Check whether you call onStart in all activities");
        }
        if (opened == 0) {
            handler.postDelayed(onActivitiesClosedRunnable, DELAY_CLOSED);
        }
    }
}