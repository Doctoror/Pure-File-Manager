package com.docd.purefm.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.activities.BrowserActivity;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.BookmarksHelper;

import android.content.res.Resources;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ListAdapter of Bookmarks (Sliding drawer content)
 * @author Doctoror
 */
public final class BookmarksAdapter implements ListAdapter {
    private final List<String> bookmarks;
    
    private final DataSetObservable mDataSetObservable;
    private final LayoutInflater inflater;
    private final BrowserActivity activity;
    
    private boolean modified;
    
    private int userStart;
    
    private Drawable iconStorage;
    private Drawable iconSdcard;
    private Drawable iconUsb;
    private Drawable iconUser;
    
    private String rootDisplayName;
    
    public BookmarksAdapter(final BrowserActivity activity, final Set<String> user) {
        this.activity = activity;
        this.mDataSetObservable = new DataSetObservable();
        this.inflater = activity.getLayoutInflater();
        
        final Set<String> storageSet = BookmarksHelper.getStorageBookmarks();
        final Set<File> usb = Environment.getUsbStorageDirectories();
                
        this.bookmarks = new ArrayList<String>(user.size() + usb.size() + storageSet.size());
        this.bookmarks.addAll(storageSet);
        for (final File file : usb) {
            this.bookmarks.add(file.getAbsolutePath());
        }
        this.userStart = this.bookmarks.size();
        this.bookmarks.addAll(user);

        final Resources res = activity.getResources();
        this.iconStorage = res.getDrawable(R.drawable.holo_light_ic_storage);
        this.iconSdcard = res.getDrawable(R.drawable.holo_light_ic_sdcard);
        this.iconUsb = res.getDrawable(R.drawable.holo_light_ic_usb);
        this.iconUser = res.getDrawable(R.drawable.holo_light_ic_bookmark);
        this.rootDisplayName = res.getString(R.string.root);
    }
    
    public void addItem(String path) {
        if (bookmarks.contains(path)) {
            Toast.makeText(activity, R.string.bookmark_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bookmarks.add(path)) {
            Toast.makeText(activity, R.string.bookmark_not_added, Toast.LENGTH_SHORT).show();
            return;
        }        
        this.modified = true;
        this.notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return this.bookmarks.size();
    }

    @Override
    public String getItem(final int pos) {
        return this.bookmarks.get(pos);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getItemViewType(int arg0) {
        return arg0 < userStart ? 0 : 1;
    }

    @Override
    public View getView(int pos, View v, ViewGroup arg2) {
        
        final int viewType = this.getItemViewType(pos);
        Holder h;
        
        if (v == null) {
            if (viewType == 0) {
                v = this.inflater.inflate(R.layout.list_item_bookmark, null);
            } else {
                v = this.inflater.inflate(R.layout.list_item_bookmark_user, null);
            }
            h = new Holder();
            h.icon = (ImageView) v.findViewById(android.R.id.icon);
            h.title = (TextView) v.findViewById(android.R.id.title);
            h.summary = (TextView) v.findViewById(android.R.id.summary);
            h.remove = v.findViewById(android.R.id.button1);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }
        
        final String cur = this.getItem(pos);
        final String currentName = FilenameUtils.getName(cur);
        
        v.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                final GenericFile path = FileFactory.newFile(cur);
                activity.setCurrentPath(path);
            }
        });
        
        h.title.setText(currentName.equals(Environment.rootDirectory.getName()) ?
                this.rootDisplayName : currentName);

        h.summary.setText(cur);
        if (viewType == 1) {
            h.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    bookmarks.remove(cur);
                    modified = true;
                    notifyDataSetChanged();
                }
            });
        }
        
        switch (BookmarksHelper.getBookmarkType(currentName)) {
            case SDCARD:
                h.icon.setImageDrawable(iconSdcard);
                break;
            case STORAGE:
                h.icon.setImageDrawable(iconStorage);
                break;
            case USB:
                h.icon.setImageDrawable(iconUsb);
                break;
            case USER:
            default:
                h.icon.setImageDrawable(iconUser);
                break;        
        }
        
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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

    @NotNull
    public Set<String> getData() {
        final Set<String> user = new LinkedHashSet<String>();
        int i = 0;
        for (final String bookmark : this.bookmarks) {
            if (i++ >= this.userStart) {
                user.add(bookmark);
            }
        }
        return user;
    }
    
    private static final class Holder {
        private ImageView icon;
        private TextView title;
        private TextView summary;
        private View remove;
    }

}
