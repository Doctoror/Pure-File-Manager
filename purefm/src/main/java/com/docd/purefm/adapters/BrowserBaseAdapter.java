package com.docd.purefm.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.FileObserver;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverCache;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.MultiListenerFileObserver;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.utils.FileSortType;
import com.docd.purefm.utils.PreviewHolder;

public abstract class BrowserBaseAdapter implements ListAdapter,
        MultiListenerFileObserver.OnEventListener {

    private static final int OBSERVER_EVENTS =
            FileObserver.DELETE_SELF |
            FileObserver.ATTRIB |
            FileObserver.MODIFY |
            FileObserver.MOVED_TO;

    private final Handler handler;

    private final DataSetObservable mDataSetObservable;
    private final FileObserverCache observerCache;
    
    protected final Activity activity;
    protected final LayoutInflater inflater;
    protected final List<GenericFile> files;
    protected final List<MultiListenerFileObserver> fileObservers;
    
    protected ExecutorService executor;
    protected FileSortType comparator;

    protected BrowserBaseAdapter(final Activity context) {
        this.handler = new Handler();
        this.mDataSetObservable = new DataSetObservable();
        this.observerCache = FileObserverCache.getInstance();
        this.activity = context;
        this.inflater = LayoutInflater.from(context);
        this.files = new ArrayList<GenericFile>();
        this.fileObservers = new ArrayList<MultiListenerFileObserver>();
        this.comparator = FileSortType.NAME_ASC;
    }
    
    public void updateData(GenericFile[] data) {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.files.clear();
        this.releaseObservers();
        if (data != null) {
            Arrays.sort(data, this.comparator.getComparator());
            for (final GenericFile file : data) {
                this.files.add(file);
                final MultiListenerFileObserver observer = this.observerCache
                        .getOrCreate(file.getAbsolutePath(), OBSERVER_EVENTS);
                observer.addOnEventListener(this);
                observer.startWatching();
                this.fileObservers.add(observer);
            }
        }
        this.notifyDataSetChanged();
    }

    public void releaseObservers() {
        for (final MultiListenerFileObserver observer : this.fileObservers) {
            observer.removeOnEventListener(this);
            observer.stopWatching();
        }
        this.fileObservers.clear();
    }
    
    public void addFile(final GenericFile file) {
        this.files.add(file);
        Collections.sort(this.files, this.comparator.getComparator());
        this.notifyDataSetChanged();
    }
    
    public void setCompareType(final FileSortType comp) {
        this.comparator = comp;
        Collections.sort(this.files, comp.getComparator());
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.files.size();
    }

    @Override
    public GenericFile getItem(int pos) {
        return this.files.get(pos);
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
        return this.files.isEmpty();
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
        handler.removeCallbacks(this.notifyDataSetChangedRunnable);
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.ATTRIB:
            case FileObserver.MODIFY:
                final GenericFile target = FileFactory.newFile(path);
                this.files.remove(target);
                final int index = this.files.indexOf(target);
                if (index != -1) {
                    this.files.set(index, target);
                } else {
                    this.files.add(target);
                    Collections.sort(this.files, this.comparator.getComparator());
                }
                break;

            case FileObserver.DELETE_SELF:
            case FileObserver.MOVED_TO:
                final int filesSize = this.files.size();
                for (int i = 0; i < filesSize; i++) {
                    final GenericFile file = this.files.get(i);
                    if (file.getAbsolutePath().equals(path)) {
                        this.files.remove(i);
                        break;
                    }
                }

                final int observersSize = this.fileObservers.size();
                for (int i = 0; i < observersSize; i++) {
                    final MultiListenerFileObserver observer = this.fileObservers.get(i);
                    if (observer.getPath().equals(path)) {
                        observer.stopWatching();
                        this.fileObservers.remove(i);
                        break;
                    }
                }
                break;
        }
        handler.post(this.notifyDataSetChangedRunnable);
    }

    protected void applyOverlay(GenericFile f, ImageView overlay) {
        final Permissions p = f.getPermissions();
        
        if (f.isSymlink()) {
            overlay.setImageResource(R.drawable.ic_fso_symlink);
            overlay.setVisibility(View.VISIBLE);
        } else if (f.isDirectory()) {
            overlay.setVisibility(View.INVISIBLE);
        } else {
            int icon = f.getTypeIcon();
            if (icon == 0) {
                if (p.gx || p.ux || p.ox) {
                    icon = R.drawable.ic_fso_type_executable;
                }
            }
            if (icon == 0) {
                overlay.setVisibility(View.INVISIBLE);
            } else {
                overlay.setVisibility(View.VISIBLE);
                overlay.setImageResource(icon);
            }
        }
    }

    private final Runnable notifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    protected final class Job
            implements Runnable
    {

        private final ImageView logo;
        private final ImageView overlay;
        private final GenericFile file;

        protected Job(GenericFile file, ImageView logo, ImageView overlay)
        {
            this.file = file;
            this.logo = logo;
            this.overlay = overlay;
        }

        @Override
        public void run()
        {
            final Thread t = Thread.currentThread();
            t.setPriority(Thread.NORM_PRIORITY - 1);
            
            final Bitmap result = PreviewHolder.get(this.file.toFile());
            
            if (result != null && logo.getTag().equals(this.file)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logo.setImageBitmap(result);
                        overlay.setImageDrawable(null);
                    }
                });
            }
        }
    }

}