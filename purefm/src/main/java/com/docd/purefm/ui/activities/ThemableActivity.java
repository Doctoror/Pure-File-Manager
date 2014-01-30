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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.docd.purefm.settings.Settings;

/**
 * Activity that manages theme changes
 *
 * @author Doctoror
 */
public abstract class ThemableActivity extends Activity {

    protected static final String EXTRA_SAVED_STATE = "ThemableActivity.extras.SAVED_STATE";

    private int currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentTheme = Settings.theme;
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentTheme != Settings.theme) {
            restart();
        }
    }

    protected void restart() {
        final Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        final Intent intent = new Intent(this, getClass());
        intent.putExtra(EXTRA_SAVED_STATE, outState);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
