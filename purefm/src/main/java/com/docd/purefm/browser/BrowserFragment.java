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
package com.docd.purefm.browser;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cyanogenmod.filemanager.util.MediaHelper;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.adapters.BrowserGridAdapter;
import com.docd.purefm.adapters.BrowserListAdapter;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.controller.ActionModeController;
import com.docd.purefm.controller.MenuController;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.DirectoryScanTask;
import com.docd.purefm.ui.fragments.UserVisibleHintFragment;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.view.BreadCrumbTextView;
import com.docd.purefm.view.BreadCrumbTextView.OnSequenceClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Fragment manages file List, menu and ActionMode.
 * @author Doctoror
 */
public final class BrowserFragment extends UserVisibleHintFragment
        implements OnRefreshListener {

    private static final String STATE_BROWSER = "BrowserFragment.state.mBrowser";

    //private ActionBar actionBar;
    private BrowserActivity mAttachedBrowserActivity;
    private ActionModeController actionModeController;
    private MenuController menuController;

    private Browser mBrowser;
    private BrowserBaseAdapter adapter;
    private OnNavigateListener mParentOnNavigateListener;

    private BreadCrumbTextView bBreadCrumbView;
    private OnSequenceClickListener sequenceListener;

    private PullToRefreshLayout mPullToRefreshLayout;
    private AbsListView mListView;
    private View mainProgress;

    private DirectoryScanTask scanner;

    private boolean refreshFlag;

    /**
     * If Browser is not yet initialized, initial state will be saved to this field
     */
    private Parcelable mBrowserInitialState;

    private int mPrevId;
    private boolean firstRun;

    private void ensureGridViewColumns(Configuration config) {
        if (this.mListView instanceof GridView) {
            ((GridView) this.mListView)
                    .setNumColumns(config.orientation == Configuration.ORIENTATION_LANDSCAPE ? 6
                            : 4);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.ensureGridViewColumns(newConfig);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BrowserActivity) {
            mBrowser = new Browser((BrowserActivity) activity);
        } else {
            throw new IllegalStateException("BrowserFragment should be attached only to BrowserActivity");
        }
        mBrowser.setOnNavigateListener(new OnNavigateListener() {

            @Override
            public void onNavigate(GenericFile path) {
                if (mParentOnNavigateListener != null) {
                    mParentOnNavigateListener.onNavigate(path);
                }
                if (isResumedAndVisible()) {
                    startScan();
                } else {
                    refreshFlag = true;
                }
            }

            @Override
            public void onNavigationCompleted(GenericFile path) {
                if (mainProgress.getVisibility() == View.VISIBLE) {
                    mainProgress.setVisibility(View.GONE);
                }
                if (firstRun) {
                    firstRun = false;
                }
                if (refreshFlag) {
                    refreshFlag = false;
                }
                if (mParentOnNavigateListener != null) {
                    mParentOnNavigateListener.onNavigationCompleted(path);
                }
            }
        });

        this.sequenceListener = new OnSequenceClickListener() {
            @Override
            public void onSequenceClick(String sequence) {
                final GenericFile target = FileFactory.newFile(sequence);
                mBrowser.navigate(target, true);
            }
        };

        this.mAttachedBrowserActivity = (BrowserActivity) activity;
        this.menuController = new MenuController(this.mAttachedBrowserActivity, mBrowser);
        this.actionModeController = new ActionModeController(this.mAttachedBrowserActivity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentOnNavigateListener = null;
    }

    @Override
    public void onCreate(Bundle state) {
        this.setRetainInstance(true);
        this.setHasOptionsMenu(true);
        this.firstRun = true;
        restoreManualState(state);
        super.onCreate(state);
    }

    public void restoreManualState(final Bundle state) {
        if (state != null) {
            state.setClassLoader(BrowserFragment.class.getClassLoader());
            final BrowserFragment.SavedState savedState = state.getParcelable(STATE_BROWSER);
            if (savedState != null) {
                if (savedState.mBrowserState != null) {
                    if (mBrowser != null) {
                        mBrowser.restoreState(savedState.mBrowserState);
                    } else {
                        mBrowserInitialState = savedState.mBrowserState;
                    }
                }
                mPrevId = savedState.mPrevId;
            }
        }
    }

    public void saveManualState(final Bundle outState) {
        final Parcelable browserState;
        if (mBrowser == null) {
            browserState = null;
        } else {
            browserState = mBrowser.saveInstanceState();
        }
        outState.putParcelable(STATE_BROWSER, new SavedState(mPrevId, browserState));
    }

    public boolean onBackPressed() {
        return this.mBrowser != null && this.mBrowser.back();
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        ensureGridViewColumns(getResources().getConfiguration());
        actionModeController.setListView(mListView);
        mParentOnNavigateListener = mAttachedBrowserActivity;

        bBreadCrumbView = this.mAttachedBrowserActivity.getTitleView();
        mBrowser.restoreState(mBrowserInitialState);
        mBrowserInitialState = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (this.mAttachedBrowserActivity != null && !this.mAttachedBrowserActivity.isDrawerOpened()) {
            inflater.inflate(R.menu.browser, menu);

            // TODO it returns true even on devices that don't have the physical key. Find a better method to detect search hardware button
            //if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_SEARCH)) {
            //    menu.removeItem(R.id.menu_search);
            //}

            final MenuItem content = menu.findItem(android.R.id.content);

            if (Settings.appearance == Settings.APPEARANCE_LIST) {
                content.setIcon(ThemeUtils.getDrawable(this.mAttachedBrowserActivity, R.attr.action_view_as_grid))
                        .setTitle(R.string.menu_view_as_grid)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                content.setIcon(ThemeUtils.getDrawable(this.mAttachedBrowserActivity, R.attr.action_view_as_list))
                        .setTitle(R.string.menu_view_as_list)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (ClipBoard.isEmpty()) {
                menu.removeItem(android.R.id.paste);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menuController.onMenuItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_browser, null);
        this.mPullToRefreshLayout = (PullToRefreshLayout) parent.findViewById(R.id.pullToRefreshLayout);
        this.initList(parent);
        return parent;
    }

    private void initList(View parent) {
        if (this.mListView != null) {
            this.mListView.getEmptyView().setVisibility(View.GONE);
            this.mListView.setVisibility(View.GONE);
        }
        this.mainProgress = parent.findViewById(android.R.id.progress);

        if (Settings.appearance == Settings.APPEARANCE_LIST) {
            View vs = parent.findViewById(android.R.id.list);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.mListView = (AbsListView) vs;
            this.adapter = new BrowserListAdapter(this.getActivity());
        } else {
            View vs = parent.findViewById(R.id.grid);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.mListView = (AbsListView) vs;
            this.adapter = new BrowserGridAdapter(this.getActivity());
        }

        this.menuController.setAdapter(this.adapter);

        this.mListView.setId(this.getNewId(parent));
        this.mListView.setEmptyView(parent.findViewById(android.R.id.empty));
        this.mListView.setAdapter(this.adapter);
        this.mListView.getEmptyView().setVisibility(View.GONE);
        this.mListView.setVisibility(View.GONE);

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                final GenericFile target = (GenericFile) (av
                        .getItemAtPosition(pos));
                if (target.isDirectory()) {
                    mBrowser.navigate(target, true);
                } else {
                    if (mAttachedBrowserActivity.getGetContentMimeType() == null) {
                        PureFMFileUtils.openFile(mAttachedBrowserActivity, target.toFile());
                    } else {
                        final Uri result = Uri.fromFile(target.toFile());
                        final Intent intent = new Intent();
                        intent.setData(getResultUriForFileFromIntent(mAttachedBrowserActivity.getContentResolver(),
                                target.toFile(), mAttachedBrowserActivity.getIntent()));
                        mAttachedBrowserActivity.setResult(Activity.RESULT_OK, intent);
                        mAttachedBrowserActivity.finish();
                    }
                }
            }

            /*
             * Copyright (C) 2013 The CyanogenMod Project
             *
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
            private Uri getResultUriForFileFromIntent(ContentResolver cr, File src, Intent intent) {
                // Try to find the preferred uri scheme
                Uri result = MediaHelper.fileToContentUri(cr, src);
                if (result == null) {
                    result = Uri.fromFile(src);
                }

                if (Intent.ACTION_PICK.equals(intent.getAction()) && intent.getData() != null) {
                    final String scheme = intent.getData().getScheme();
                    if (scheme != null) {
                        result = result.buildUpon().scheme(scheme).build();
                    }
                }

                return result;
            }
        });

        mListView.setChoiceMode(mAttachedBrowserActivity.getGetContentMimeType() == null ?
                AbsListView.CHOICE_MODE_MULTIPLE_MODAL : AbsListView.CHOICE_MODE_NONE);
    }

    private void onFirstInvalidate() {
        this.actionModeController.setListView(this.mListView);
        this.mBrowser.invalidate();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveManualState(outState);
    }

    @Override
    protected void onVisible() {
        invalidatePullToRefresh();
        if (this.firstRun || this.refreshFlag) {
            onFirstInvalidate();
        } else {
            mParentOnNavigateListener.onNavigationCompleted(this.mBrowser
                    .getCurrentPath());
        }
        bBreadCrumbView.setOnSequenceClickListener(this.sequenceListener);
        mAttachedBrowserActivity.updateCurrentlyDisplayedFragment(this);
    }

    @Override
    protected void onInvisible() {
        if (this.actionModeController != null) {
            this.actionModeController.finishActionMode();
        }
        if (this.scanner != null
                && this.scanner.getStatus() == AsyncTask.Status.RUNNING) {
            this.scanner.cancel(false);
        }
    }

    private void invalidatePullToRefresh() {
        if (mPullToRefreshLayout != null) {
            ActionBarPullToRefresh.from(getActivity())
                    .theseChildrenArePullable(mListView, mListView.getEmptyView())
                    .listener(this)
                    .setup(mPullToRefreshLayout);
        }
    }

    /**
     * Called from PullToRefresh
     */
    @Override
    public void onRefreshStarted(View view) {
        if (mBrowser != null) {
            mBrowser.invalidate();
        }
    }

    private void startScan() {
        if (this.scanner != null
                && this.scanner.getStatus() == AsyncTask.Status.RUNNING) {
            this.scanner.cancel(false);
        }

        this.scanner = new DirectoryScanTask(mBrowser, mPullToRefreshLayout,
                mAttachedBrowserActivity.getGetContentMimeType(), adapter);
        this.scanner.execute(mBrowser.getCurrentPath());
    }

    private int getNewId(View parent) {
        this.mPrevId++;
        while (parent.findViewById(this.mPrevId) != null) {
            this.mPrevId++;
        }
        return this.mPrevId;
    }

    @Nullable
    public Browser getBrowser() {
        return this.mBrowser;
    }

    private static final class SavedState implements Parcelable {
        final int mPrevId;
        final Parcelable mBrowserState;

        SavedState(final int prevId, final Parcelable browserState) {
            this.mPrevId = prevId;
            this.mBrowserState = browserState;
        }

        SavedState(final Parcel source) {
            this.mPrevId = source.readInt();
            this.mBrowserState = source.readParcelable(Browser.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPrevId);
            dest.writeParcelable(this.mBrowserState, 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @NotNull
            @Override
            public SavedState createFromParcel(final Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
    }
}
