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
import com.docd.purefm.ui.view.OverlayImageView;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.support.annotation.NonNull;

/**
 * Browser adapter for GridView
 * @author Doctoror
 */
public final class BrowserGridAdapter extends BrowserBaseAdapter {

    public BrowserGridAdapter(@NonNull final Activity context) {
        super(context);
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
            v = this.mLayoutInflater.inflate(R.layout.grid_item_file, null);
            h = new Holder();
            h.mIcon = (OverlayImageView) v.findViewById(android.R.id.icon);
            h.mTitle = (TextView) v.findViewById(android.R.id.title);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final GenericFile f = this.getItem(pos);
        h.mIcon.setImageResource(f.isDirectory() ? R.drawable.ic_fso_folder :
                R.drawable.ic_fso_default);
        this.applyOverlay(f, h.mIcon);
        
        if (Settings.showPreviews) {
            loadPreview(f, h.mIcon);
        }
        h.mTitle.setText(f.getName());
        
        return v;
    }
    
    private static final class Holder {
        
        Holder() {}
        
        OverlayImageView mIcon;
        TextView mTitle;
    }

}
