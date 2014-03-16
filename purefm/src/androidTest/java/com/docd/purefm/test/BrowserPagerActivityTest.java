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
package com.docd.purefm.test;

import android.test.ActivityInstrumentationTestCase2;

import com.docd.purefm.R;
import com.docd.purefm.ui.activities.BrowserPagerActivity;

/**
 * Tests {@link com.docd.purefm.ui.activities.BrowserPagerActivity}
 *
 * @author Doctoror
 */
public final class BrowserPagerActivityTest extends
        ActivityInstrumentationTestCase2<BrowserPagerActivity> {

    public BrowserPagerActivityTest() {
        super(BrowserPagerActivity.class);
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        final BrowserPagerActivity activity = getActivity();
        assertTrue(activity.getActionBar() != null);
        assertTrue(activity.getActionBar().getCustomView() != null);
        assertTrue(activity.getActionBar().getCustomView().getId() == R.id.bread_crumb_view);
        assertTrue(activity.findViewById(R.id.pager) != null);
    }
}
