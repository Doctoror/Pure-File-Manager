package com.docd.purefm.activities;

import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.docd.purefm.R;
import com.docd.purefm.settings.Settings;

/**
 * Manages ActionBar icon. If superuser is enabled, the ActionBar icon is ic_superuser, default otherwise
 */
public abstract class SuperuserActionBarMonitoredActivity extends MonitoredActivity {

    /**
     * Current ActionBar
     */
    private ActionBar mActionBar;

    /**
     * Default Activity icon
     */
    private Drawable mDefaultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = this.getActionBar();
        try {
            mDefaultIcon = getPackageManager().getActivityIcon(this.getComponentName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mDefaultIcon = getResources().getDrawable(R.drawable.ic_fso_folder);
        }
        this.invalidateActionBarIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.invalidateActionBarIcon();
    }

    /**
     * Sets ActionBar icon to ic_superuser if superuser enabled.
     */
    protected final void invalidateActionBarIcon() {
        if (Settings.useCommandLine && Settings.su) {
            mActionBar.setIcon(R.drawable.ic_superuser);
        } else {
            mActionBar.setIcon(mDefaultIcon);
        }
    }
}
