package com.docd.purefm.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

import com.docd.purefm.R;

import org.jetbrains.annotations.NotNull;

public final class ThemeUtils {
    private ThemeUtils() {}

    public static int getBrowserGridColumns(@NotNull final Resources.Theme theme,
                                            @NotNull final Configuration config) {
        final TypedArray array = theme.obtainStyledAttributes(new int[] {
                config.orientation == Configuration.ORIENTATION_PORTRAIT ?
                R.attr.browserGridColumnsPort : R.attr.browserGridColumnsLand
        });
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

    @NotNull
    public static Drawable getDrawable(final ContextThemeWrapper themeWrapper, final int attr) {
        final TypedArray array = themeWrapper.obtainStyledAttributes(new int[] {attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }
}
