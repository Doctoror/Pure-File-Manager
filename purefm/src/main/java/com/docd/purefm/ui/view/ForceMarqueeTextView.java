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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView that forces {@link android.text.TextUtils.TruncateAt} MARQUEE ellipsize by setting it and returning true in {@link #isFocused()}
 *
 * @author Doctoror
 */
public class ForceMarqueeTextView extends TextView {

    public ForceMarqueeTextView(Context context) {
        super(context);
        this.init();
    }

    public ForceMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ForceMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    /**
     * Called by constructor to init this View
     */
    private void init() {
        this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.setMarqueeRepeatLimit(-1);
    }

    /**
     * Returns true to force marquee without focus
     *
     * @return always true
     */
    @Override
    public boolean isFocused() {
        return true;
    }
}
