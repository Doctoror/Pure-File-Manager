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
package com.docd.purefm.ui.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ListView;
import android.widget.TextView;

import com.cyanogenmod.filemanager.util.MediaHelper;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.adapters.BrowserGridAdapter;
import com.docd.purefm.adapters.BrowserListAdapter;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.controller.ActionModeController;
import com.docd.purefm.controller.MenuController;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.DirectoryScanTask;
import com.docd.purefm.ui.fragments.UserVisibleHintFragment;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PFMFileUtils;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.ui.view.BreadCrumbTextView.OnSequenceClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;


/**
 * Fragment manages file List, menu and ActionMode.
 * @author Doctoror
 */
public final class BrowserFragment extends UserVisibleHintFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_BROWSER = "BrowserFragment.state.mBrowser";

    //private ActionBar actionBar;
    private AbstractBrowserActivity mAttachedBrowserActivity;
    private ActionModeController actionModeController;
    private MenuController menuController;

    private Browser mBrowser;
    private BrowserBaseAdapter mAdapter;
    private OnNavigateListener mParentOnNavigateListener;

    private OnSequenceClickListener mOnSequenceListener;

    private SwipeRefreshLayout mSwipeRefreshLayoutList;
    private SwipeRefreshLayout mSwipeRefreshLayoutEmpty;

    private AbsListView mListView;
    private View mMainProgress;

    private DirectoryScanTask mScannerTask;

    private boolean mRefreshFlag;

    /**
     * If Browser is not yet initialized, initial state will be saved to this field
     */
    private Parcelable mBrowserInitialState;

    private int mPrevId;
    private boolean firstRun;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mListView instanceof GridView) {
            ((GridView) mListView).setNumColumns(ThemeUtils.getInteger(
                    getActivity().getTheme(), R.attr.browserGridColumns));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof AbstractBrowserActivity) {
            mBrowser = new Browser((AbstractBrowserActivity) activity,
                    ((AbstractBrowserActivity) activity).isHistoryEnabled());
        } else {
            throw new IllegalStateException(
                    "BrowserFragment should be attached only to BrowserPagerActivity");
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
                    mRefreshFlag = true;
                }
            }

            @Override
            public void onNavigationCompleted(GenericFile path) {
                if (mMainProgress.getVisibility() == View.VISIBLE) {
                    mMainProgress.setVisibility(View.GONE);
                }
                if (firstRun) {
                    firstRun = false;
                }
                if (mRefreshFlag) {
                    mRefreshFlag = false;
                }
                if (mParentOnNavigateListener != null) {
                    mParentOnNavigateListener.onNavigationCompleted(path);
                }
            }
        });

        mOnSequenceListener = new OnSequenceClickListener() {
            @Override
            public void onSequenceClick(String sequence) {
                final GenericFile target = FileFactory.newFile(sequence);
                mBrowser.navigate(target, true);
            }
        };

        mAttachedBrowserActivity = (AbstractBrowserActivity) activity;
        menuController = new MenuController(this.mAttachedBrowserActivity, mBrowser);

        // needs FragmentActivity because FilePropertiesDialog uses childFragmentManager
        actionModeController = new ActionModeController(mAttachedBrowserActivity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentOnNavigateListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.dropCaches();
    }

    @Override
    public void onCreate(Bundle state) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        firstRun = true;
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
        return mBrowser != null && mBrowser.back();
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        actionModeController.setListView(mListView);
        mParentOnNavigateListener = mAttachedBrowserActivity;

        mBrowser.restoreState(mBrowserInitialState);
        mBrowserInitialState = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mAttachedBrowserActivity != null && mAttachedBrowserActivity
                .shouldShowBrowserFragmentMenu()) {
            inflater.inflate(R.menu.browser, menu);

            // TODO it returns true even on devices that don't have the physical key. Find a better method to detect search hardware button
            //if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_SEARCH)) {
            //    menu.removeItem(R.id.menu_search);
            //}

            final MenuItem content = menu.findItem(android.R.id.content);

            if (Settings.appearance == Settings.APPEARANCE_LIST) {
                content.setIcon(ThemeUtils.getDrawable(this.mAttachedBrowserActivity, R.attr.ic_menu_view_as_grid))
                        .setTitle(R.string.menu_view_as_grid)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                content.setIcon(ThemeUtils.getDrawable(this.mAttachedBrowserActivity, R.attr.ic_menu_view_as_list))
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
        return menuController.onMenuItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_browser, null);
        initList(inflater, parent);
        firstRun = true;
        return parent;
    }

    private void initList(@NotNull final LayoutInflater inflater, @NotNull final View parent) {
        if (mListView != null) {
            mListView.getEmptyView().setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        }
        mMainProgress = parent.findViewById(android.R.id.progress);
        mSwipeRefreshLayoutList = (SwipeRefreshLayout) inflater.inflate(
                Settings.appearance == Settings.APPEARANCE_LIST ? R.layout.browser_listview :
                        R.layout.browser_gridview, (ViewGroup) parent.findViewById(
                        R.id.list_container)).findViewById(R.id.browser_list_swipe_refresh);
        mSwipeRefreshLayoutEmpty = (SwipeRefreshLayout) parent.findViewById(android.R.id.empty);

        mListView = (AbsListView) mSwipeRefreshLayoutList.findViewById(android.R.id.list);
        if (mListView instanceof ListView) {
            mAdapter = new BrowserListAdapter(getActivity());
        } else {
            mAdapter = new BrowserGridAdapter(getActivity());
        }

        menuController.setBrowserAdapter(this.mAdapter);

        mListView.setId(this.getNewId(parent));
        mListView.setEmptyView(parent.findViewById(android.R.id.empty));
        mListView.setAdapter(this.mAdapter);
        mListView.getEmptyView().setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                final GenericFile target = (GenericFile) (av
                        .getItemAtPosition(pos));
                if (target.isDirectory()) {
                    mBrowser.navigate(target, true);
                } else {
                    if (mAttachedBrowserActivity.getGetContentMimeType() == null) {
                        PFMFileUtils.openFile(mAttachedBrowserActivity, target.toFile());
                    } else {
                        final Intent intent = new Intent();
                        intent.setData(getResultUriForFileFromIntent(mAttachedBrowserActivity
                                .getContentResolver(), target.toFile(), mAttachedBrowserActivity
                                        .getIntent()));
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

        mSwipeRefreshLayoutList.setOnRefreshListener(this);
        mSwipeRefreshLayoutEmpty.setOnRefreshListener(this);

        mSwipeRefreshLayoutList.setColorScheme(R.color.holo_light_selected,
                R.color.holo_light_selected,
                R.color.holo_light_selected,
                R.color.holo_light_selected);
        mSwipeRefreshLayoutEmpty.setColorScheme(R.color.holo_light_selected,
                R.color.holo_light_selected,
                R.color.holo_light_selected,
                R.color.holo_light_selected);
    }

    private void onFirstInvalidate() {
        actionModeController.setListView(mListView);
        mBrowser.invalidate();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveManualState(outState);
    }

    @Override
    protected void onVisible() {
        if (firstRun || mRefreshFlag) {
            onFirstInvalidate();
        } else {
            mParentOnNavigateListener.onNavigationCompleted(mBrowser
                    .getCurrentPath());
        }
        mAttachedBrowserActivity.setOnSequenceClickListener(mOnSequenceListener);
        mAttachedBrowserActivity.setCurrentlyDisplayedFragment(this);
    }

    @Override
    protected void onInvisible() {
        if (actionModeController != null) {
            actionModeController.finishActionMode();
        }
        if (mScannerTask != null
                && mScannerTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScannerTask.cancel(false);
        }
    }

    @Override
    public void onRefresh() {
        if (mBrowser != null) {
            mBrowser.invalidate();
        }
    }

    private void startScan() {
        if (mScannerTask != null
                && mScannerTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScannerTask.cancel(false);
        }

        mScannerTask = new DirectoryScanTask(mBrowser,
                mAttachedBrowserActivity.getGetContentMimeType(), mAdapter,
                        mSwipeRefreshLayoutList, mSwipeRefreshLayoutEmpty);
        mScannerTask.execute(mBrowser.getCurrentPath());
    }

    private int getNewId(View parent) {
        mPrevId++;
        while (parent.findViewById(mPrevId) != null) {
            mPrevId++;
        }
        return mPrevId;
    }

    @Nullable
    public Browser getBrowser() {
        return mBrowser;
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
