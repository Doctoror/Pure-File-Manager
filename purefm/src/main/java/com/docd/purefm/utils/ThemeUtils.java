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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;

import android.support.annotation.NonNull;

/**
 * Provides methods for obtaining styled attributes
 */
public final class ThemeUtils {
    private ThemeUtils() {}

    /**
     * Returns an int for attr from the {@link Resources.Theme}
     *
     * @param theme {@link Resources.Theme} to get int from
     * @param attr Attribute of the int
     * @return dimension for attr from the {@link Resources.Theme}
     */
    public static int getInteger(@NonNull final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getInteger(0, 0);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns an int for attr from the {@link Resources.Theme}
     *
     * @param theme {@link Resources.Theme} to get int from
     * @param attr Attribute of the int
     * @param defaultValue value to return if not found
     * @return dimension for attr from the {@link Resources.Theme}
     */
    public static int getInteger(@NonNull final Resources.Theme theme,
                                 final int attr,
                                 final int defaultValue) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getInteger(0, defaultValue);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns a dimension for attr from the {@link Resources.Theme}
     *
     * @param theme {@link Resources.Theme} to get dimension from
     * @param attr Attribute of the dimension
     * @return dimension for attr from the {@link Resources.Theme}
     */
    public static float getDimension(@NonNull final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDimension(0, 0f);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns a {@link Drawable} for attr from the {@link Resources.Theme},
     * or null if not found
     *
     * @param theme {@link Resources.Theme} to get {@link Drawable} from
     * @param attr Attribute of the {@link Drawable}
     * @return {@link Drawable} for attr from the {@link Resources.Theme}
     */
    @Nullable
    public static Drawable getDrawable(@NonNull final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns a {@link Drawable} for attr from the {@link Resources.Theme}.
     *
     * @param theme {@link Resources.Theme} to get {@link Drawable} from
     * @param attr Attribute of the {@link Drawable}
     * @throws RuntimeException if the {@link Drawable} is not found
     * @return {@link Drawable} for attr from the  {@link Resources.Theme}
     */
    @NonNull
    public static Drawable getDrawableNonNull(@NonNull final Resources.Theme theme,
                                              final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            final Drawable drawable = array.getDrawable(0);
            if (drawable == null) {
                throw new RuntimeException(
                        "Drawable for attr \'" + attr + "\' not found in " + theme);
            }
            return drawable;
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns a {@link Drawable} for attr from the {@link ContextThemeWrapper}
     * Throws {@link RuntimeException} if the {@link Drawable} not found
     *
     * @param themeWrapper {@link ContextThemeWrapper} to get {@link Drawable} from
     * @param attr Attribute of the {@link Drawable}
     * @throws RuntimeException if the {@link Drawable} is not found
     * @return {@link Drawable} for attr from the {@link ContextThemeWrapper}
     */
    @NonNull
    public static Drawable getDrawableNonNull(@NonNull final ContextThemeWrapper themeWrapper,
                                              final int attr) {
        return getDrawableNonNull(themeWrapper.getTheme(), attr);
    }
}
