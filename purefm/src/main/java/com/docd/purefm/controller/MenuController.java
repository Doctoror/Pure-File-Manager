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
package com.docd.purefm.controller;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.ui.activities.AbstractBrowserActivity;
import com.docd.purefm.ui.activities.SearchActivity;
import com.docd.purefm.settings.SettingsActivity;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.ui.dialogs.CreateDirectoryDialog;
import com.docd.purefm.ui.dialogs.CreateFileDialog;
import com.docd.purefm.ui.dialogs.PartitionInfoDialog;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.PasteTaskExecutor;
import com.docd.purefm.utils.FileSortType;

import android.app.DialogFragment;
import android.content.Intent;
import android.view.MenuItem;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Controls Browser menu
 *
 * @author Doctoror
 */
public final class MenuController {

    private final AbstractBrowserActivity mActivity;
    private final Browser mBrowser;

    private BrowserBaseAdapter mBrowserAdapter;

    public MenuController(@NonNull final AbstractBrowserActivity activity,
                          @NonNull final Browser browser) {
        this.mActivity = activity;
        this.mBrowser = browser;
    }

    public void setBrowserAdapter(@Nullable final BrowserBaseAdapter mBrowserAdapter) {
        this.mBrowserAdapter = mBrowserAdapter;
    }

    public boolean onMenuItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.mBrowser.up();
                return true;

            case android.R.id.paste:
                final PasteTaskExecutor ptc = new PasteTaskExecutor(mActivity,
                        mBrowser.getCurrentPath());
                ptc.start();
                return true;

            case android.R.id.content:
                final Settings settings = Settings.getInstance(mActivity);
                switch (settings.getListAppearance()) {
                    case LIST:
                        settings.setListAppearance(Settings.ListAppearance.GRID);
                        break;

                    case GRID:
                        settings.setListAppearance(Settings.ListAppearance.LIST);
                        break;

                    default:
                        throw new IllegalArgumentException("Unexpected ListAppearance: " +
                        settings.getListAppearance());
                }
                mActivity.invalidateList();
                return true;

            case R.id.menu_search:
                final Intent searchIntent = new Intent(mActivity, SearchActivity.class);
                searchIntent.putExtra(Extras.EXTRA_FILE, mBrowser.getCurrentPath());
                mActivity.startActivity(searchIntent);
                return true;

            case R.id.menu_settings:
                mActivity.startActivityForResult(new Intent(mActivity, SettingsActivity.class), AbstractBrowserActivity.REQUEST_CODE_SETTINGS);
                return true;

            case R.id.menu_folder_new:
                final DialogFragment cd = CreateDirectoryDialog.instantiate(mBrowser.getCurrentPath().toFile());
                cd.show(mActivity.getFragmentManager(), AbstractBrowserActivity.TAG_DIALOG);
                return true;

            case R.id.menu_file_new:
                final DialogFragment cf = CreateFileDialog.instantiate(mBrowser.getCurrentPath().toFile());
                cf.show(mActivity.getFragmentManager(), AbstractBrowserActivity.TAG_DIALOG);
                return true;

            case R.id.menu_partition:
                final DialogFragment pid = PartitionInfoDialog.instantiate(mBrowser.getCurrentPath());
                pid.show(mActivity.getFragmentManager(), AbstractBrowserActivity.TAG_DIALOG);
                return true;

            case R.id.menu_sort_name_asc:
                mBrowserAdapter.setCompareType(FileSortType.NAME_ASC);
                return true;

            case R.id.menu_sort_name_desc:
                mBrowserAdapter.setCompareType(FileSortType.NAME_DESC);
                return true;

            case R.id.menu_sort_type_asc:
                mBrowserAdapter.setCompareType(FileSortType.TYPE_ASC);
                return true;

            case R.id.menu_sort_type_desc:
                mBrowserAdapter.setCompareType(FileSortType.TYPE_DESC);
                return true;

            case R.id.menu_sort_date_asc:
                mBrowserAdapter.setCompareType(FileSortType.DATE_ASC);
                return true;

            case R.id.menu_sort_date_desc:
                mBrowserAdapter.setCompareType(FileSortType.DATE_DESC);
                return true;

            case R.id.menu_sort_size_asc:
                mBrowserAdapter.setCompareType(FileSortType.SIZE_ASC);
                return true;

            case R.id.menu_sort_size_desc:
                mBrowserAdapter.setCompareType(FileSortType.SIZE_DESC);
                return true;

            default:
                return false;
        }
    }
}
