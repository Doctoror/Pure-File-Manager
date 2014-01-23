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

import android.app.ActionBar;
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
import com.docd.purefm.activities.BrowserActivity;
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
import com.docd.purefm.view.SequentialTextView;
import com.docd.purefm.view.SequentialTextView.OnSequenceClickListener;

import org.jetbrains.annotations.Nullable;

/**
 * Fragment manages file List, menu and ActionMode.
 * @author Doctoror
 */
public final class BrowserFragment extends Fragment {

    private static final String KEY_FILE = "KEY_SAVED_FILE";
    private static final String KEY_PREV_ID = "KEY_PREVIOUS_ID";

    private ActionBar actionBar;
    private BrowserActivity browserActivity;
    private ActionModeController actionModeController;
    private MenuController menuController;

    private Browser browser;
    private BrowserBaseAdapter adapter;
    private OnNavigateListener parentListener;

    private SequentialTextView title;
    private OnSequenceClickListener sequenceListener;

    private AbsListView list;
    private View menuProgress;
    private View mainProgress;

    private DirectoryScanTask scanner;
    private String mimeType;
    private MenuItem refreshItem;

    private boolean refreshFlag;

    private int prevId;
    private boolean firstRun;
    private boolean isVisible;

    private void ensureGridViewColumns(Configuration config) {
        if (this.list instanceof GridView) {
            ((GridView) this.list)
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
            this.browser = new Browser((BrowserActivity) activity);
        } else {
            throw new IllegalStateException("BrowserFragment should be attached only to BrowserActivity");
        }
        this.browser.setOnNavigateListener(new OnNavigateListener() {

            @Override
            public void onNavigate(GenericFile path) {
                parentListener.onNavigate(path);
                if (refreshItem == null) {
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
                browser.navigate(target, true);
            }
        };

        this.browserActivity = (BrowserActivity) activity;
        this.menuController = new MenuController(this.browserActivity, browser);
        this.actionModeController = new ActionModeController(this.browserActivity);
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
                if (this.browser != null) {
                    this.browser.setInitialPath(initialPath);
                } else {
                    //TODO save initial path
                }
            }
            if (state.containsKey(KEY_PREV_ID)) {
                this.prevId = state.getInt(KEY_PREV_ID);
            }
        }
    }

    public void saveManualState(final Bundle outState) {
        outState.putSerializable(KEY_FILE, this.browser.getPath().toFile());
        outState.putInt(KEY_PREV_ID, this.prevId);
    }

    public boolean onBackPressed() {
        return this.browser != null && this.browser.back();
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        this.ensureGridViewColumns(getResources().getConfiguration());
        this.actionModeController.setListView(list);

        this.actionBar = this.browserActivity.getActionBar();
        this.title = this.browserActivity.getTitleView();
        this.parentListener = this.browserActivity.createOnNavigationListener();
        if (this.isVisible() && this.isAdded() && this.isVisible) {
            this.actionBar.setDisplayHomeAsUpEnabled(!browser.isRoot());
            this.title.setOnSequenceClickListener(this.sequenceListener);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (this.browserActivity != null && !this.browserActivity.isDrawerOpened()) {
            inflater.inflate(R.menu.browser, menu);

            // TODO it returns true even on devices that don't have the physical key. Find a better method to detect search hardware button
            //if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_SEARCH)) {
            //    menu.removeItem(R.id.menu_search);
            //}

            final MenuItem content = menu.findItem(android.R.id.content);
            this.refreshItem = menu.findItem(R.id.refresh);

            if (Settings.appearance == Settings.APPEARANCE_LIST) {
                content.setIcon(ThemeUtils.getDrawable(this.browserActivity, R.attr.action_view_as_grid))
                        .setTitle(R.string.menu_view_as_grid)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                content.setIcon(ThemeUtils.getDrawable(this.browserActivity, R.attr.action_view_as_list))
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
        if (this.list != null) {
            this.list.getEmptyView().setVisibility(View.GONE);
            this.list.setVisibility(View.GONE);
        }
        this.mainProgress = parent.findViewById(android.R.id.progress);

        if (Settings.appearance == Settings.APPEARANCE_LIST) {
            View vs = parent.findViewById(android.R.id.list);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.list = (AbsListView) vs;
            this.adapter = new BrowserListAdapter(this.getActivity());
        } else {
            View vs = parent.findViewById(R.id.grid);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.list = (AbsListView) vs;
            this.adapter = new BrowserGridAdapter(this.getActivity());
        }

        this.menuController.setAdapter(this.adapter);

        this.list.setId(this.getNewId(parent));
        this.list.setEmptyView(parent.findViewById(android.R.id.empty));
        this.list.setAdapter(this.adapter);
        this.list.getEmptyView().setVisibility(View.GONE);
        this.list.setVisibility(View.GONE);

        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                final GenericFile target = (GenericFile) (av
                        .getItemAtPosition(pos));
                if (target.isDirectory()) {
                    browser.navigate(target, true);
                } else {
                    final Activity a = getActivity();
                    if (mimeType == null) {
                        PureFMFileUtils.openFile(a, target.toFile());
                    } else {
                        final Uri result = Uri.fromFile(target.toFile());
                        final Intent intent = new Intent();
                        intent.setData(result);
                        a.setResult(Activity.RESULT_OK, intent);
                        a.finish();
                    }
                }
            }
        });
    }

    private void onFirstInvalidate() {
        this.actionModeController.setListView(this.list);
        this.actionBar.setDisplayHomeAsUpEnabled(!browser.isRoot());
        this.browser.invalidate();
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
                this.parentListener.onNavigationCompleted(this.browser
                        .getPath());
            }
            this.actionBar.setDisplayHomeAsUpEnabled(!browser.isRoot());
            this.title.setOnSequenceClickListener(this.sequenceListener);
            this.browserActivity.updateCurrentlyDisplayedFragment(this);
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
                this.browserActivity.updateCurrentlyDisplayedFragment(this);
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

        this.scanner = new DirectoryScanTask(browser, refreshItem,
                menuProgress, mimeType, adapter);
        this.scanner.execute(this.browser.getPath());
    }

    public void setMimeType(final String type) {
        this.mimeType = type;
        this.list
                .setChoiceMode(type == null ? AbsListView.CHOICE_MODE_MULTIPLE_MODAL
                        : AbsListView.CHOICE_MODE_NONE);
    }

    @Nullable
    public Browser getBrowser() {
        return this.browser;
    }
}
