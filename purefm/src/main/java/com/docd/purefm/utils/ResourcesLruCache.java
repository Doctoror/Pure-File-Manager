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
package com.docd.purefm.utils;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import org.jetbrains.annotations.Nullable;

public final class ResourcesLruCache extends LruCache<Integer, Drawable> {

    private static ResourcesLruCache instance;

    public static synchronized ResourcesLruCache getInstance(final Resources res) {
        if (instance == null) {
            instance = new ResourcesLruCache(res);
        }
        return instance;
    }

    private final Resources mResources;

    private ResourcesLruCache(final Resources res) {
        super(512 * 1024);
        this.mResources = res;
    }

    @Nullable
    @Override
    protected Drawable create(final Integer key) {
        return this.mResources.getDrawable(key);
    }

    @Override
    protected int sizeOf(Integer key, Drawable value) {
        if (value instanceof BitmapDrawable) {
            return ((BitmapDrawable) value).getBitmap().getByteCount() / 1024;
        } else {
            return super.sizeOf(key, value);
        }
    }
}
