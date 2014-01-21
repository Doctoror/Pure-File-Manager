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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.res.Resources;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ListAdapter;

import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverCache;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.MultiListenerFileObserver;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.utils.DrawableLruCache;
import com.docd.purefm.utils.FileSortType;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.view.OverlayRecyclingImageView;

/**
 * Base adapter for file list.
 * Manages FileObserver events
 * @author Doctoror
 */
public abstract class BrowserBaseAdapter implements ListAdapter,
        MultiListenerFileObserver.OnEventListener {

    private static final int OBSERVER_EVENTS =
            FileObserver.DELETE_SELF |
            FileObserver.ATTRIB |
            FileObserver.MODIFY |
            FileObserver.MOVED_TO;

    private final Handler mHandler;

    private final Resources mResources;
    private final Resources.Theme mTheme;

    private final DataSetObservable mDataSetObservable;
    private final FileObserverCache mObserverCache;
    private final DrawableLruCache mDrawableLruCache;
    
    private final List<GenericFile> mContent;
    private final List<MultiListenerFileObserver> mFileObservers;
    
    private ExecutorService mExecutor;
    private FileSortType mComparator;

    protected final LayoutInflater mLayoutInflater;

    protected BrowserBaseAdapter(final Activity context) {
        this.mHandler = new Handler();
        this.mResources = context.getResources();
        this.mTheme = context.getTheme();
        this.mDataSetObservable = new DataSetObservable();
        this.mObserverCache = FileObserverCache.getInstance();
        this.mDrawableLruCache = DrawableLruCache.getInstance();
        this.mLayoutInflater = context.getLayoutInflater();
        this.mContent = new ArrayList<GenericFile>();
        this.mFileObservers = new ArrayList<MultiListenerFileObserver>();
        this.mComparator = FileSortType.NAME_ASC;
    }
    
    public void updateData(GenericFile[] data) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
        mExecutor = Executors.newSingleThreadExecutor();
        mContent.clear();
        releaseObservers();
        if (data != null) {
            Arrays.sort(data, mComparator.getComparator());
            for (final GenericFile file : data) {
                mContent.add(file);
                final MultiListenerFileObserver observer = mObserverCache
                        .getOrCreate(file.getAbsolutePath(), OBSERVER_EVENTS);
                observer.addOnEventListener(this);
                observer.startWatching();
                mFileObservers.add(observer);
            }
        }
        this.notifyDataSetChanged();
    }

    public final void releaseObservers() {
        for (final MultiListenerFileObserver observer : mFileObservers) {
            observer.removeOnEventListener(this);
            observer.stopWatching();
        }
        mFileObservers.clear();
    }
    
    public final void addFile(final GenericFile file) {
        mContent.add(file);
        Collections.sort(mContent, mComparator.getComparator());
        notifyDataSetChanged();
    }
    
    public void setCompareType(final FileSortType comp) {
        mComparator = comp;
        Collections.sort(mContent, comp.getComparator());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mContent.size();
    }

    @Override
    public GenericFile getItem(int pos) {
        return this.mContent.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0L;
    }

    @Override
    public int getItemViewType(int pos) {
        return 0;
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
        return this.mContent.isEmpty();
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
    protected synchronized final void notifyDataSetChanged() {
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

    @Override
    public void onEvent(final int event, final String path) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(new FileObserverEventRunnable(this, event, path));
    }

    void onEventUIThread(final int event, final String path) {
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.ATTRIB:
            case FileObserver.MODIFY:
                onFileModified(path);
                break;

            case FileObserver.DELETE_SELF:
            case FileObserver.MOVED_TO:
                final int filesSize = mContent.size();
                for (int i = 0; i < filesSize; i++) {
                    final GenericFile file = mContent.get(i);
                    if (file.getAbsolutePath().equals(path)) {
                        mContent.remove(i);
                        break;
                    }
                }
                removeObserverForPath(path);
                break;

            default:
                //Sometimes it happens that some unknown event is delivered instead of DELETE_SELF
                //So check what happened and perform corresponding action
                onFileModified(path);
                break;

        }
        this.notifyDataSetChanged();
    }

    private void onFileModified(final String path) {
        final GenericFile affectedFile = FileFactory.newFile(path);
        mContent.remove(affectedFile);
        if (affectedFile.exists()) {
            final int index = mContent.indexOf(affectedFile);
            if (index != -1) {
                mContent.set(index, affectedFile);
            } else {
                mContent.add(affectedFile);
                Collections.sort(mContent, mComparator.getComparator());
            }
        } else {
            removeObserverForPath(path);
        }
    }

    private void removeObserverForPath(final String path) {
        final int observersSize = mFileObservers.size();
        for (int i = 0; i < observersSize; i++) {
            final MultiListenerFileObserver observer = mFileObservers.get(i);
            if (observer.getPath().equals(path)) {
                observer.stopWatching();
                mFileObservers.remove(i);
                break;
            }
        }
    }

    protected void applyOverlay(GenericFile f, OverlayRecyclingImageView overlay) {
        final Permissions p = f.getPermissions();
        
        if (f.isSymlink()) {
            Drawable symlink = mDrawableLruCache.get(R.attr.ic_fso_symlink);
            if (symlink == null) {
                symlink = ThemeUtils.getDrawable(mTheme, R.attr.ic_fso_symlink);
                mDrawableLruCache.put(R.attr.ic_fso_symlink, symlink);
            }
            overlay.setOverlay(symlink);
        } else if (f.isDirectory()) {
            overlay.setOverlay(null);
        } else {
            int icon = f.getTypeIcon();
            if (icon == 0) {
                if (p.gx || p.ux || p.ox) {
                    icon = R.drawable.ic_fso_type_executable;
                }
            }
            if (icon == 0) {
                overlay.setOverlay(null);
            } else {
                Drawable iconDrawable = mDrawableLruCache.get(icon);
                if (iconDrawable == null) {
                    iconDrawable = mResources.getDrawable(icon);
                    mDrawableLruCache.put(icon, iconDrawable);
                }
                overlay.setOverlay(iconDrawable);
            }
        }
    }

    private static final class FileObserverEventRunnable implements Runnable {
        private final WeakReference<BrowserBaseAdapter> adapter;
        private final int event;
        private final String path;

        private FileObserverEventRunnable(final BrowserBaseAdapter adapter, final int event, final String path) {
            this.adapter = new WeakReference<BrowserBaseAdapter>(adapter);
            this.event = event;
            this.path = path;
        }

        @Override
        public void run() {
            final BrowserBaseAdapter localAdapter = this.adapter.get();
            if (localAdapter != null) {
                localAdapter.onEventUIThread(this.event, this.path);
            }
        }
    }

    protected final void loadPreview(final GenericFile file, OverlayRecyclingImageView logo) {
        try {
            mExecutor.submit(new Job(mHandler, file, logo));
        } catch (Exception e) {
            Log.w("BrowserBaseAdapter", "Error submitting Job:" + e);
        }
    }

    private static final class Job
            implements Runnable
    {

        private final Handler handler;
        private final OverlayRecyclingImageView logo;
        private final GenericFile file;

        Job(Handler handler, GenericFile file, OverlayRecyclingImageView logo)
        {
            this.handler = handler;
            this.file = file;
            this.logo = logo;
            logo.setTag(file);
        }

        @Override
        public void run()
        {
            final Thread t = Thread.currentThread();
            t.setPriority(Thread.NORM_PRIORITY - 1);
            
            final Drawable result = PreviewHolder.get(this.file.toFile());
            if (result != null && this.logo.getTag().equals(this.file)) {
                this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        logo.setImageDrawable(result);
                        logo.setOverlay(null);
                    }
                });
            }
        }
    }

}