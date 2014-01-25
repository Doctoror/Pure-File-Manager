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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

/**
 * Can draw two drawables
 *
 * @author Doctoror
 */
public class OverlayImageView extends ImageView {

    private Drawable overlay;
    private boolean drawOverlay;

    public OverlayImageView(Context context) {
        super(context);
        this.drawOverlay = true;
    }

    public OverlayImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.drawOverlay = true;
    }

    public OverlayImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.drawOverlay = true;
    }

    public final void setOverlay(final Drawable overlay) {
        if (this.overlay != overlay) {
            this.overlay = overlay;
            if (this.drawOverlay) {
                this.invalidate();
            }
        }
    }

    protected final void setDrawOverlay(final boolean draw) {
        if (this.drawOverlay != draw) {
            this.drawOverlay = draw;
            this.invalidate();
        }
    }

    @Override
    protected void onDraw(@NotNull final Canvas c) {
        super.onDraw(c);
        final Drawable drawable = this.getDrawable();
        if (drawOverlay && drawable != null && overlay != null) {

            final Rect bounds = drawable.getBounds();
            final int drawableWidth = bounds.right - bounds.left;
            final int drawableHeight = bounds.bottom - bounds.top;
            if (drawableWidth == 0 || drawableHeight == 0) {
                return;     // nothing to draw (empty bounds)
            }

            final int saveCount = c.getSaveCount();
            c.save();

            if (this.getCropToPaddingCompat()) {
                final int scrollX = this.getScrollX();
                final int scrollY = this.getScrollY();
                c.clipRect(scrollX + this.getPaddingLeft(), scrollY + this.getPaddingTop(),
                        scrollX + this.getRight() - this.getLeft() - this.getPaddingLeft(),
                        scrollY + this.getBottom() - this.getTop() - this.getPaddingBottom());
            }

            c.translate(this.getPaddingLeft(), this.getPaddingTop());

            c.concat(this.getImageMatrix());
            overlay.setBounds(bounds);
            overlay.draw(c);
            c.restoreToCount(saveCount);
        }
    }

    @SuppressLint("NewApi")
    public final boolean getCropToPaddingCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return this.getCropToPadding();
        } else {
            return false;
        }
    }
}
