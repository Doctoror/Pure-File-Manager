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
package com.docd.purefm.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.docd.purefm.drawable.RecyclingBitmapDrawable;

import org.jetbrains.annotations.Nullable;

/**
 * @author Doctoror
 * ImageView that manages isDisplayed for RecyclingBitmapDrawable
 */
public class RecyclingImageView extends ImageView {

    private boolean isAttachedToWindow;

    public RecyclingImageView(Context context) {
        super(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        this.onDrawableAdded(this.getDrawable(), drawable);
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.isAttachedToWindow = true;
        this.performAttach(this.getDrawable());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedToWindow = false;
        this.performDetach(this.getDrawable());
    }

    protected final boolean isAttachedToWindowCompat() {
        return this.isAttachedToWindow;
    }

    protected final void performAttach(@Nullable final Drawable drawable) {
        if (drawable != null && drawable instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(true);
        }
    }

    protected final void performDetach(@Nullable final Drawable drawable) {
        if (drawable != null && drawable instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(false);
        }
    }

    protected final void onDrawableAdded(@Nullable Drawable old, @Nullable final Drawable added) {
        if (old instanceof RecyclingBitmapDrawable && old != added && this.isAttachedToWindow) {
            ((RecyclingBitmapDrawable) old).setIsDisplayed(false);
        }
        if (added != null && added instanceof RecyclingBitmapDrawable && this.isAttachedToWindow) {
            ((RecyclingBitmapDrawable) added).setIsDisplayed(true);
        }
    }
}
