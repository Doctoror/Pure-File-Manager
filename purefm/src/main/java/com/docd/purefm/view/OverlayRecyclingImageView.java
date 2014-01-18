package com.docd.purefm.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import org.jetbrains.annotations.NotNull;

public class OverlayRecyclingImageView extends RecyclingImageView {

    private Drawable overlay;
    private boolean drawOverlay;

    public OverlayRecyclingImageView(Context context) {
        super(context);
        this.drawOverlay = true;
    }

    public OverlayRecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.drawOverlay = true;
    }

    public OverlayRecyclingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.drawOverlay = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.performAttach(this.overlay);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.performDetach(this.overlay);
    }

    public final void setOverlay(final Drawable overlay) {
        if (this.overlay != overlay) {
            this.onDrawableAdded(this.overlay, overlay);
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
