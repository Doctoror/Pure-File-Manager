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

import java.io.IOException;
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
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.DrawableLruCache;
import com.docd.purefm.utils.FileSortType;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.ui.view.OverlayImageView;

import org.apache.commons.io.FilenameUtils;
import android.support.annotation.NonNull;

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
            FileObserver.MOVED_TO;

    /**
     * Cache that holds file icons
     */
    private static DrawableLruCache<Integer> sDrawableLruCache;

    /**
     * Cache that holds file icons
     */
    private static DrawableLruCache<String> sMimeTypeIconCache;

    @NonNull
    private final Handler mHandler;

    /**
     * Application's {@link android.content.res.Resources}
     */
    @NonNull
    private final Resources mResources;

    /**
     * Current {@link android.content.res.Resources.Theme}
     */
    @NonNull
    private final Resources.Theme mTheme;

    @NonNull
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @NonNull
    private final FileObserverCache mObserverCache = FileObserverCache.getInstance();

    /**
     * Adapter's content
     */
    @NonNull
    private final List<GenericFile> mContent = new ArrayList<>();

    /**
     * Observers for Files used in this Adapter
     */
    @NonNull
    private final List<MultiListenerFileObserver> mFileObservers = new ArrayList<>();

    /**
     * Executor for loading file previews
     */
    private ExecutorService mExecutor;

    /**
     * Current FileSortType
     */
    private FileSortType mComparator = FileSortType.NAME_ASC;

    /**
     * Current LayoutInflater
     */
    protected final LayoutInflater mLayoutInflater;

    private final PreviewHolder mPreviewHolder;

    /**
     * Settings instance
     */
    @NonNull
    protected final Settings mSettings;

    protected BrowserBaseAdapter(@NonNull final Activity context) {
        if (sDrawableLruCache == null) {
            sDrawableLruCache = new DrawableLruCache<>();
        }
        if (sMimeTypeIconCache == null) {
            sMimeTypeIconCache = new DrawableLruCache<>();
        }
        mSettings = Settings.getInstance(context);
        mPreviewHolder = PreviewHolder.getInstance(context);
        mTheme = context.getTheme();
        mResources = context.getResources();
        mHandler = new FileObserverEventHandler(this);
        mLayoutInflater = context.getLayoutInflater();
    }

    public void dropCaches() {
        sDrawableLruCache.evictAll();
        sMimeTypeIconCache.evictAll();
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
                        .getOrCreate(file, OBSERVER_EVENTS);
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
     * Inserts new items to this Adapter's data to position determined by current FileSortType
     *
     * @param files Files to insert
     */
    public final void addFiles(final GenericFile... files) {
        for (final GenericFile file : files) {
            mContent.add(file);
            mFileObservers.add(mObserverCache.getOrCreate(file, OBSERVER_EVENTS));
        }
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
    public void registerDataSetObserver(final DataSetObserver arg0) {
        this.mDataSetObservable.registerObserver(arg0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDataSetObserver(final DataSetObserver arg0) {
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
        final Message message = mHandler.obtainMessage(FileObserverEventHandler.MESSAGE_OBSERVER_EVENT);
        message.arg1 = event;
        message.obj = FileFactory.newFile(mSettings, path);
        mHandler.sendMessage(message);
    }

    /**
     * {@link android.os.FileObserver} event that should be ran only on UI thread
     *
     * @param event The type of event which happened
     * @param file The modified file, relative to the main monitored file or directory,
     *             of the file or directory which triggered the event
     */
    synchronized void onEventUIThread(final int event,
                                      @NonNull final GenericFile file) {
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.CREATE:
                //Do nothing. The event is handled in Browser
                break;

            default:
                onFileModified(file);
                break;
        }
    }

    /**
     * Should be called when the file at path was modified
     *
     * @param modified The modified file
     */
    private void onFileModified(@NonNull final GenericFile modified) {
        final GenericFile affectedFile = getFileByPath(modified.getAbsolutePath());
        if (affectedFile != null) {
            if (modified.exists()) {
                final int index = mContent.indexOf(affectedFile);
                if (index != -1) {
                    mContent.set(index, modified);
                } else {
                    mContent.add(modified);
                    Collections.sort(mContent, mComparator.getComparator());
                }
            } else {
                mContent.remove(affectedFile);
                removeObserverForPath(modified.getAbsolutePath());
            }
        }
    }

    @Nullable
    private GenericFile getFileByPath(@NonNull final String path) {
        for (final GenericFile file : mContent) {
            if (file.getAbsolutePath().equals(path)) {
                return file;
            }
        }
        //try with canonical paths
        for (final GenericFile file : mContent) {
            try {
                if (file.getCanonicalPath().equals(path)) {
                    return file;
                }
            } catch (IOException e) {
                //ignored
            }
        }
        return null;
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
            Drawable mimeIcon = sMimeTypeIconCache.get(fileExt);
            if (mimeIcon == null) {
                final int mimeIconId = MimeTypes.getIconForExt(fileExt);
                if (mimeIconId != 0) {
                    mimeIcon = mResources.getDrawable(mimeIconId);
                    sMimeTypeIconCache.put(fileExt, mimeIcon);
                }
            }
            if (mimeIcon != null) {
                icon.setImageDrawable(mimeIcon);
            } else {
                final Permissions p = file.getPermissions();
                if (!file.isSymlink() && (p.gx || p.ux || p.ox)) {
                    final int executableIcon = R.drawable.ic_fso_type_executable;
                    Drawable iconDrawable = sDrawableLruCache.get(executableIcon);
                    if (iconDrawable == null) {
                        iconDrawable = mResources.getDrawable(executableIcon);
                        sDrawableLruCache.put(executableIcon, iconDrawable);
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
    @NonNull
    private static Drawable getDrawableForRes(final Resources.Theme theme, final int attrId) {
        Drawable drawable = sDrawableLruCache.get(attrId);
        if (drawable == null) {
            drawable = ThemeUtils.getDrawableNonNull(theme, attrId);
            sDrawableLruCache.put(attrId, drawable);
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
    @NonNull
    private static Drawable getDrawableForRes(final Resources res, final int resId) {
        Drawable drawable = sDrawableLruCache.get(resId);
        if (drawable == null) {
            drawable = res.getDrawable(resId);
            sDrawableLruCache.put(resId, drawable);
        }
        return drawable;
    }

    /**
     * Loads preview from cache. If the preview in cache is not found it starts new
     * {@link com.docd.purefm.adapters.BrowserBaseAdapter.Job} for loading preview from file
     *
     * @param file File to load preview for
     * @param logo View to set loaded preview to
     */
    protected final void loadPreview(@NonNull final GenericFile file,
                                     @NonNull final OverlayImageView logo) {
        final Bitmap result = mPreviewHolder.getCached(file.toFile());
        if (result != null) {
            logo.setImageBitmap(result);
        } else {
            try {
                mExecutor.submit(new Job(mHandler, mPreviewHolder, file, logo));
            } catch (Exception e) {
                Log.w("BrowserBaseAdapter", "Error submitting Job:" + e);
            }
        }
    }

    private static final class FileObserverEventHandler extends Handler {

        static final int MESSAGE_OBSERVER_EVENT = 666;

        @NonNull
        private final WeakReference<BrowserBaseAdapter> mAdapterReference;

        FileObserverEventHandler(@NonNull final BrowserBaseAdapter adapter) {
            super(Looper.getMainLooper());
            this.mAdapterReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == MESSAGE_OBSERVER_EVENT) {
                final BrowserBaseAdapter adapter = mAdapterReference.get();
                if (adapter != null) {
                    adapter.onEventUIThread(msg.arg1, (GenericFile) msg.obj);
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

        @NonNull
        private final Handler mHandler;

        @NonNull
        private final PreviewHolder mPreviewHolder;

        @NonNull
        private final OverlayImageView mImageView;

        @NonNull
        private final GenericFile mFile;

        Job(@NonNull final Handler handler,
            @NonNull final PreviewHolder previewHolder,
            @NonNull final GenericFile file,
            @NonNull final OverlayImageView imageView)
        {
            this.mHandler = handler;
            this.mPreviewHolder = previewHolder;
            this.mFile = file;
            this.mImageView = imageView;
            imageView.setTag(file);
        }

        @Override
        public void run()
        {
            final Thread t = Thread.currentThread();
            t.setPriority(Thread.NORM_PRIORITY - 1);
            
            final Bitmap result = mPreviewHolder.loadPreview(this.mFile.toFile());
            if (result != null && this.mImageView.getTag().equals(this.mFile)) {
                this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(result);
                        mImageView.setOverlay(null);
                    }
                });
            }
        }
    }

}