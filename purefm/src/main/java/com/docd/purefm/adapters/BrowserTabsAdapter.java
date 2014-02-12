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

import com.docd.purefm.ui.activities.BrowserFragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
/**
 * FragmentPagerAdapter for BrowserFragments used by BrowserPagerActivity
 *
 * @author Doctoror
 */
public final class BrowserTabsAdapter extends FragmentStatePagerAdapter {

    /**
     * Prefix for manual saved Fragment state extra
     */
    private static final String STATE_FRAGMENT_PREFIX = "BrowserTabsAdapter.state.FRAGMENT_";

    /**
     * Currently displayed Fragment reference array
     */
    private final BrowserFragment[] tabs;

    /**
     * Manually saved state to restore
     */
    private Parcelable mToRestore;

    /**
     * ViewPager that uses this adapter
     */
    private ViewPager mPager;
        
    public BrowserTabsAdapter(final FragmentManager fm) {
        super(fm);
        this.tabs = new BrowserFragment[2];
    }

    /**
     * Creates and returns Fragment for the position, adding it to reference array
     * and tries to restore manual saved state if the Fragment for last position is returned
     *
     * @param position position to return Fragment associated for
     * @return new Fragment associated with a specified position.
     */
    @Override
    public BrowserFragment getItem(final int position) {
        final BrowserFragment f = new BrowserFragment();
        this.tabs[position] = f;
        if (position == 1 && mToRestore != null) {
            doRestoreManualState(mToRestore);
        }
        return f;
    }

    /**
     * Sets ViewPager which uses this adapter.
     *
     * @param viewPager ViewPager that uses this adapter
     */
    public void setViewPager(final ViewPager viewPager) {
        mPager = viewPager;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Saves manual state.
     * This state should be manually restored by calling {@link #restoreManualState(android.os.Parcelable)}
     *
     * @return object holding the saved state
     */
    public Parcelable saveManualState() {
        final BrowserTabsAdapterState state = new BrowserTabsAdapterState();
        for (int i = 0; i < tabs.length; i++) {
            final BrowserFragment tab = tabs[i];
            if (tab != null) {
                final Bundle tabState = new Bundle();
                tab.saveManualState(tabState);
                state.fragmentStates.put(STATE_FRAGMENT_PREFIX + i, tabState);
            }
        }
        if (mPager != null) {
            state.currentPage = mPager.getCurrentItem();
        }
        return state;
    }

    /**
     * Restores manual-saved state
     *
     * @param state State saved by {@link #saveManualState()}
     */
    private void doRestoreManualState(final Parcelable state) {
        mToRestore = null;
        final BrowserTabsAdapterState savedState = (BrowserTabsAdapterState) state;
        for (int i = 0; i < tabs.length; i++) {
            final BrowserFragment tab = tabs[i];
            if (tab != null) {
                final Bundle tabState = savedState.fragmentStates.get(STATE_FRAGMENT_PREFIX + i);
                tab.restoreManualState(tabState);
            }
        }
        if (mPager != null && savedState.currentPage < getCount()) {
            mPager.setCurrentItem(savedState.currentPage);
        }
    }

    /**
     * Restores manual-saved state when all fragments were initialized
     *
     * @param state State saved by {@link #saveManualState()}
     */
    public void restoreManualState(final Parcelable state) {
        int initedFragments = 0;
        for (final Object tab : tabs) {
            if (tab != null) {
                initedFragments++;
            }
        }
        if (initedFragments == 2) {
            doRestoreManualState(state);
        } else {
            mToRestore = state;
        }
    }

    /**
     * Represents manual saved state
     */
    private static final class BrowserTabsAdapterState implements Parcelable {
        private final HashMap<String, Bundle> fragmentStates;
        private int currentPage;

        BrowserTabsAdapterState() {
            this.fragmentStates = new HashMap<String, Bundle>();
        }

        private BrowserTabsAdapterState(final Parcel source) {
            this.fragmentStates = source.readHashMap(HashMap.class.getClassLoader());
            this.currentPage = source.readInt();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeMap(this.fragmentStates);
            dest.writeInt(this.currentPage);
        }

        public static final Creator<BrowserTabsAdapterState> CREATOR = new Creator<BrowserTabsAdapterState>() {
            @NotNull
            @Override
            public BrowserTabsAdapterState createFromParcel(final Parcel source) {
                return new BrowserTabsAdapterState(source);
            }

            @Override
            public BrowserTabsAdapterState[] newArray(int size) {
                return new BrowserTabsAdapterState[size];
            }
        };
    }
}
