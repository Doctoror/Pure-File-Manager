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
import android.support.v4.widget.SwipeRefreshLayout;

import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.GenericFileFilter;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.MimeTypes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class DirectoryScanTask extends
        AsyncTask<GenericFile, Void, GenericFile[]> {

    @NonNull
    private final BrowserBaseAdapter mBrowserAdapter;

    @NonNull
    private final Browser mBrowser;

    @NonNull
    private final SwipeRefreshLayout[] mSwipeRefreshLayouts;

    private final ListFileFilter mFileFilter;
    
    private GenericFile mFile;

    public DirectoryScanTask(@NonNull final Browser browser,
                             @Nullable final String mimeType,
                             @NonNull final BrowserBaseAdapter adapter,
                             @NonNull final Settings settings,
                             @NonNull final SwipeRefreshLayout... refreshLayouts) {
        mBrowser = browser;
        mSwipeRefreshLayouts = refreshLayouts;
        mBrowserAdapter = adapter;
        mFileFilter = new ListFileFilter(settings.showHidden());
        mFileFilter.setMimeType(mimeType);
    }

    @Override
    protected void onPreExecute() {
        for (final SwipeRefreshLayout layout : mSwipeRefreshLayouts) {
            layout.setRefreshing(true);
        }
    }

    @Override
    protected GenericFile[] doInBackground(GenericFile... arg0) {
        this.mFile = arg0[0];
        return arg0[0].listFiles(mFileFilter);
    }

    @Override
    protected void onPostExecute(GenericFile[] result) {
        super.onPostExecute(result);
        mBrowserAdapter.updateData(result);
        mBrowser.onScanFinished(this.mFile);
        for (final SwipeRefreshLayout layout : mSwipeRefreshLayouts) {
            layout.setRefreshing(false);
        }
    }

    @Override
    protected void onCancelled(GenericFile[] result) {
        super.onCancelled(result);
        mBrowser.onScanCancelled(false);
        for (final SwipeRefreshLayout layout : mSwipeRefreshLayouts) {
            layout.setRefreshing(false);
        }
    }

    private static final class ListFileFilter implements GenericFileFilter {

        private String mType;
        private boolean mAcceptAll;

        private final boolean mShowHidden;

        ListFileFilter(final boolean showHidden) {
            mShowHidden = showHidden;
        }
        
        void setMimeType(String type) {
            this.mType = type;
            this.mAcceptAll = type == null || type.equals("*/*");
        }

        @Override
        public boolean accept(final GenericFile pathname) {
            if (!mShowHidden && pathname.isHidden()) {
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