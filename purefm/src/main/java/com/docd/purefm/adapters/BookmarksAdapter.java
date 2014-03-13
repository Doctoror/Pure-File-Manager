package com.docd.purefm.adapters;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.docd.purefm.R;
import com.docd.purefm.ui.activities.BrowserPagerActivity;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.BookmarksHelper;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
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

    private final List<BookmarksHelper.BookmarkItem> mBookmarks;
    
    private final DataSetObservable mDataSetObservable;
    private final LayoutInflater mLayoutInflater;
    private final BrowserPagerActivity mActivity;
    
    private boolean modified;
    
    private int mUserBookmarksStart;
    
    public BookmarksAdapter(final BrowserPagerActivity activity) {
        this.mActivity = activity;
        this.mDataSetObservable = new DataSetObservable();
        this.mLayoutInflater = activity.getLayoutInflater();

        this.mUserBookmarksStart = BookmarksHelper.getUserBookmarkOffset();
        this.mBookmarks = BookmarksHelper.getAllBookmarks(activity);
    }
    
    public void addItem(@NotNull final String path) {
        if (bookmarksContainPath(path)) {
            Toast.makeText(mActivity, R.string.bookmark_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        final BookmarksHelper.BookmarkItem item = BookmarksHelper
                .createUserBookmarkItem(mActivity, path);
        if (!mBookmarks.add(item)) {
            Toast.makeText(mActivity, R.string.bookmark_not_added, Toast.LENGTH_SHORT).show();
            return;
        }        
        this.modified = true;
        this.notifyDataSetChanged();
    }

    private boolean bookmarksContainPath(@NotNull final String path) {
        for (final BookmarksHelper.BookmarkItem item : mBookmarks) {
            if (item.getDisplayPath().toString().equals(path)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int getCount() {
        return this.mBookmarks.size();
    }

    @Override
    public BookmarksHelper.BookmarkItem getItem(final int pos) {
        return this.mBookmarks.get(pos);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getItemViewType(int arg0) {
        return arg0 < mUserBookmarksStart ? 0 : 1;
    }

    @Override
    public View getView(int pos, View v, ViewGroup arg2) {
        
        final int viewType = this.getItemViewType(pos);
        Holder h;
        
        if (v == null) {
            if (viewType == 0) {
                v = this.mLayoutInflater.inflate(R.layout.list_item_bookmark, null);
            } else {
                v = this.mLayoutInflater.inflate(R.layout.list_item_bookmark_user, null);
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
        
        final BookmarksHelper.BookmarkItem cur = this.getItem(pos);
        final String currentPath = cur.getDisplayPath().toString();

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final GenericFile file = FileFactory.newFile(currentPath);
                mActivity.setCurrentPath(file);
            }
        });

        h.title.setText(cur.getDisplayName());
        h.summary.setText(currentPath);
        if (viewType == 1) {
            h.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mBookmarks.remove(cur);
                    modified = true;
                    notifyDataSetChanged();
                }
            });
        }

        h.icon.setImageDrawable(cur.getIcon());
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
        return this.mBookmarks.isEmpty();
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
        final Set<String> user = new LinkedHashSet<>();
        int i = 0;
        for (final BookmarksHelper.BookmarkItem bookmark : mBookmarks) {
            if (i++ >= mUserBookmarksStart) {
                user.add(bookmark.getDisplayPath().toString());
            }
        }
        return user;
    }
    
    private static final class Holder {
        ImageView icon;
        TextView title;
        TextView summary;
        View remove;
    }
}
