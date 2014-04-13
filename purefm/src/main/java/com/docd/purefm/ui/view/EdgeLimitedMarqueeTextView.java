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
package com.docd.purefm.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * TextView uses it's own edge-limited implementation of Marquee
 *
 * @author Doctoror
 */
public class EdgeLimitedMarqueeTextView extends TextView {

    private EdgeLimitedMarquee mMarquee;

    @SuppressWarnings("unused")
    public EdgeLimitedMarqueeTextView(Context context) {
        super(context);
        this.init();
    }

    @SuppressWarnings("unused")
    public EdgeLimitedMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    @SuppressWarnings("unused")
    public EdgeLimitedMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    /**
     * Called by constructor to init this View
     */
    private void init() {
        this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.setHorizontallyScrolling(true);
        this.mMarquee = new EdgeLimitedMarquee(this);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.invalidateMarquee();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.invalidateMarquee();
    }

    public void invalidateMarquee() {
        final Layout layout = getLayout();
        if (layout != null) {
            final int lineWidth = (int) layout.getLineWidth(0);
            final boolean canMove = getWidth() < lineWidth;
            if (canMove) {
                mMarquee.setMaxScrollX(Math.abs(lineWidth - getWidth()));
                mMarquee.restart();
            } else {
                mMarquee.stop();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.invalidateMarquee();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mMarquee.stop();
    }

    @Override
    protected void onDraw(@NotNull final Canvas canvas) {
        super.onDraw(canvas);
        this.mMarquee.sendEmptyMessageDelayed(EdgeLimitedMarquee.MESSAGE_TICK,
                EdgeLimitedMarquee.MARQUEE_RESOLUTION);
    }

    /*
     * Copyright (C) 2006 The Android Open Source Project
     * Copyright (C) 2014 Yaroslav Mytkalyk
     *
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
    private static final class EdgeLimitedMarquee extends Handler {
        private static final int MARQUEE_RESOLUTION = 1000 / 30;
        private static final int MARQUEE_PIXELS_PER_SECOND = 30;

        private static final byte MARQUEE_STOPPED = 0x0;
        private static final byte MARQUEE_RUNNING = 0x2;

        private static final int MESSAGE_TICK = 0x2;

        private final WeakReference<TextView> mView;

        private byte mStatus = MARQUEE_STOPPED;
        private final int mScrollUnit;
        private final int mMinScrollX = 0;
        private int mMaxScrollX;

        private int mScroll;
        private int mScrollUpdate;

        EdgeLimitedMarquee(final TextView view) {
            final Resources res = view.getResources();
            if (res == null) {
                throw new IllegalArgumentException("No Resources for view");
            }
            final float density = res.getDisplayMetrics().density;
            mScrollUnit = Math.round(MARQUEE_PIXELS_PER_SECOND * density) / MARQUEE_RESOLUTION;
            mView = new WeakReference<>(view);
        }

        void setMaxScrollX(final int maxScrollX) {
            mScroll = 0;
            mScrollUpdate = mScrollUnit;
            mMaxScrollX = maxScrollX;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TICK:
                    this.tick();
                    break;
            }
        }

        void tick() {
            if (mStatus != MARQUEE_RUNNING) {
                return;
            }

            removeMessages(MESSAGE_TICK);

            final TextView textView = mView.get();
            if (textView != null) {
                if (mScroll >= mMaxScrollX) {
                    mScrollUpdate = -mScrollUnit;
                } else if (mScroll < mMinScrollX) {
                    mScrollUpdate = mScrollUnit;
                }
                mScroll += mScrollUpdate;
                textView.setScrollX(mScroll);
            }
        }

        void stop() {
            this.mStatus = MARQUEE_STOPPED;
            this.removeMessages(MESSAGE_TICK);
            this.resetScroll();
        }

        void start() {
            this.removeMessages(MESSAGE_TICK);
            this.mStatus = MARQUEE_RUNNING;
            this.tick();
        }

        void restart() {
            this.stop();
            this.start();
        }

        private void resetScroll() {
            mScroll = 0;
            mScrollUpdate = mScrollUnit;
            final TextView textView = mView.get();
            if (textView != null) {
                textView.setScrollX(0);
            }
        }
    }
}
