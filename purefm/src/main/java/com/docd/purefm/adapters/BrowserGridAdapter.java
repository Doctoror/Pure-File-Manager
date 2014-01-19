package com.docd.purefm.adapters;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.view.OverlayRecyclingImageView;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class BrowserGridAdapter extends BrowserBaseAdapter {

    public BrowserGridAdapter(Activity context) {
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
            h.icon = (OverlayRecyclingImageView) v.findViewById(android.R.id.icon);
            h.title = (TextView) v.findViewById(android.R.id.title);
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
        
        return v;
    }
    
    private static final class Holder {
        
        private Holder() {}
        
        private OverlayRecyclingImageView icon;
        private TextView title;
    }

}
