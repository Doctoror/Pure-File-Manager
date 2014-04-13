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

import com.docd.purefm.utils.PFMFileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Map<String, WeakReference<MultiListenerFileObserver>> mCache = new HashMap<>();

    private FileObserverCache() {

    }

    public void clear() {
        this.mCache.clear();
    }

    @Nullable
    public MultiListenerFileObserver get(@NotNull final GenericFile path) {
        return mCache.get(PFMFileUtils.fullPath(path)).get();
    }

    @NotNull
    public MultiListenerFileObserver getOrCreate(@NotNull final GenericFile file, final int events) {
        final String path = PFMFileUtils.fullPath(file);
        final WeakReference<MultiListenerFileObserver> reference = mCache.get(path);
        MultiListenerFileObserver observer;
        if (reference != null && (observer = reference.get()) != null) {
            return observer;
        } else {
            observer = new MultiListenerFileObserver(path, events);
            this.mCache.put(path, new WeakReference<>(observer));
        }
        return observer;
    }
}
