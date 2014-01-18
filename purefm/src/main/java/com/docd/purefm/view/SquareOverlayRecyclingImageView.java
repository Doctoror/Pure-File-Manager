package com.docd.purefm.view;

import android.content.Context;
import android.util.AttributeSet;

public final class SquareOverlayRecyclingImageView extends OverlayRecyclingImageView {

    public SquareOverlayRecyclingImageView(Context context) {
        super(context);
    }

    public SquareOverlayRecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareOverlayRecyclingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
