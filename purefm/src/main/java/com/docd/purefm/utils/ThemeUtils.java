package com.docd.purefm.utils;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

public final class ThemeUtils {
    private ThemeUtils() {}

    public static Drawable getDrawable(final Resources.Theme theme, final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }

    public static Drawable getDrawable(final ContextThemeWrapper themeWrapper, final int attr) {
        final TypedArray array = themeWrapper.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }
}
