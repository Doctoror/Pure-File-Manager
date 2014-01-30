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
package com.docd.purefm.tasks;

import android.os.AsyncTask;

import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.GenericFileFilter;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.MimeTypes;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public final class DirectoryScanTask extends
        AsyncTask<GenericFile, Void, GenericFile[]> {

    private static final ListFileFilter sFilter;
    private final BrowserBaseAdapter mBrowserAdapter;
    
    private final Browser mBrowser;
    private final PullToRefreshLayout mPullToRefreshLayout;
    
    private GenericFile file;
    
    static {
        sFilter = new ListFileFilter();
    }

    public DirectoryScanTask(Browser browser, PullToRefreshLayout layout, String mimeType, BrowserBaseAdapter adapter) {
        this.mBrowser = browser;
        this.mPullToRefreshLayout = layout;
        this.mBrowserAdapter = adapter;
        sFilter.setMimeType(mimeType);
    }

    @Override
    protected void onPreExecute() {
        mPullToRefreshLayout.setRefreshing(true);
    }

    @Override
    protected GenericFile[] doInBackground(GenericFile... arg0) {
        this.file = arg0[0];
        return arg0[0].listFiles(sFilter);
    }

    @Override
    protected void onPostExecute(GenericFile[] result) {
        super.onPostExecute(result);
        mBrowserAdapter.updateData(result);
        mBrowser.onScanFinished(this.file);
        mPullToRefreshLayout.setRefreshComplete();
    }

    @Override
    protected void onCancelled(GenericFile[] result) {
        super.onCancelled(result);
        mBrowser.onScanCancelled(false);
        mPullToRefreshLayout.setRefreshComplete();
    }

    private static final class ListFileFilter implements GenericFileFilter {

        private String mType;
        private boolean mAcceptAll;

        protected ListFileFilter() {
        }
        
        protected void setMimeType(String type) {
            this.mType = type;
            this.mAcceptAll = type == null || type.equals("*/*");
        }

        @Override
        public boolean accept(GenericFile pathname) {
            if (!Settings.showHidden && pathname.isHidden()) {
                return false;
            }
            if (this.mType == null) {
                return true;
            }
            if (this.mAcceptAll || pathname.isDirectory()) {
                return true;
            }
            final String fileType = pathname.getMimeType();
            return fileType != null
                    && MimeTypes.mimeTypeMatch(this.mType, fileType);
        }
    }

}