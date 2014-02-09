package com.docd.purefm.ui.activities;

import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.ui.view.BreadCrumbTextView;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractBrowserActivity extends ActionBarIconMonitoredActivity
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
}
