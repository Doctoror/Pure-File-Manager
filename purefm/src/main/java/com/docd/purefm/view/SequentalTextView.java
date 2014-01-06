package com.docd.purefm.view;

import java.io.File;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class SequentalTextView extends TextView {

    private static final String SEQUENCE_SEPARATOR = " > ";
    private static final String ROOT = "root";
    
    public interface OnSequenceClickListener {
        void onSequenceClick(String sequence);
    }
    
    private final Runnable SCROLL_RIGHT = new Runnable() {
        @Override
        public void run() {
            parent.fullScroll(View.FOCUS_RIGHT);
        }
    };
    
    private OnSequenceClickListener listener;
    private HorizontalScrollView parent;
    
    public SequentalTextView(Context context)
    {
        super(context);
    }

    public SequentalTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SequentalTextView(Context context, AttributeSet attrs,
            int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    public final void setFile(File path) {
        final String[] dirs = path.getPath().split(File.separator);
        final StringBuilder p = new StringBuilder(ROOT);
        for (int i = 1; i < dirs.length; i++) {
            p.append(SEQUENCE_SEPARATOR);
            p.append(dirs[i]);
        }
        this.setText(p.toString());
        if (this.parent == null) {
            final ViewParent parent = this.getParent();
            if (parent == null || !(parent instanceof HorizontalScrollView)) {
                throw new RuntimeException("SequentalTextView must have HorizontalScrollView parent");
            }
            this.parent = (HorizontalScrollView) parent;
        }
        this.parent.postDelayed(SCROLL_RIGHT, 100L);
    }
    
    public final void setOnSequenceClickListener(OnSequenceClickListener l) {
        this.listener = l;
    }
    
    public final void fullScrollRight() {
        this.parent.fullScroll(View.FOCUS_RIGHT);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Layout layout = this.getLayout();
            if (layout != null && this.listener != null) {
                int line = layout.getLineForVertical(y);
                int offset = layout.getOffsetForHorizontal(line, x);
                this.fireListenerEvent(offset);
                return true;
            }
        }

        return super.onTouchEvent(event);
    }
    
    private void fireListenerEvent(final int offset) {
        final CharSequence text = this.getText();
        if (text == null) {
            throw new IllegalStateException("fireListenerEvent called, but text is not set");
        }
        final String[] parts = text.toString().split(SEQUENCE_SEPARATOR);
        int length = ROOT.length() + SEQUENCE_SEPARATOR.length() - 2;
        final StringBuilder path = new StringBuilder();
        path.append(File.separatorChar);
        for (int i = 1; i < parts.length && length < offset; i++) {
            path.append(parts[i]);
            path.append(File.separatorChar);
            length += parts[i].length() + SEQUENCE_SEPARATOR.length();
        }
        this.listener.onSequenceClick(path.toString());
    }
    
}
