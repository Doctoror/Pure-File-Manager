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

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
/**
 * FragmentPagerAdapter for BrowserFragments used by BrowserActivity
 *
 * @author Doctoror
 */
public final class BrowserTabsAdapter extends FragmentStatePagerAdapter {

    private static final String STATE_PREFIX = "BrowserTabsAdapter.state.fragment_";
    private final BrowserFragment[] tabs;
    private Parcelable mToRestore;
        
    public BrowserTabsAdapter(final FragmentManager fm) {
        super(fm);
        this.tabs = new BrowserFragment[2];
    }

    @Override
    public BrowserFragment getItem(int position) {
        BrowserFragment f = this.tabs[position];
        if (f == null) {
            f = new BrowserFragment();
            this.tabs[position] = f;
        }
        if (position == 1 && mToRestore != null) {
            restoreManualState(mToRestore);
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

    public Parcelable saveManualState() {
        final BrowserTabsAdapterState state = new BrowserTabsAdapterState();
        for (int i = 0; i < tabs.length; i++) {
            final BrowserFragment tab = tabs[i];
            if (tab != null) {
                final Bundle tabState = new Bundle();
                tab.saveManualState(tabState);
                state.states.put(STATE_PREFIX + i, tabState);
            }
        }
        return state;
    }

    private void doRestoreManualState(final Parcelable state) {
        mToRestore = null;
        final BrowserTabsAdapterState savedState = (BrowserTabsAdapterState) state;
        for (int i = 0; i < tabs.length; i++) {
            final BrowserFragment tab = tabs[i];
            if (tab != null) {
                final Bundle tabState = savedState.get(STATE_PREFIX + i);
                tab.restoreManualState(tabState);
            }
        }
    }

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

    private static final class BrowserTabsAdapterState implements Parcelable {
        private final HashMap<String, Bundle> states;

        BrowserTabsAdapterState() {
            this.states = new HashMap<String, Bundle>();
        }

        private BrowserTabsAdapterState(final Parcel source) {
            this.states = source.readHashMap(HashMap.class.getClassLoader());
        }

        void put(final String key, final Bundle value) {
            this.states.put(key, value);
        }

        Bundle get(final String key) {
            return this.states.get(key);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeMap(this.states);
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
