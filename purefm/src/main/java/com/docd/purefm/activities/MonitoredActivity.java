package com.docd.purefm.activities;

import com.docd.purefm.ActivityMonitor;

import android.support.v4.app.FragmentActivity;

public abstract class MonitoredActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        ActivityMonitor.onStart(this);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        ActivityMonitor.onStop(this);
    }
}
