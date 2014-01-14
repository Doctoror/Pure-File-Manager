package com.docd.purefm.activities;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.docd.purefm.R;
import com.docd.purefm.adapters.BookmarksAdapter;
import com.docd.purefm.adapters.BrowserTabsAdapter;
import com.docd.purefm.browser.BrowserFragment;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.commandline.ShellFactory;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.view.SequentalTextView;

import org.jetbrains.annotations.Nullable;

public final class BrowserActivity extends MonitoredActivity {

    public static final String TAG_DIALOG = "dialog";

    public static final int REQUEST_CODE_SETTINGS = 0;

    private ActionBar actionBar;
    private SequentalTextView title;

    private boolean isDrawerOpened;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    BookmarksAdapter bookmarksAdapter;
    GenericFile currentPath;

    private ViewPager pager;
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
    public void onLowMemory() {
        PreviewHolder.recycle();
    }

    private void initActionBar() {
        this.actionBar = this.getActionBar();
        this.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO);

        final View custom = LayoutInflater.from(this).inflate(
                R.layout.activity_browser_actionbar, null);
        this.actionBar.setCustomView(custom);

        this.title = (SequentalTextView) custom
                .findViewById(android.R.id.title);
    }

    private void initView() {
        this.pager = (ViewPager) this.findViewById(R.id.pager);
        this.pagerAdapter = new BrowserTabsAdapter(
                this.getSupportFragmentManager());
        this.pager.setAdapter(this.pagerAdapter);
        this.pager.setOffscreenPageLimit(2);

        this.drawerLayout = (DrawerLayout) this
                .findViewById(R.id.drawer);
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
        this.drawerLayout.setDrawerListener(new DrawerListener() {

            private boolean hadShowHomeAsUp;

            @Override
            public void onDrawerOpened(View arg0) {
                isDrawerOpened = true;
                this.hadShowHomeAsUp = (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) == ActionBar.DISPLAY_HOME_AS_UP;
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_USE_LOGO
                        | ActionBar.DISPLAY_SHOW_TITLE);
                actionBar.setTitle(R.string.menu_bookmarks);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View arg0) {
                isDrawerOpened = false;
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_CUSTOM
                        | ActionBar.DISPLAY_USE_LOGO
                        | (this.hadShowHomeAsUp ? ActionBar.DISPLAY_HOME_AS_UP
                                : 0));
                if (bookmarksAdapter.isModified()) {
                    Settings.saveBookmarks(getApplicationContext(),
                            bookmarksAdapter.getData());
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View arg0, float arg1) {
            }

            @Override
            public void onDrawerStateChanged(int arg0) {
            }

        });

        this.drawerList = (ListView) this.findViewById(R.id.drawerList);
        this.drawerList.setAdapter(bookmarksAdapter = new BookmarksAdapter(this,
                Settings.getBookmarks(getApplicationContext())));
    }

    public void invalidateList() {
        this.pagerAdapter.notifyDataSetChanged();
    }

    @Nullable
    public SequentalTextView getTitleView() {
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
                actionBar.setDisplayHomeAsUpEnabled(!path.toFile().equals(
                        this.root));
                invalidateOptionsMenu();
            }

        };
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (this.isDrawerOpened) {
            this.getMenuInflater().inflate(R.menu.activity_bookmarks, menu);
            return true;
        } else {
            this.getMenuInflater().inflate(R.menu.activity_browser, menu);
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
            
        case R.id.menu_drawer:
            this.drawerLayout.openDrawer(this.drawerList);
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
                this.currentlyDisplayedFragment.getBrowser().navigate(path, true);
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
            b.create().show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
}
