package com.docd.purefm.adapters;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public final class BrowserGridAdapter extends BrowserBaseAdapter {

    public BrowserGridAdapter(Activity context) {
        super(context);
    }

    @Override
    public View getView(int pos, View v, ViewGroup arg2) {
        Holder h;
        
        if (v == null) {
            v = this.inflater
                    .inflate(R.layout.grid_item_file, null);
            h = new Holder();
            h.icon = (ImageView) v.findViewById(android.R.id.icon);
            h.overlay = (ImageView) v.findViewById(android.R.id.icon1);
            h.title = (TextView) v.findViewById(android.R.id.title);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final GenericFile f = this.files.get(pos);
        h.icon.setImageResource(f.isDirectory() ? R.drawable.ic_fso_folder : R.drawable.ic_fso_default);
        this.applyOverlay(f, h.overlay);
        
        if (Settings.showPreviews) {
            h.icon.setTag(f);
            try {
                this.executor.submit(new Job(f, h.icon, h.overlay));
            } catch (Exception e) {
            }
        }
        h.title.setText(f.getName());
        
        return v;
    }
    
    private static final class Holder {
        
        private Holder() {}
        
        private ImageView icon;
        private ImageView overlay;
        private TextView title;
    }

}
