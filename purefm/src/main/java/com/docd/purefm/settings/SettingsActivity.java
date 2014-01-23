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
package com.docd.purefm.settings;

import com.docd.purefm.R;
import com.docd.purefm.activities.SuperuserActionBarMonitoredActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public final class SettingsActivity extends SuperuserActionBarMonitoredActivity {

    private boolean needInvalidate;
    private int createdWithThemeId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createdWithThemeId = Settings.theme;
        this.setContentView(R.layout.activity_settings);
        this.getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP |
                ActionBar.DISPLAY_SHOW_TITLE |
                ActionBar.DISPLAY_USE_LOGO);
    }
    
    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBeforeFinish();
                return super.onMenuItemSelected(featureId, item);
                
            default:
                return false;
        }
    }
    
    @Override
    public void onBackPressed() {
        this.onBeforeFinish();
        super.onBackPressed();
    }

    void proxyRestart() {
        restart();
    }

    private void onBeforeFinish() {
        this.setResult(this.needInvalidate ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
    }
    
    public void notifyNeedInvalidate() {
        this.needInvalidate = true;
    }

    void proxyInvalidateActionBarIcon() {
        invalidateActionBarIcon();
    }
}
