package com.docd.purefm.adapters;

import com.docd.purefm.browser.BrowserFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public final class BrowserTabsAdapter extends FragmentStatePagerAdapter {
    
    private final BrowserFragment[] tabs;
        
    public BrowserTabsAdapter(FragmentManager fm) {
        super(fm);
        this.tabs = new BrowserFragment[2];
    }

    @Override
    public BrowserFragment getItem(int position) {
        BrowserFragment f = this.tabs[position];
        if (f == null) {
            f = new BrowserFragment();
        }
        return f;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
    
    /**
     * For fragments being recreated in notifyDataSetChanged
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
