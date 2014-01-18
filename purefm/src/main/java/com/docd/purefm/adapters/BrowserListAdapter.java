package com.docd.purefm.adapters;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.view.OverlayRecyclingImageView;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class BrowserListAdapter extends BrowserBaseAdapter {
    
    public BrowserListAdapter(Activity context) {
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
            v = this.inflater
                    .inflate(R.layout.list_item_file, null);
            h = new Holder();
            h.icon = (OverlayRecyclingImageView) v.findViewById(android.R.id.icon);
            h.title = (TextView) v.findViewById(android.R.id.title);
            h.date = (TextView) v.findViewById(android.R.id.text1);
            h.perm = (TextView) v.findViewById(android.R.id.text2);
            h.size = (TextView) v.findViewById(android.R.id.summary);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final GenericFile f = this.files.get(pos);
        h.icon.setImageResource(f.isDirectory() ? R.drawable.ic_fso_folder : R.drawable.ic_fso_default);
        this.applyOverlay(f, h.icon);

        if (Settings.showPreviews) {
            h.icon.setTag(f);
            try {
                this.executor.submit(new Job(f, h.icon));
            } catch (Exception e) {
            }
        }
        h.title.setText(f.getName());
        h.date.setText(Settings.showLastModified ? f.humanReadableLastModified() : "");
        h.size.setText(f.isDirectory() || !Settings.showSize ? "" : f.humanReadableLength());
        h.perm.setText(Settings.showPermissions ? f.getPermissions().toString() : "");

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
