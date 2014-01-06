package com.docd.purefm.activities;

import com.docd.purefm.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public final class SettingsActivity extends MonitoredActivity {

    private boolean needInvalidate;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        this.getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP |
                ActionBar.DISPLAY_SHOW_TITLE |
                ActionBar.DISPLAY_USE_LOGO);
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
                
            default:
                return false;
        }
    }
    
    @Override
    public void onBackPressed() {
        this.setResult(this.needInvalidate ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        this.finish();
    }
    
    public void notifyNeedInvalidate() {
        this.needInvalidate = true;
    }
    
}
