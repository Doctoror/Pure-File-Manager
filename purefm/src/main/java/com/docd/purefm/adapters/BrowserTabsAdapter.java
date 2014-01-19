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
package com.docd.purefm.adapters;

import com.docd.purefm.browser.BrowserFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * FragmentPagerAdapter for BrowserFragments used by BrowserActivity
 *
 * @author Doctoror
 */
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
