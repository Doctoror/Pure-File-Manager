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
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PureFMTextUtils;
import com.docd.purefm.view.OverlayRecyclingImageView;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Browser adapter for ListView
 * @author Doctoror
 */
public final class BrowserListAdapter extends BrowserBaseAdapter {

    private final Typeface mTypefaceMonospace;
    private final HashMap<Long, String> mHumanReadableLastModified;
    private final HashMap<Long, String> mHumanReadableLength;
    
    public BrowserListAdapter(Activity context) {
        super(context);
        mTypefaceMonospace = Typeface.createFromAsset(context.getAssets(), "DroidSansMono.ttf");
        this.mHumanReadableLastModified = new HashMap<Long, String>();
        this.mHumanReadableLength = new HashMap<Long, String>();
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
            h.icon = (OverlayRecyclingImageView) v.findViewById(android.R.id.icon);
            h.title = (TextView) v.findViewById(android.R.id.title);
            h.date = (TextView) v.findViewById(android.R.id.text1);
            h.perm = (TextView) v.findViewById(android.R.id.text2);
            h.size = (TextView) v.findViewById(R.id.size);

            h.date.setTypeface(this.mTypefaceMonospace);
            h.perm.setTypeface(this.mTypefaceMonospace);
            h.size.setTypeface(this.mTypefaceMonospace);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final GenericFile f = this.getItem(pos);
        h.icon.setImageResource(f.isDirectory() ? R.drawable.ic_fso_folder : R.drawable.ic_fso_default);
        this.applyOverlay(f, h.icon);

        if (Settings.showPreviews) {
            loadPreview(f, h.icon);
        }
        h.title.setText(f.getName());

        if (Settings.showLastModified) {
            final long lastModified = f.lastModified();
            String humanReadableLastModified = mHumanReadableLastModified.get(lastModified);
            if (humanReadableLastModified == null) {
                humanReadableLastModified = PureFMTextUtils.humanReadableDate(lastModified);
                mHumanReadableLastModified.put(lastModified, humanReadableLastModified);
            }
            h.date.setText(humanReadableLastModified);
        } else {
            h.date.setText("");
        }

        if (Settings.showSize && !f.isDirectory()) {
            final long fileSize = f.length();
            String humanReadableFileSize = mHumanReadableLength.get(fileSize);
            if (humanReadableFileSize == null) {
                humanReadableFileSize = PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(fileSize));
                mHumanReadableLength.put(fileSize, humanReadableFileSize);
            }
            h.size.setText(humanReadableFileSize);
        } else {
            h.size.setText("");
        }

        if (Settings.showPermissions) {
            h.perm.setText(f.getPermissions().toString());
        } else {
            h.perm.setText("");
        }

        return v;
    }
    
    protected static final class Holder {
        
        private Holder() {}
        
        protected OverlayRecyclingImageView icon;
        protected TextView title;
        protected TextView size;
        protected TextView date;
        protected TextView perm;
        
    }

}
