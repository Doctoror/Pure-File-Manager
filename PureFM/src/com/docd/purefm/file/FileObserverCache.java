package com.docd.purefm.file;

import android.os.FileObserver;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class FileObserverCache {

    private final Map<String, WeakReference<MultiListenerFileObserver>> cache;

    public FileObserverCache() {
        this.cache = new HashMap<String, WeakReference<MultiListenerFileObserver>>();
    }

    public void clear() {
        this.cache.clear();
    }

    public MultiListenerFileObserver getOrCreate(final String path, final int events) {
        final WeakReference<MultiListenerFileObserver> reference = cache.get(path);
        MultiListenerFileObserver observer;
        if (reference != null && (observer = reference.get()) != null) {
            return observer;
        } else {
            observer = new MultiListenerFileObserver(path, events);
            this.cache.put(path, new WeakReference<MultiListenerFileObserver>(observer));
        }
        return observer;
    }
}
