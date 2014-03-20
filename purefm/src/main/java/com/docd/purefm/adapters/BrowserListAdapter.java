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
package com.docd.purefm.adapters;

import com.docd.purefm.R;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PureFMTextUtils;
import com.docd.purefm.ui.view.OverlayImageView;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * Browser adapter for ListView
 * @author Doctoror
 */
public final class BrowserListAdapter extends BrowserBaseAdapter {

    private final Typeface mTypefaceMonospace;
    private final LongSparseArray<String> mHumanReadableLastModified;
    private final LongSparseArray<String> mHumanReadableLength;
    
    public BrowserListAdapter(@NotNull final Activity context) {
        super(context);
        mTypefaceMonospace = Typeface.createFromAsset(context.getAssets(), "DroidSansMono.ttf");
        mHumanReadableLastModified = new LongSparseArray<>();
        mHumanReadableLength = new LongSparseArray<>();
    }

    @Override
    public View getView(int pos, View v, ViewGroup arg2) {
        if (pos >= this.getCount()) {
            //workaround for indexOutOfBoundsException when deleting from adapter
            // and notifyDataSetChanged is not yet called
            return v;
        }
        
        Holder h;
        
        if (v == null) {
            v = this.mLayoutInflater
                    .inflate(R.layout.list_item_file, null);
            h = new Holder();
            h.mIcon = (OverlayImageView) v.findViewById(android.R.id.icon);
            h.mTitle = (TextView) v.findViewById(android.R.id.title);
            h.mDate = (TextView) v.findViewById(android.R.id.text1);
            h.mPerm = (TextView) v.findViewById(android.R.id.text2);
            h.mSize = (TextView) v.findViewById(R.id.size);

            h.mDate.setTypeface(this.mTypefaceMonospace);
            h.mPerm.setTypeface(this.mTypefaceMonospace);
            h.mSize.setTypeface(this.mTypefaceMonospace);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final GenericFile f = this.getItem(pos);
        this.setIcon(f, h.mIcon);
        this.applyOverlay(f, h.mIcon);

        if (Settings.showPreviews) {
            loadPreview(f, h.mIcon);
        }
        h.mTitle.setText(f.getName());

        if (Settings.showLastModified) {
            final long lastModified = f.lastModified();
            String humanReadableLastModified = mHumanReadableLastModified.get(lastModified);
            if (humanReadableLastModified == null) {
                humanReadableLastModified = PureFMTextUtils.humanReadableDate(
                        lastModified,
                        f instanceof CommandLineFile);
                mHumanReadableLastModified.put(lastModified, humanReadableLastModified);
            }
            h.mDate.setText(humanReadableLastModified);
        } else {
            h.mDate.setText(null);
        }

        if (Settings.showSize && !f.isDirectory()) {
            final long fileSize = f.length();
            String humanReadableFileSize = mHumanReadableLength.get(fileSize);
            if (humanReadableFileSize == null) {
                humanReadableFileSize = PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(fileSize));
                mHumanReadableLength.put(fileSize, humanReadableFileSize);
            }
            h.mSize.setText(humanReadableFileSize);
        } else {
            h.mSize.setText(null);
        }

        if (Settings.showPermissions) {
            h.mPerm.setText(f.getPermissions().toString());
        } else {
            h.mPerm.setText(null);
        }

        return v;
    }
    
    private static final class Holder {
        
        Holder() {}
        
        OverlayImageView mIcon;
        TextView mTitle;
        TextView mSize;
        TextView mDate;
        TextView mPerm;
        
    }

}
