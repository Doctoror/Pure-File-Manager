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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Toast;

import com.cyanogenmod.filemanager.ui.policy.IntentsActionPolicy;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.ui.activities.BrowserPagerActivity;
import com.docd.purefm.ui.dialogs.DeleteFilesDialog;
import com.docd.purefm.ui.dialogs.FilePropertiesDialog;
import com.docd.purefm.ui.dialogs.RenameFileDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PFMFileUtils;

/**
 * Controller that manages ActionMode for BrowserFragment
 *
 * @author Doctoror
 */
public final class ActionModeController {

    private final MultiChoiceModeListener multiChoiceListener;

    private final Activity mActivity;

    private AbsListView mListView;

    private ActionMode mActionMode;
    private Intent mShareIntent;
    private int mShareItemsCount;

    public ActionModeController(final Activity activity) {
        this.mActivity = activity;
        this.multiChoiceListener = new MultiChoiceListener();
    }

    public void finishActionMode() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
    }

    public void setListView(AbsListView list) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mListView = list;
        this.mListView.setMultiChoiceModeListener(this.multiChoiceListener);
    }

    private final class MultiChoiceListener implements MultiChoiceModeListener {

        @Override
        public boolean onPrepareActionMode(ActionMode mode,
                                           Menu menu) {
            menu.clear();
            mActivity.getMenuInflater().inflate(
                    R.menu.browser_contextual, menu);

            final SparseBooleanArray items = mListView.getCheckedItemPositions();
            final int checkedCount = mListView.getCheckedItemCount();

            final ArrayList<GenericFile> toShare = new ArrayList<>(checkedCount);
            for (int i = 0; i < items.size(); i++) {
                final int key = items.keyAt(i);
                if (items.get(key)) {
                    final GenericFile selected = (GenericFile) mListView.getItemAtPosition(key);
                    if (selected != null && !selected.isDirectory()) {
                        toShare.add(selected);
                    }
                }
            }

            mShareItemsCount = toShare.size();
            if (toShare.isEmpty()) {
                menu.removeItem(R.id.menu_share);
            } else if (toShare.size() == 1) {
                mShareIntent = IntentsActionPolicy.createShareIntent(mActivity, toShare.get(0));
            } else {
                mShareIntent = IntentsActionPolicy.createShareIntent(mActivity, toShare);
            }

            if (checkedCount > 1) {
                menu.removeItem(android.R.id.edit);
                menu.removeItem(R.id.properties);
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ActionModeController.this.mActionMode = null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ActionModeController.this.mActionMode = mode;
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode,
                                           final MenuItem item) {
            final SparseBooleanArray items = mListView
                    .getCheckedItemPositions();
            final int checkedItemSize = items.size();
            final Resources res = mActivity.getResources();
            switch (item.getItemId()) {
                case android.R.id.edit:
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            final RenameFileDialog rename = RenameFileDialog
                                    .instantiate(mode, (GenericFile) mListView
                                            .getItemAtPosition(key));
                            rename.show(mActivity.getFragmentManager(),
                                    BrowserPagerActivity.TAG_DIALOG);
                            return true;
                        }
                    }
                    return false;

                case R.id.properties:
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            final FilePropertiesDialog prop = FilePropertiesDialog
                                    .newInstance((GenericFile) mListView.getItemAtPosition(key));
                            mode.finish();
                            prop.show(mActivity.getFragmentManager(),
                                    BrowserPagerActivity.TAG_DIALOG);
                            break;
                        }
                    }
                    return true;

                case R.id.menu_delete:
                    final List<GenericFile> files1 = new LinkedList<>();
                    final BrowserBaseAdapter adapter = (BrowserBaseAdapter) mListView.getAdapter();
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files1.add(adapter.getItem(key));
                        }
                    }
                    final DeleteFilesDialog dialog = DeleteFilesDialog.newInstance(mode, files1);
                    dialog.show(mActivity.getFragmentManager(), BrowserPagerActivity.TAG_DIALOG);
                    return true;

                case android.R.id.cut:
                    final GenericFile[] files = new GenericFile[mListView
                            .getCheckedItemCount()];
                    int index = -1;
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files[++index] = (GenericFile) mListView.getItemAtPosition(key);
                        }
                    }
                    ClipBoard.cutMove(files);
                    Toast.makeText(
                            mActivity,
                            res.getQuantityString(
                                    R.plurals.cut_n_files_to_clipboard, index + 1, index + 1),
                             Toast.LENGTH_SHORT
                            ).show();
                    mode.finish();
                    return true;

                case android.R.id.copy:
                    final GenericFile[] files2 = new GenericFile[mListView
                            .getCheckedItemCount()];
                    int index1 = -1;
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files2[++index1] = (GenericFile) mListView.getItemAtPosition(key);
                        }
                    }
                    ClipBoard.cutCopy(files2);
                    Toast.makeText(
                            mActivity,
                            res.getQuantityString(R.plurals.copied_n_files_to_clipboard, index1 + 1,
                                    index1 + 1),
                            Toast.LENGTH_SHORT
                    ).show();
                    mode.finish();
                    return true;

                case R.id.select_all:
                    for (int i = 0; i < mListView.getCount(); i++) {
                        mListView.setItemChecked(i, true);
                    }
                    return true;

                case R.id.menu_add_to_media_store:
                    final String[] paths = new String[mListView.getCheckedItemCount()];
                    int pathsCounter = 0;
                    for (int i = 0; i < checkedItemSize; i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            paths[pathsCounter++] = PFMFileUtils.fullPath((GenericFile)
                                    mListView.getItemAtPosition(key));
                        }
                    }
                    MediaScannerConnection.scanFile(mActivity, paths, null, null);
                    mode.finish();
                    return true;

                case R.id.menu_share:
                    if (mShareIntent != null) {
                        mActivity.startActivity(mShareIntent);
                    } else {
                        Toast.makeText(mActivity, res.getQuantityText(
                                R.plurals.no_applications_can_share, mShareItemsCount),
                                        Toast.LENGTH_SHORT).show();
                    }
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                              boolean checked) {
            mode.setTitle(mActivity.getString(R.string._selected, mListView.getCheckedItemCount()));
            mode.invalidate();
        }
    }
}
