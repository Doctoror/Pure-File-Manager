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
package com.docd.purefm.activities;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BookmarksAdapter;
import com.docd.purefm.adapters.BrowserTabsAdapter;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.browser.BrowserFragment;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.view.SequentialTextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Activity that holds ViewPager with BrowserFragments
 * and manages ActionBar and Key events
 *
 * @author Doctoror
 */
public final class BrowserActivity extends SuperuserActionBarMonitoredActivity {

    public static final String TAG_DIALOG = "dialog";

    public static final int REQUEST_CODE_SETTINGS = 0;

    private ActionBar actionBar;
    private SequentialTextView title;

    private boolean isDrawerOpened;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mShowHomeAsUp;

    private ListView drawerList;
    BookmarksAdapter bookmarksAdapter;
    GenericFile currentPath;

    private BrowserTabsAdapter pagerAdapter;

    private final Object currentlyDisplayedFragmentLock;
    private BrowserFragment currentlyDisplayedFragment;

    public BrowserActivity() {
        this.currentlyDisplayedFragmentLock = new Object();
    }

    /**
     * If not null it means we are in GET_CONTENT mode
     */
    private String mimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browser);
        this.checkIntentAction(getIntent());
        this.initActionBar();
        this.initView();
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
    public void onLowMemory() {
        PreviewHolder.recycle();
    }

    private void initActionBar() {
        this.actionBar = this.getActionBar();
        if (this.actionBar == null) {
            throw new RuntimeException("BrowserActivity should have an ActionBar");
        }
        this.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_USE_LOGO
                | ActionBar.DISPLAY_HOME_AS_UP);

        final View custom = LayoutInflater.from(this).inflate(
                R.layout.activity_browser_actionbar, null);
        this.actionBar.setCustomView(custom);

        this.title = (SequentialTextView) custom
                .findViewById(android.R.id.title);
    }

    private void initView() {
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager);
        this.pagerAdapter = new BrowserTabsAdapter(
                this.getSupportFragmentManager());
        pager.setAdapter(this.pagerAdapter);
        pager.setOffscreenPageLimit(2);

        this.drawerLayout = (DrawerLayout) this
                .findViewById(R.id.drawer);
        this.drawerLayout.setDrawerShadow(R.drawable.holo_light_drawer_shadow,
				GravityCompat.START);
        this.mDrawerToggle = new BrowserActivityDrawerToggle(this, this.drawerLayout,
                R.drawable.holo_light_ic_drawer, R.string.menu_bookmarks, R.string.app_name);
        this.drawerLayout.setDrawerListener(this.mDrawerToggle);

        this.drawerList = (ListView) this.findViewById(R.id.drawerList);
        this.drawerList.setAdapter(bookmarksAdapter = new BookmarksAdapter(this,
                Settings.getBookmarks(getApplicationContext())));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //if (!mShowHomeAsUp) {
            mDrawerToggle.syncState();
        //}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (!mShowHomeAsUp && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    public void invalidateList() {
        this.pagerAdapter.notifyDataSetChanged();
    }

    @Nullable
    public SequentialTextView getTitleView() {
        return this.title;
    }

    public boolean isDrawerOpened() {
        return isDrawerOpened;
    }

    public void updateCurrentlyDisplayedFragment(final BrowserFragment fragment) {
        synchronized (this.currentlyDisplayedFragmentLock) {
            this.currentlyDisplayedFragment = fragment;
        }
    }

    /**
     * Toggles between using up button or navigation drawer icon by setting the DrawerListener
     */
    void setActionBarDrawerListener(final boolean showUpButton) {
        mShowHomeAsUp = showUpButton;
        mDrawerToggle.setDrawerIndicatorEnabled(!showUpButton);
    }

    public OnNavigateListener createOnNavigationListener() {
        return new OnNavigateListener() {

            private final File root = File.listRoots()[0];

            @Override
            public void onNavigate(GenericFile path) {
                invalidateOptionsMenu();
            }

            @Override
            public void onNavigationCompleted(GenericFile path) {
                currentPath = path;
                title.setFile(path.toFile());
                setActionBarDrawerListener(!path.toFile().equals(
                        this.root));
                invalidateOptionsMenu();
            }

        };
    }

    @Override
    public boolean onKeyUp(int keyCode, @NotNull KeyEvent event) {
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
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_bookmarks_new:
            final String path = this.currentPath.getAbsolutePath();
            if (path != null) {
                this.bookmarksAdapter.addItem(path);
            }
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        this.checkIntentAction(newIntent);
    }

    private void setCurrentMimeType() {
        synchronized (this.currentlyDisplayedFragmentLock) {
            if (this.currentlyDisplayedFragment != null) {
                this.currentlyDisplayedFragment.setMimeType(mimeType);
            }
        }
    }

    /**
     * Should be called by BookmarksAdapter to set current path and close the Drawer
     */
    public void setCurrentPath(GenericFile path) {
        if (this.drawerLayout.isDrawerOpen(this.drawerList)) {
            this.drawerLayout.closeDrawer(this.drawerList);
        }
        synchronized (this.currentlyDisplayedFragmentLock) {
            if (this.currentlyDisplayedFragment != null) {
                final Browser browser = this.currentlyDisplayedFragment.getBrowser();
                if (browser != null) {
                    browser.navigate(path, true);
                }
            }
        }
    }

    private void checkIntentAction(Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_GET_CONTENT)) {
            mimeType = intent.getType();
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "*/*";
            }
            this.setCurrentMimeType();
        }
    }

    @Override
    public void onBackPressed() {
        final boolean onFragmentBackPressed;
        synchronized (this.currentlyDisplayedFragmentLock) {
            onFragmentBackPressed = this.currentlyDisplayedFragment != null
                    && this.currentlyDisplayedFragment.onBackPressed();
        }
        if (!onFragmentBackPressed) {
            final AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(R.string.dialog_quit_message);
            b.setCancelable(true);
            b.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PreviewHolder.recycle();
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
        this.title.fullScrollRight();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private final class BrowserActivityDrawerToggle extends ActionBarDrawerToggle {

        private BrowserActivityDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(final View drawerView) {
            super.onDrawerOpened(drawerView);
            isDrawerOpened = true;
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_USE_LOGO
                    | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setTitle(R.string.menu_bookmarks);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(final View drawerView) {
            super.onDrawerClosed(drawerView);
            isDrawerOpened = false;
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_USE_LOGO);
            if (bookmarksAdapter.isModified()) {
                Settings.saveBookmarks(getApplicationContext(),
                        bookmarksAdapter.getData());
            }
            invalidateOptionsMenu();
        }
    }
}
