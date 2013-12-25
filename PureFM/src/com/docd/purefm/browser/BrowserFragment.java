package com.docd.purefm.browser;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.docd.purefm.utils.Cache;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.view.SequentalTextView;
import com.docd.purefm.view.SequentalTextView.OnSequenceClickListener;

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

    private SequentalTextView title;
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
                GenericFile target = Cache.get(sequence);
                if (target == null) {
                    target = FileFactory.newFile(sequence);
                }
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
        if (state != null) {
            if (state.containsKey(KEY_FILE)) {
                this.browser.setInitialPath((File) state.get(KEY_FILE));
            }
            if (state.containsKey(KEY_PREV_ID)) {
                this.prevId = state.getInt(KEY_PREV_ID);
            }
        }
        super.onCreate(state);
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
        this.parentListener = this.browserActivity.getOnNavagationListener();
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

            final MenuItem content = menu.findItem(android.R.id.content);
            this.refreshItem = menu.findItem(R.id.refresh);

            if (Settings.appearance == Settings.APPEARANCE_LIST) {
                content.setIcon(R.drawable.action_view_as_grid)
                        .setTitle(R.string.menu_view_as_grid)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                content.setIcon(R.drawable.action_view_as_list)
                        .setTitle(R.string.menu_view_as_list)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
        this.actionModeController.setBrowser(this.browser);
        this.actionModeController.setListView(this.list);
        this.actionBar.setDisplayHomeAsUpEnabled(!browser.isRoot());
        this.browser.invalidate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_FILE, this.browser.getPath().toFile());
        outState.putInt(KEY_PREV_ID, this.prevId);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
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

    public Browser getBrowser() {
        return this.browser;
    }
}
