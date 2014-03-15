package com.docd.purefm.services;

import android.app.Service;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Similar to IntentService, but can execute two tasks at a time
 */
public abstract class MultiWorkerService extends Service {

    private ExecutorService mExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newFixedThreadPool(2);
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
