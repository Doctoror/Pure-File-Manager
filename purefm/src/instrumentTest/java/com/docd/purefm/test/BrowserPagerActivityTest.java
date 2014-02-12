package com.docd.purefm.test;

import android.test.ActivityInstrumentationTestCase2;

import com.docd.purefm.R;
import com.docd.purefm.ui.activities.BrowserPagerActivity;

public final class BrowserPagerActivityTest extends ActivityInstrumentationTestCase2<BrowserPagerActivity> {

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
