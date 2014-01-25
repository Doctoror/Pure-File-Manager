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

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ProgressBar;

import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.adapters.BrowserGridAdapter;
import com.docd.purefm.adapters.BrowserListAdapter;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.DirectoryScanTask;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.ThemeUtils;
import com.docd.purefm.view.BreadCrumbTextView;
import com.docd.purefm.view.BreadCrumbTextView.OnSequenceClickListener;

import org.jetbrains.annotations.Nullable;

/**
 * Fragment manages file List, menu and ActionMode.
 * @author Doctoror
 */
public final class BrowserFragment extends Fragment {

    private static final String KEY_FILE = "KEY_SAVED_FILE";
    private static final String KEY_PREV_ID = "KEY_PREVIOUS_ID";

    //private ActionBar actionBar;
    private BrowserActivity mAttachedBrowserActivity;
    private ActionModeController actionModeController;
    private MenuController menuController;

    private Browser mBrowser;
    private BrowserBaseAdapter adapter;
    private OnNavigateListener parentListener;

    private BreadCrumbTextView title;
    private OnSequenceClickListener sequenceListener;

    private AbsListView mListView;
    private View menuProgress;
    private View mainProgress;

    private DirectoryScanTask scanner;
    private MenuItem mRefreshMenuItem;

    private boolean refreshFlag;

    /**
     * If Browser is not yet initialized, initial path will be saved to this field
     */
    private File browserInitialPath;

    private int prevId;
    private boolean firstRun;
    private boolean isVisible;

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
        this.menuProgress = new ProgressBar(activity);

        if (activity instanceof BrowserActivity) {
            this.mBrowser = new Browser((BrowserActivity) activity);
            this.mBrowser.setInitialPath(this.browserInitialPath);
            this.browserInitialPath = null;
        } else {
            throw new IllegalStateException("BrowserFragment should be attached only to BrowserActivity");
        }
        this.mBrowser.setOnNavigateListener(new OnNavigateListener() {

            @Override
            public void onNavigate(GenericFile path) {
                parentListener.onNavigate(path);
                if (mRefreshMenuItem == null) {
                    refreshFlag = true;
                } else {
                    startScan();
                }
            }

            @Override
            public void onNavigationCompleted(GenericFile path) {
                if (mainProgress.getVisibility() == View.VISIBLE) {
                    mainProgress.setVisibility(View.GONE);
                }
                if (firstRun && isVisible) {
                    firstRun = false;
                }
                parentListener.onNavigationCompleted(path);
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
    public void onCreate(Bundle state) {
        this.setRetainInstance(true);
        this.setHasOptionsMenu(true);
        this.firstRun = true;
        restoreManualState(state);
        super.onCreate(state);
    }

    public void restoreManualState(final Bundle state) {
        if (state != null) {
            state.setClassLoader(getClass().getClassLoader());
            if (state.containsKey(KEY_FILE)) {
                final File initialPath = (File) state.get(KEY_FILE);
                if (this.mBrowser != null) {
                    this.mBrowser.setInitialPath(initialPath);
                } else {
                    this.browserInitialPath = initialPath;
                }
            }
            if (state.containsKey(KEY_PREV_ID)) {
                this.prevId = state.getInt(KEY_PREV_ID);
            }
        }
    }

    public void saveManualState(final Bundle outState) {
        outState.putSerializable(KEY_FILE, this.mBrowser.getPath().toFile());
        outState.putInt(KEY_PREV_ID, this.prevId);
    }

    public boolean onBackPressed() {
        return this.mBrowser != null && this.mBrowser.back();
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        this.ensureGridViewColumns(getResources().getConfiguration());
        this.actionModeController.setListView(mListView);

        this.title = this.mAttachedBrowserActivity.getTitleView();
        this.parentListener = this.mAttachedBrowserActivity.createOnNavigationListener();
        if (this.isVisible() && this.isAdded() && this.isVisible) {
            this.title.setOnSequenceClickListener(this.sequenceListener);
        }
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
            this.mRefreshMenuItem = menu.findItem(R.id.refresh);

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

            if (refreshFlag) {
                refreshFlag = false;
                startScan();
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
                        intent.setData(result);
                        mAttachedBrowserActivity.setResult(Activity.RESULT_OK, intent);
                        mAttachedBrowserActivity.finish();
                    }
                }
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
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && this.isAdded()) {
            if (this.firstRun && this.isVisible()) {
                this.onFirstInvalidate();
            } else {
                this.parentListener.onNavigationCompleted(this.mBrowser
                        .getPath());
            }
            this.title.setOnSequenceClickListener(this.sequenceListener);
            this.mAttachedBrowserActivity.updateCurrentlyDisplayedFragment(this);
        } else {
            if (this.actionModeController != null) {
                this.actionModeController.finishActionMode();
            }
        }
        this.isVisible = isVisibleToUser;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.firstRun && this.isVisible) {
            if (this.isAdded()) {
                this.mAttachedBrowserActivity.updateCurrentlyDisplayedFragment(this);
            }
            this.onFirstInvalidate();
        }
    }

    @Override
    public void onStop() {
        if (this.scanner != null
                && this.scanner.getStatus() == AsyncTask.Status.RUNNING) {
            this.scanner.cancel(false);
        }
        this.actionModeController.finishActionMode();
        super.onStop();
    }

    private int getNewId(View parent) {
        this.prevId++;
        while (parent.findViewById(this.prevId) != null) {
            this.prevId++;
        }
        return this.prevId;
    }

    private void startScan() {
        if (this.scanner != null
                && this.scanner.getStatus() == AsyncTask.Status.RUNNING) {
            this.scanner.cancel(false);
        }

        this.scanner = new DirectoryScanTask(mBrowser, mRefreshMenuItem,
                menuProgress, mAttachedBrowserActivity.getGetContentMimeType(), adapter);
        this.scanner.execute(mBrowser.getPath());
    }

    @Nullable
    public Browser getBrowser() {
        return this.mBrowser;
    }
}
