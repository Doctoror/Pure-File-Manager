package com.docd.purefm.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;

public final class DashSpan implements LeadingMarginSpan {
    
    private static final String SPAN = "- ";
    
    private int margin;

    public DashSpan() {
        this.margin = -1;
    }
    
    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top,
            int baseline, int bottom, CharSequence text, int start, int end,
            boolean first, Layout layout)
    {
        if (first) {
            if (this.margin == -1) {
                this.margin = (int) p.measureText(SPAN);
            }
            c.drawText(SPAN, x + dir, baseline, p);
        }
    }

    @Override
    public int getLeadingMargin(boolean first)
    {
        return margin;
    }
}