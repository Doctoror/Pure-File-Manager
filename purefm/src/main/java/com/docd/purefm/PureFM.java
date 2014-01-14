package com.docd.purefm;

import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.TextUtil;

import android.app.Application;

public final class PureFM extends Application implements ActivityMonitor.OnActivitiesOpenedListener {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityMonitor.init(this);
        Environment.init(this);
        Settings.init(this, this.getResources());
        PreviewHolder.initialize(this);
        TextUtil.init(this);
        ensureNoShellUsedIfNoBusybox();
        ActivityMonitor.addOnActivitiesOpenedListener(this);
    }

    private void ensureNoShellUsedIfNoBusybox() {
        if (Settings.useCommandLine) {
            if (!Environment.hasBusybox()) {
                Settings.setUseCommandLine(this, false);
                if (Settings.su) {
                    Settings.setAllowRoot(this, false);
                }
            }
        }
    }

    @Override
    public void onActivitiesOpen() {
        //rescan for environment changes
        Environment.init(this);
        ensureNoShellUsedIfNoBusybox();
    }

    @Override
    public void onActivitiesClosed() {

    }
}
