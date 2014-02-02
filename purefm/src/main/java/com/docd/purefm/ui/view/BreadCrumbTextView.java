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

import java.io.File;

import android.content.Context;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

/**
 * TextView that builds and shows clickable bread crumb links
 * Don't use {@link #setText(char[], int, int)} method to build view.
 * Instead, use {@link #setFile(java.io.File)}
 *
 * @author Doctoror
 */
public class BreadCrumbTextView extends TextView {

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

    private OnSequenceClickListener mOnSequenceClickListener;
    private HorizontalScrollView parent;

    public BreadCrumbTextView(Context context) {
        super(context);
        init();
    }

    public BreadCrumbTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BreadCrumbTextView(Context context, AttributeSet attrs,
                              int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public final void setFile(final File file) {
        setText(formatFilePath(file));
        if (this.parent == null) {
            final ViewParent parent = this.getParent();
            if (parent == null || !(parent instanceof HorizontalScrollView)) {
                throw new RuntimeException("BreadCrumbTextView must have HorizontalScrollView parent");
            }
            this.parent = (HorizontalScrollView) parent;
        }
        this.parent.postDelayed(SCROLL_RIGHT, 100L);
    }

    private CharSequence formatFilePath(final File file) {
        final SpannableStringBuilder formatted = new SpannableStringBuilder(ROOT + file.getPath());
        final int length = formatted.length();
        if (formatted.charAt(length - 1) == File.separatorChar) {
            formatted.delete(length - 1, length);
        }
        int prevIndex = 0;
        int index;
        do {
            String currentlyFormattedString = formatted.toString();
            index = currentlyFormattedString.indexOf(File.separatorChar, prevIndex);
            if (index == -1) {
                formatted.setSpan(new NavigationSpan(toPathForListener(currentlyFormattedString.substring(0, formatted.length()))), prevIndex, formatted.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                formatted.replace(index, index + 1, SEQUENCE_SEPARATOR);
                currentlyFormattedString = formatted.toString();

                formatted.setSpan(new NavigationSpan(toPathForListener(currentlyFormattedString.substring(0, index))), prevIndex, index, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                prevIndex = index + SEQUENCE_SEPARATOR.length();
            }
        } while (index != -1);
        return formatted;
    }

    private static String toPathForListener(final String withSequences) {
        return withSequences.replace(SEQUENCE_SEPARATOR, File.separator);
    }

    public final void setOnSequenceClickListener(OnSequenceClickListener l) {
        this.mOnSequenceClickListener = l;
    }

    public final void fullScrollRight() {
        if (this.parent != null) {
            this.parent.fullScroll(View.FOCUS_RIGHT);
        }
    }

    private final class NavigationSpan extends ClickableSpan
            implements ParcelableSpan {

        private final String mPath;

        NavigationSpan(final String path) {
            // remove artificial "root" prefix
            if (path.equals(ROOT)) {
                this.mPath = File.separator;
            } else {
                this.mPath = path.substring(ROOT.length(), path.length());
            }
        }

        @Override
        public void onClick(final View widget) {
            mOnSequenceClickListener.onSequenceClick(this.mPath);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPath);
        }

        @Override
        public int getSpanTypeId() {
            return 666;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
