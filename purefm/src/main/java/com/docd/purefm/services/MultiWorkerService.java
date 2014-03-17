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
package com.docd.purefm.services;

import android.app.Service;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Similar to IntentService, but can execute up to three tasks at parallel
 */
public abstract class MultiWorkerService extends Service {

    private ExecutorService mExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newFixedThreadPool(3);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mExecutors.submit(new HandleIntentRunnable(this, intent, startId));
        }
        return START_NOT_STICKY;
    }

    protected abstract void onHandleIntent(@NotNull final Intent intent);

    private static final class HandleIntentRunnable implements Runnable {

        private final WeakReference<MultiWorkerService> mServiceRef;
        private final int mStartId;
        private final Intent mIntent;

        HandleIntentRunnable(@NotNull final MultiWorkerService service,
                             @NotNull final Intent intent,
                             final int startId) {
            this.mServiceRef = new WeakReference<>(service);
            this.mIntent = intent;
            this.mStartId = startId;
        }

        @Override
        public void run() {
            final MultiWorkerService service = mServiceRef.get();
            if (service != null) {
                service.onHandleIntent(mIntent);
                service.stopSelf(mStartId);
            }
        }
    }
}
