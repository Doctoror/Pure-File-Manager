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

import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.ui.view.BreadCrumbTextView;

import android.support.annotation.Nullable;

/**
 * Base BrowserActivity that can hold one or more BrowserFragment
 *
 * @author Doctoror
 */
public abstract class AbstractBrowserActivity extends ActionBarIconThemableActivity
        implements Browser.OnNavigateListener {

    public static final int REQUEST_CODE_SETTINGS = 0;

    public static final String TAG_DIALOG = "dialog";


    @Override
    public void onNavigate(GenericFile path) {

    }

    @Override
    public void onBackPressed() {
        final boolean onFragmentBackPressed;
        final BrowserFragment fragment = getCurrentlyDisplayedFragment();
            onFragmentBackPressed = fragment != null
                && fragment.onBackPressed();

        if (!onFragmentBackPressed) {
            onBackPressedConfirmed();
        }
    }

    /**
     * Returns currently displayed BrowserFragment
     *
     * @return currently displayed BrowserFragment
     */
    @Nullable
    protected abstract BrowserFragment getCurrentlyDisplayedFragment();

    /**
     * Sets currently displayed fragment
     *
     * @param fragment BrowserFragment to set
     */
    protected abstract void setCurrentlyDisplayedFragment(BrowserFragment fragment);

    /**
     * Set OnSequenceClickListener to BreadCrumbTextView
     *
     * @param sequenceListener OnSequenceListener to set
     */
    protected abstract void setOnSequenceClickListener(BreadCrumbTextView.OnSequenceClickListener sequenceListener);

    /**
     * Returns mime type if GET_CONTENT mode is set
     *
     * @return mime type if GET_CONTENT mode is set
     */
    protected String getGetContentMimeType() {
        return null;
    }

    /**
     * Perform action if onBackPressed from BrowserFragment confirmed
     */
    protected void onBackPressedConfirmed() {
        finish();
    }

    /**
     * Returns true if BrowserFragment can show it's menu
     *
     * @return true if BrowserFragment can show it's menu
     */
    protected abstract boolean shouldShowBrowserFragmentMenu();

    /**
     * Refresh content list
     */
    public abstract void invalidateList();

    /**
     * Should return true if history is enabled
     */
    public abstract boolean isHistoryEnabled();
}
