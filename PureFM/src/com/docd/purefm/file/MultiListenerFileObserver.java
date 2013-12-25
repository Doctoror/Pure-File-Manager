package com.docd.purefm.file;

import android.os.FileObserver;

import java.util.HashSet;
import java.util.Set;

public final class MultiListenerFileObserver extends FileObserver {

    public interface OnEventListener {
        void onEvent(int event, String path);
    }

    private final Set<OnEventListener> listeners;

    private int watchCount;

    public MultiListenerFileObserver(String path, int mask) {
        super(path, mask);
        this.listeners = new HashSet<OnEventListener>();
    }

    public void addOnEventListener(final OnEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeOnEventListener(final OnEventListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void onEvent(final int event, final String path) {
        for (final OnEventListener listener : this.listeners) {
            listener.onEvent(event, path);
        }
    }

    @Override
    public synchronized void startWatching() {
        super.startWatching();
        this.watchCount++;
    }

    @Override
    public synchronized void stopWatching() {
        if (--this.watchCount <= 0) {
            if (this.watchCount < 0) {
                this.watchCount = 0;
            }
            super.stopWatching();
        }
    }
}
