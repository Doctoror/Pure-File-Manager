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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverCache;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.MultiListenerFileObserver;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.utils.DrawableLruCache;
import com.docd.purefm.utils.FileSortType;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.ui.view.OverlayImageView;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Base adapter for file list.
 * Manages FileObserver events
 * @author Doctoror
 */
public abstract class BrowserBaseAdapter implements ListAdapter,
        MultiListenerFileObserver.OnEventListener {

    /**
     * Events to be monitor for every File in this Adapter
     */
    private static final int OBSERVER_EVENTS =
            FileObserver.CREATE |
            FileObserver.DELETE_SELF |
            FileObserver.ATTRIB |
            FileObserver.MODIFY |
            FileObserver.MOVED_TO;

    /**
     * Cache that holds file icons
     */
    private static DrawableLruCache<Integer> mDrawableLruCache;

    /**
     * Cache that holds file icons
     */
    private static DrawableLruCache<String> mMimeTypeIconCache;

    private final Handler mHandler;

    /**
     * Application's {@link android.content.res.Resources}
     */
    private final Resources mResources;

    /**
     * Current {@link android.content.res.Resources.Theme}
     */
    private final Resources.Theme mTheme;

    private final DataSetObservable mDataSetObservable;
    private final FileObserverCache mObserverCache;

    /**
     * Adapter's content
     */
    private final List<GenericFile> mContent;

    /**
     * Observers for Files used in this Adapter
     */
    private final List<MultiListenerFileObserver> mFileObservers;

    /**
     * Executor for loading file previews
     */
    private ExecutorService mExecutor;

    /**
     * Current FileSortType
     */
    private FileSortType mComparator;

    /**
     * Current LayoutInflater
     */
    protected final LayoutInflater mLayoutInflater;

    protected BrowserBaseAdapter(final Activity context) {
        if (mDrawableLruCache == null) {
            mDrawableLruCache = new DrawableLruCache<Integer>();
        }
        if (mMimeTypeIconCache == null) {
            mMimeTypeIconCache = new DrawableLruCache<String>();
        }

        this.mResources = context.getResources();
        this.mHandler = new FileObserverEventHandler(this);
        this.mTheme = context.getTheme();
        this.mDataSetObservable = new DataSetObservable();
        this.mObserverCache = FileObserverCache.getInstance();
        this.mLayoutInflater = context.getLayoutInflater();
        this.mContent = new ArrayList<GenericFile>();
        this.mFileObservers = new ArrayList<MultiListenerFileObserver>();
        this.mComparator = FileSortType.NAME_ASC;
    }

    /**
     * Sets and applies new data
     *
     * @param data Data to apply
     */
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

    /**
     * Removes references for all {@link android.os.FileObserver}s
     */
    public final void releaseObservers() {
        for (final MultiListenerFileObserver observer : mFileObservers) {
            observer.removeOnEventListener(this);
            observer.stopWatching();
        }
        mFileObservers.clear();
    }

    /**
     * Inserts new item to this Adapter's data to position determined by current FileSortType
     *
     * @param file File to insert
     */
    public final void addFile(final GenericFile file) {
        mContent.add(file);
        mFileObservers.add(mObserverCache.getOrCreate(file.getAbsolutePath(), OBSERVER_EVENTS));
        Collections.sort(mContent, mComparator.getComparator());
        notifyDataSetChanged();
    }

    /**
     * Sets and applies {@link com.docd.purefm.utils.FileSortType}
     *
     * @param comp FileSortType to apply
     */
    public void setCompareType(final FileSortType comp) {
        mComparator = comp;
        Collections.sort(mContent, comp.getComparator());
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return this.mContent.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericFile getItem(int pos) {
        return this.mContent.get(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int pos) {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int pos) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.mContent.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDataSetObserver(DataSetObserver arg0) {
        this.mDataSetObservable.registerObserver(arg0);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(int arg0) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvent(final int event, final String path) {
        //mHandler.removeCallbacksAndMessages(null);
        final Bundle data = new Bundle();
        data.putInt(FileObserverEventHandler.DATA_OBSERVER_EVENT, event);
        data.putString(FileObserverEventHandler.DATA_OBSERVER_PATH, path);
        final Message message = mHandler.obtainMessage(FileObserverEventHandler.MESSAGE_OBSERVER_EVENT);
        message.setData(data);
        mHandler.sendMessage(message);
        //mHandler.post(new FileObserverEventRunnable(this, event, path));
    }

    /**
     * {@link android.os.FileObserver} event that should be ran only on UI thread
     *
     * @param event The type of event which happened
     * @param path The path, relative to the main monitored file or directory,
     *             of the file or directory which triggered the event
     */
    synchronized void onEventUIThread(final int event, final String path) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("");
        }
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

            case FileObserver.CREATE:
                //Do nothing. The event is handled in Browser
                break;

            default:
                //Sometimes it happens that some unknown event is delivered instead of DELETE_SELF
                //So check what happened and perform corresponding action
                onFileModified(path);
                break;

        }
    }

    /**
     * Should be called when the file at path was modified
     *
     * @param path of the modified file
     */
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

    /**
     * Removes {@link android.os.FileObserver} that monitors the path from cache
     *
     * @param path Path to remove FileObserver for
     */
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

    /**
     * Resolves icon that should be used for the File
     *
     * @param file File to set icon for
     * @param icon View to set icon
     */
    protected final void setIcon(final GenericFile file, final ImageView icon) {
        if (file.isDirectory()) {
            icon.setImageDrawable(getDrawableForRes(mResources, R.drawable.ic_fso_folder));
        } else {
            final String fileExt = FilenameUtils.getExtension(file.getName());
            Drawable mimeIcon = mMimeTypeIconCache.get(fileExt);
            if (mimeIcon == null) {
                final int mimeIconId = MimeTypes.getIconForExt(fileExt);
                if (mimeIconId != 0) {
                    mimeIcon = mResources.getDrawable(mimeIconId);
                    mMimeTypeIconCache.put(fileExt, mimeIcon);
                }
            }
            if (mimeIcon != null) {
                icon.setImageDrawable(mimeIcon);
            } else {
                final Permissions p = file.getPermissions();
                if (!file.isSymlink() && (p.gx || p.ux || p.ox)) {
                    final int executableIcon = R.drawable.ic_fso_type_executable;
                    Drawable iconDrawable = mDrawableLruCache.get(executableIcon);
                    if (iconDrawable == null) {
                        iconDrawable = mResources.getDrawable(executableIcon);
                        mDrawableLruCache.put(executableIcon, iconDrawable);
                    }
                    icon.setImageDrawable(iconDrawable);
                } else {
                    icon.setImageDrawable(getDrawableForRes(mResources, R.drawable.ic_fso_default));
                }
            }
        }
    }

    /**
     * Applies overlay for File, if should be applied. Removes overlay if not.
     *
     * @param f File to apply overlay for
     * @param overlay View to apply overlay to
     */
    protected final void applyOverlay(GenericFile f, OverlayImageView overlay) {
        if (f.isSymlink()) {
            overlay.setOverlay(getDrawableForRes(mTheme, R.attr.ic_fso_symlink));
        } else {
            overlay.setOverlay(null);
        }
    }

    /**
     * Loads drawable from resources cache. If not found in cache, loads the
     * {@link android.graphics.drawable.Drawable} from {@link android.content.res.Resources.Theme}
     *
     * @param theme Theme to load drawable for
     * @param attrId attribute id of resource to load
     * @return Drawable for Theme
     */
    @NotNull
    private static Drawable getDrawableForRes(final Resources.Theme theme, final int attrId) {
        Drawable drawable = mDrawableLruCache.get(attrId);
        if (drawable == null) {
            drawable = ThemeUtils.getDrawable(theme, attrId);
            mDrawableLruCache.put(attrId, drawable);
        }
        return drawable;
    }

    /**
     * Loads drawable from resources cache. If not found in cache, loads the
     * {@link android.graphics.drawable.Drawable} from {@link android.content.res.Resources}
     *
     * @param res Resources to load drawable from
     * @param resId Id of resource to load
     * @return Drawable from resources
     */
    @NotNull
    private static Drawable getDrawableForRes(final Resources res, final int resId) {
        Drawable drawable = mDrawableLruCache.get(resId);
        if (drawable == null) {
            drawable = res.getDrawable(resId);
            mDrawableLruCache.put(resId, drawable);
        }
        return drawable;
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

    /**
     * Loads preview from cache. If the preview in cache is not found it starts new
     * {@link com.docd.purefm.adapters.BrowserBaseAdapter.Job} for loading preview from file
     *
     * @param file File to load preview for
     * @param logo View to set loaded preview to
     */
    protected final void loadPreview(final GenericFile file, OverlayImageView logo) {
        final Bitmap result = PreviewHolder.getCached(file.toFile());
        if (result != null) {
            logo.setImageBitmap(result);
        } else {
            try {
                mExecutor.submit(new Job(mHandler, file, logo));
            } catch (Exception e) {
                Log.w("BrowserBaseAdapter", "Error submitting Job:" + e);
            }
        }
    }

    private static final class FileObserverEventHandler extends Handler {

        static final int MESSAGE_OBSERVER_EVENT = 666;
        static final String DATA_OBSERVER_EVENT = "FileObserverEventHandler.data.OBSERVER_EVENT";
        static final String DATA_OBSERVER_PATH = "FileObserverEventHandler.data.OBSERVER_PATHT";

        private final WeakReference<BrowserBaseAdapter> mAdapterReference;

        private FileObserverEventHandler(BrowserBaseAdapter adapter) {
            this.mAdapterReference = new WeakReference<BrowserBaseAdapter>(adapter);
        }

        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == MESSAGE_OBSERVER_EVENT) {
                final Bundle data = msg.getData();
                final BrowserBaseAdapter adapter = mAdapterReference.get();
                if (adapter != null) {
                    adapter.onEventUIThread(data.getInt(DATA_OBSERVER_EVENT),
                            data.getString(DATA_OBSERVER_PATH));
                    if (!hasMessages(MESSAGE_OBSERVER_EVENT)) {
                        adapter.notifyDataSetChanged();
                    }
                }
            } else {
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Executor job for loading preview from file
     */
    private static final class Job
            implements Runnable
    {

        private final Handler handler;
        private final OverlayImageView logo;
        private final GenericFile file;

        Job(Handler handler, GenericFile file, OverlayImageView logo)
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
            
            final Bitmap result = PreviewHolder.loadPreview(this.file.toFile());
            if (result != null && this.logo.getTag().equals(this.file)) {
                this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        logo.setImageBitmap(result);
                        logo.setOverlay(null);
                    }
                });
            }
        }
    }

}