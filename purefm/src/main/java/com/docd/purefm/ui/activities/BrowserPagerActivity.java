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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.docd.purefm.Environment;
import com.docd.purefm.Extras;
import com.docd.purefm.PureFM;
import com.docd.purefm.R;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.adapters.BookmarksAdapter;
import com.docd.purefm.adapters.BrowserTabsAdapter;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.ui.dialogs.ProgressAlertDialogBuilder;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.ui.view.BreadCrumbTextView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Activity that holds ViewPager with BrowserFragments
 * and manages ActionBar and Key events
 *
 * @author Doctoror
 */
public final class BrowserPagerActivity extends AbstractBrowserActivity
        implements ServiceConnection, OperationsService.OperationListener {

    /**
     * Saved fragment state. This saving mechanism is used for restoring
     * the state when Activity is recreated because of theme change
     */
    private static final String EXTRA_SAVED_FRAGMENT_ADAPTER_STATE = "BrowserPagerActivity.extras.SAVED_FRAGMENT_STATE";

    private ActionBar mActionBar;
    private BreadCrumbTextView mBreadCrumbView;

    private boolean isDrawerOpened;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mShowHomeAsUp;

    private ListView mDrawerList;
    BookmarksAdapter mBookmarksAdapter;
    GenericFile currentPath;

    private BrowserTabsAdapter mPagerAdapter;

    private BrowserFragment mCurrentlyDisplayedFragment;

    private Dialog mOperationProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getBundleExtra(EXTRA_SAVED_STATE);
        }
        this.setContentView(R.layout.activity_browser);
        this.initActionBar();
        this.initView();
        this.restoreSavedState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final FragmentManager fm = getFragmentManager();
        final Fragment f = fm.findFragmentByTag(TAG_DIALOG);
        if (f != null) {
            fm.beginTransaction().remove(f).commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.invalidateOptionsMenu();
    }

    @Override
    protected void setActionBarIcon(Drawable icon) {
        getActionBar().setIcon(icon);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_SAVED_FRAGMENT_ADAPTER_STATE, mPagerAdapter.saveManualState());
    }

    private void restoreSavedState(final Bundle savedState) {
        if (savedState != null) {
            final Parcelable adapterState = savedState.getParcelable(EXTRA_SAVED_FRAGMENT_ADAPTER_STATE);
            if (adapterState != null) {
                if (mPagerAdapter != null) {
                    mPagerAdapter.restoreManualState(adapterState);
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
        PreviewHolder.getInstance(getApplicationContext()).recycle();
    }

    private void initActionBar() {
        this.mActionBar = this.getActionBar();
        if (this.mActionBar == null) {
            throw new RuntimeException("BrowserPagerActivity should have an ActionBar");
        }
        this.mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_USE_LOGO
                | ActionBar.DISPLAY_HOME_AS_UP);

        final View custom = LayoutInflater.from(this).inflate(
                R.layout.activity_browser_actionbar, null);
        this.mActionBar.setCustomView(custom);

        this.mBreadCrumbView = (BreadCrumbTextView) custom
                .findViewById(R.id.bread_crumb_view);
    }

    private void initView() {
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager);
        mPagerAdapter = new BrowserTabsAdapter(
                this.getFragmentManager());
        pager.setAdapter(mPagerAdapter);
        mPagerAdapter.setViewPager(pager);
        pager.setOffscreenPageLimit(2);

        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        final TypedArray array = obtainStyledAttributes(new int[]{R.attr.themeId});
        final int themeId = array.getInteger(0, PureFM.THEME_ID_LIGHT);
        array.recycle();

        mDrawerToggle = new BrowserActivityDrawerToggle(this, this.mDrawerLayout,
                themeId == PureFM.THEME_ID_LIGHT ?
                        R.drawable.holo_light_ic_drawer :
                        R.drawable.holo_dark_ic_drawer,
                                R.string.menu_bookmarks, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) this.findViewById(R.id.drawerList);
        mDrawerList.setAdapter(mBookmarksAdapter = new BookmarksAdapter(this));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, OperationsService.class), this,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
        if (mBookmarksAdapter.isModified()) {
            Settings.saveBookmarks(getApplicationContext(),
                    mBookmarksAdapter.getData());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.isDrawerIndicatorEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void invalidateList() {
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean isHistoryEnabled() {
        return true;
    }

    @Override
    protected BrowserFragment getCurrentlyDisplayedFragment() {
        return mCurrentlyDisplayedFragment;
    }

    @Override
    protected void setOnSequenceClickListener(BreadCrumbTextView.OnSequenceClickListener
                                                          sequenceListener) {
        if (mBreadCrumbView != null) {
            mBreadCrumbView.setOnSequenceClickListener(sequenceListener);
        }
    }

    @Override
    protected boolean shouldShowBrowserFragmentMenu() {
        return !isDrawerOpened;
    }

    @Override
    protected void setCurrentlyDisplayedFragment(final BrowserFragment fragment) {
        mCurrentlyDisplayedFragment = fragment;
    }

    /**
     * Toggles between using up button or navigation drawer icon by setting the DrawerListener
     */
    void setDrawerIndicatorEnabled(final boolean showUpButton) {
        mShowHomeAsUp = showUpButton;
        mDrawerToggle.setDrawerIndicatorEnabled(!showUpButton);
    }

    @Override
    public void onNavigate(GenericFile path) {
        invalidateOptionsMenu();
    }

    @Override
    public void onNavigationCompleted(GenericFile path) {
        currentPath = path;
        mBreadCrumbView.setFile(path.toFile());
        setDrawerIndicatorEnabled(!path.toFile().equals(Environment.sRootDirectory));
        invalidateOptionsMenu();
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
            final Intent searchIntent = new Intent(this, SearchActivity.class);
            searchIntent.putExtra(Extras.EXTRA_PATH, currentPath.getAbsolutePath());
            startActivity(searchIntent);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (this.isDrawerOpened) {
            this.getMenuInflater().inflate(R.menu.activity_bookmarks, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, @NonNull final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_bookmarks_new:
            if (currentPath != null) {
                final String path = currentPath.getAbsolutePath();
                this.mBookmarksAdapter.addItem(path);
            }
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    /**
     * Should be called by BookmarksAdapter to set current path and close the Drawer
     */
    public void setCurrentPath(GenericFile path) {
        if (this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            this.mDrawerLayout.closeDrawer(this.mDrawerList);
        }
        if (this.mCurrentlyDisplayedFragment != null) {
            final Browser browser = this.mCurrentlyDisplayedFragment.getBrowser();
            if (browser != null) {
                browser.navigate(path, true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        final boolean onFragmentBackPressed;
        onFragmentBackPressed = this.mCurrentlyDisplayedFragment != null
                && this.mCurrentlyDisplayedFragment.onBackPressed();
        if (!onFragmentBackPressed) {
                final AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage(R.string.dialog_quit_message);
                b.setCancelable(true);
                b.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PreviewHolder.getInstance(getApplicationContext()).recycle();
                                finish();
                            }
                        });
                b.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                final Dialog dialog = b.create();
                if (!this.isFinishing()) {
                    dialog.show();
                }
            }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        mBreadCrumbView.fullScrollRight();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    // =================== SERVICE CONNECTION ==================

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ((OperationsService.LocalBinder) service).setOperationListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (mOperationProgressDialog != null) {
            mOperationProgressDialog.dismiss();
            mOperationProgressDialog = null;
        }
    }

    // ================= OPERATION LISTENER ====================

    @Override
    public void onOperationStarted(@Nullable final CharSequence operationMessage,
                                   @NonNull final Intent cancelIntent) {
        if (mOperationProgressDialog == null || !mOperationProgressDialog.isShowing()) {
            mOperationProgressDialog = ProgressAlertDialogBuilder.create(this,
                    operationMessage, new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            startService(cancelIntent);
                        }
                    });
            if (!isFinishing()) {
                mOperationProgressDialog.show();
            }
        }
    }

    @Override
    public void onOperationEnded(@Nullable final Object result) {
        if (mOperationProgressDialog != null) {
            mOperationProgressDialog.dismiss();
            mOperationProgressDialog = null;
        }
    }

    /**
     * ActionBarDrawerToggle that manages display options and title
     */
    private final class BrowserActivityDrawerToggle extends ActionBarDrawerToggle {

        private BrowserActivityDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(final View drawerView) {
            super.onDrawerOpened(drawerView);
            isDrawerOpened = true;
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_USE_LOGO
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(R.string.menu_bookmarks);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(final View drawerView) {
            super.onDrawerClosed(drawerView);
            isDrawerOpened = false;
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_USE_LOGO);
            mDrawerToggle.setDrawerIndicatorEnabled(!mShowHomeAsUp);
            invalidateOptionsMenu();
        }
    }
}
