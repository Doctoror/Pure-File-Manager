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
package com.docd.purefm.file;

import android.os.FileObserver;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public final class MultiListenerFileObserver extends FileObserver {

    public interface OnEventListener {
        void onEvent(int event, String path);
    }

    private final Set<OnEventListener> mListeners = new HashSet<>();

    @NonNull
    private final String mPath;

    private int watchCount;

    public MultiListenerFileObserver(@NonNull final String path, final int mask) {
        super(path, mask);
        this.mPath = path;
    }

    public void addOnEventListener(final OnEventListener listener) {
        this.mListeners.add(listener);
    }

    public void removeOnEventListener(final OnEventListener listener) {
        this.mListeners.remove(listener);
    }

    @NonNull
    public String getPath() {
        return this.mPath;
    }

    @Override
    public void onEvent(final int event, final String pathStub) {
        for (final OnEventListener listener : this.mListeners) {
            listener.onEvent(event, this.mPath);
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
