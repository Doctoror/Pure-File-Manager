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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.utils.FileSortType;
import com.docd.purefm.utils.PreviewHolder;

public abstract class BrowserBaseAdapter implements ListAdapter {

    private final DataSetObservable mDataSetObservable;
    
    protected final Activity context;
    protected final LayoutInflater inflater;
    protected final List<GenericFile> files;
    
    protected ExecutorService executor;
    protected FileSortType comparator;

    protected BrowserBaseAdapter(Activity context) {
        this.mDataSetObservable = new DataSetObservable();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.files = new ArrayList<GenericFile>();
        this.comparator = FileSortType.NAME_ASC;
    }
    
    public void updateData(GenericFile[] data) {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.files.clear();
        if (data != null) {
            Arrays.sort(data, this.comparator.getComparator());
            for (int i = 0; i < data.length; i++) {
                this.files.add(data[i]);
            }
        }
        this.notifyDataSetChanged();
    }
    
    public void addFile(GenericFile file) {
        this.files.add(file);
        Collections.sort(this.files, this.comparator.getComparator());
        this.notifyDataSetChanged();
    }
    
    public void setCompareType(FileSortType comp) {
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
    public abstract View getView(int pos, View v, ViewGroup arg2);

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
    protected void notifyDataSetChanged() {
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
                context.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        logo.setImageBitmap(result);
                        overlay.setImageDrawable(null);
                    }
                });
            }
        }
    }

}