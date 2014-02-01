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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Toast;

import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.browser.BrowserActivity;
import com.docd.purefm.ui.dialogs.DeleteFilesDialog;
import com.docd.purefm.ui.dialogs.FilePropertiesDialog;
import com.docd.purefm.ui.dialogs.RenameFileDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MimeTypes;

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

        final String mSelected = mActivity
                .getString(R.string._selected);

        @Override
        public boolean onPrepareActionMode(ActionMode mode,
                                           Menu menu) {
            menu.clear();
            mActivity.getMenuInflater().inflate(
                    R.menu.browser_contextual, menu);

            final SparseBooleanArray items = mListView.getCheckedItemPositions();
            final int checkedCount = mListView.getCheckedItemCount();
            mShareIntent = new Intent(Intent.ACTION_SEND);

            final ArrayList<GenericFile> toShare = new ArrayList<GenericFile>(checkedCount);
            for (int i = 0; i < items.size(); i++) {
                final int key = items.keyAt(i);
                if (items.get(key)) {
                    final GenericFile selected = (GenericFile) mListView.getAdapter().getItem(key);
                    if (!selected.isDirectory()) {
                        toShare.add(selected);
                    }
                    break;
                }
            }

            if (toShare.isEmpty()) {
                menu.removeItem(R.id.menu_share);
            } else if (toShare.size() == 1) {
                final File selectedFile = toShare.get(0).toFile();
                mShareIntent.setDataAndType(Uri.fromFile(selectedFile),
                        MimeTypes.getMimeType(selectedFile));
            } else {
                final ArrayList<Uri> uris = new ArrayList<Uri>(toShare.size());
                for (final GenericFile file : toShare) {
                    uris.add(Uri.fromFile(file.toFile()));
                }
                mShareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
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
        public boolean onActionItemClicked(ActionMode mode,
                                           MenuItem item) {
            final SparseBooleanArray items = mListView
                    .getCheckedItemPositions();
            switch (item.getItemId()) {
                case android.R.id.edit:
                    for (int i = 0; i < items.size(); i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            final DialogFragment rename = RenameFileDialog
                                    .instantiate(mode, (GenericFile) mListView
                                            .getAdapter().getItem(key));
                            rename.show(mActivity.getFragmentManager(),
                                    BrowserActivity.TAG_DIALOG);
                            return true;
                        }
                    }
                    return false;

                case R.id.properties:
                    for (int i = 0; i < items.size(); i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            final DialogFragment prop = FilePropertiesDialog
                                    .instantiate((GenericFile) mListView
                                            .getAdapter().getItem(key));
                            mode.finish();
                            prop.show(mActivity.getFragmentManager(),
                                    BrowserActivity.TAG_DIALOG);
                            break;
                        }
                    }
                    return true;

                case R.id.menu_delete:
                    final List<GenericFile> files1 = new LinkedList<GenericFile>();
                    final BrowserBaseAdapter adapter = (BrowserBaseAdapter) mListView.getAdapter();
                    for (int i = 0; i < items.size(); i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files1.add(adapter.getItem(key));
                        }
                    }
                    final DialogFragment dialog = DeleteFilesDialog.instantiate(mode, files1);
                    dialog.show(mActivity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
                    return true;

                case android.R.id.cut:
                    final GenericFile[] files = new GenericFile[mListView
                            .getCheckedItemCount()];
                    int index = -1;
                    for (int i = 0; i < items.size(); i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files[++index] = (GenericFile) mListView.getAdapter()
                                    .getItem(key);
                        }
                    }
                    ClipBoard.cutMove(files);
                    Toast.makeText(
                            mActivity,
                            mActivity.getString(R.string.cut_)
                                    + (index + 1)
                                    + mActivity
                                    .getString(R.string._files),
                            Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;

                case android.R.id.copy:
                    final GenericFile[] files2 = new GenericFile[mListView
                            .getCheckedItemCount()];
                    int index1 = -1;
                    for (int i = 0; i < items.size(); i++) {
                        final int key = items.keyAt(i);
                        if (items.get(key)) {
                            files2[++index1] = (GenericFile) mListView.getAdapter()
                                    .getItem(key);
                        }
                    }
                    ClipBoard.cutCopy(files2);
                    Toast.makeText(
                            mActivity,
                            mActivity.getString(R.string.copied_)
                                    + (index1 + 1)
                                    + mActivity
                                    .getString(R.string._files),
                            Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;

                case R.id.select_all:
                    for (int i = 0; i < mListView.getCount(); i++) {
                        mListView.setItemChecked(i, true);
                    }
                    return true;

                case R.id.menu_share:
                    mActivity.startActivity(Intent.createChooser(mShareIntent,
                            mActivity.getText(R.string.menu_share)));
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            mode.setTitle(mListView.getCheckedItemCount() + mSelected);
            mode.invalidate();
        }
    }

    ;
}
