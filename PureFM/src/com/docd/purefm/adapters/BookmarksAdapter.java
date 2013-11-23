package com.docd.purefm.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.docd.purefm.R;
import com.docd.purefm.activities.BrowserActivity;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PureFMFileUtils;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public final class BookmarksAdapter implements ListAdapter {

    private final List<String> bookmarks;
    private final DataSetObservable mDataSetObservable;
    private final LayoutInflater inflater;
    private final BrowserActivity activity;
    
    private final Set<String> data;
    
    private boolean modified;
    
    public BookmarksAdapter(final BrowserActivity activity, final Set<String> data) {
        this.activity = activity;
        this.mDataSetObservable = new DataSetObservable();
        this.inflater = activity.getLayoutInflater();
        this.bookmarks = new ArrayList<String>();
        this.data = data;
        for (String b : data) {
            this.bookmarks.add(b);
        }
    }
    
    public void addItem(String path) {
        if (data.contains(path)) {
            Toast.makeText(activity, R.string.bookmark_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!data.add(path)) {
            Toast.makeText(activity, R.string.bookmark_not_added, Toast.LENGTH_SHORT).show();
            return;
        }
        this.bookmarks.add(path);
        this.modified = true;
        this.notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return this.bookmarks.size();
    }

    @Override
    public String getItem(int arg0) {
        return this.bookmarks.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getItemViewType(int arg0) {
        return 0;
    }

    @Override
    public View getView(int pos, View v, ViewGroup arg2) {
        
        Holder h;
        
        if (v == null) {
            v = this.inflater.inflate(R.layout.list_item_bookmark, null); 
            h = new Holder();
            h.title = (TextView) v.findViewById(android.R.id.title);
            h.summary = (TextView) v.findViewById(android.R.id.summary);
            h.remove = v.findViewById(android.R.id.button1);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final String cur = this.bookmarks.get(pos);
        
        v.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                final GenericFile path = PureFMFileUtils.newFile(cur);
                activity.setCurrentPath(path);
            }
        });
        
        h.title.setText(FilenameUtils.getName(cur));
        h.summary.setText(cur);
        h.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                bookmarks.remove(cur);
                data.remove(cur);
                modified = true;
                notifyDataSetChanged();
            }
        });
        
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.bookmarks.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver arg0) {
        this.mDataSetObservable.registerObserver(arg0);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver arg0) {
        this.mDataSetObservable.unregisterObserver(arg0);
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    private void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int arg0) {
        return true;
    }
    
    public boolean isModified() {
        return this.modified;
    }
    
    public Set<String> getData() {
        return this.data;
    }
    
    private static final class Holder {
        private TextView title;
        private TextView summary;
        private View remove;
    }

}
