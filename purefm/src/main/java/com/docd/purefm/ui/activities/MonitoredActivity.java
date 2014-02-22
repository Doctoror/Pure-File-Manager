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

import android.os.Bundle;

import com.docd.purefm.ActivityMonitor;

/**
 * Activity that sends information about it's lifecycle events to ActivityMonitor
 * @author Doctoror
 */
public abstract class MonitoredActivity extends ThemableActivity {

    public interface OnStopListener {
        void onActivityStop(MonitoredActivity activity);
    }

    private OnStopListener mOnStopListener;

    public void setOnStopListener(final OnStopListener l) {
        mOnStopListener = l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMonitor.onCreate(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        ActivityMonitor.onStart(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        ActivityMonitor.onStop(this);
        if (mOnStopListener != null) {
            mOnStopListener.onActivityStop(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityMonitor.onDestroy(this);
    }
}
