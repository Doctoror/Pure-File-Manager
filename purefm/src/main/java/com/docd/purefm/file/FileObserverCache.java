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
