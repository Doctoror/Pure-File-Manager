package com.docd.purefm.file;

import android.os.FileObserver;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class FileObserverCache {

    private static FileObserverCache instance;

    @NotNull
    public static FileObserverCache getInstance() {
        if (instance == null) {
            instance = new FileObserverCache();
        }
        return instance;
    }

    private final Map<String, WeakReference<MultiListenerFileObserver>> cache;

    private FileObserverCache() {
        this.cache = new HashMap<String, WeakReference<MultiListenerFileObserver>>();
    }

    public void clear() {
        this.cache.clear();
    }

    @NotNull
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
