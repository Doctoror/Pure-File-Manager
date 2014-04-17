package com.docd.purefm.utils;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

import android.support.annotation.NonNull;

public final class ThemeUtils {
    private ThemeUtils() {}

    public static int getInteger(@NonNull final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getInteger(0, 0);
        } finally {
            array.recycle();
        }
    }

    public static float getDimension(final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDimension(0, 0f);
        } finally {
            array.recycle();
        }
    }

    public static Drawable getDrawable(final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }

    @NonNull
    public static Drawable getDrawable(final ContextThemeWrapper themeWrapper, final int attr) {
        final TypedArray array = themeWrapper.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }
}
