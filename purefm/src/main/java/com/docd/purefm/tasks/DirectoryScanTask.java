package com.docd.purefm.tasks;

import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;

import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.GenericFileFilter;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.MimeTypes;

public final class DirectoryScanTask extends
        AsyncTask<GenericFile, Void, GenericFile[]> {

    private static final ListFileFilter filter;
    private final BrowserBaseAdapter adapter;
    
    private final Browser browser;
    private final MenuItem item;
    private final View actionView;
    
    private GenericFile file;
    
    static {
        filter = new ListFileFilter();
    }

    public DirectoryScanTask(Browser browser, MenuItem item, View progress, String mimeType, BrowserBaseAdapter adapter) {
        this.browser = browser;
        this.actionView = progress;
        this.adapter = adapter;
        this.item = item;
        filter.setMimeType(mimeType);
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.item.setActionView(this.actionView);
    }

    @Override
    protected GenericFile[] doInBackground(GenericFile... arg0) {
        this.file = arg0[0];
        return arg0[0].listFiles(filter);
    }

    @Override
    protected void onPostExecute(GenericFile[] result) {
        super.onPostExecute(result);
        this.adapter.updateData(result);
        this.browser.onScanFinished(this.file);
        this.makeVisible();
    }

    @Override
    protected void onCancelled(GenericFile[] result) {
        super.onCancelled(result);
        this.browser.onScanCancelled();
        this.makeVisible();
    }

    private void makeVisible() {
        //this.show1.setVisibility(View.GONE);
        //this.hide1.setVisibility(View.VISIBLE);
        this.item.setActionView(null);
    }

    private static final class ListFileFilter implements GenericFileFilter {

        private String type;
        private boolean acceptAll;

        protected ListFileFilter() {
        }
        
        protected void setMimeType(String type) {
            this.type = type;
            this.acceptAll = type == null || type.equals("*/*");
        }

        @Override
        public boolean accept(GenericFile pathname) {
            if (!Settings.showHidden && pathname.isHidden()) {
                return false;
            }
            if (this.type == null) {
                return true;
            }
            if (this.acceptAll || pathname.isDirectory()) {
                return true;
            }
            final String fileType = pathname.getMimeType();
            return fileType != null
                    && MimeTypes.mimeTypeMatch(this.type, fileType);
        }
    }

}