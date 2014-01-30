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
package com.docd.purefm.browser;

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
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
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
    
    private final Activity activity;
    private AbsListView list;

    private ActionMode mode;

    public ActionModeController(final Activity activity) {
        this.activity = activity;
        this.multiChoiceListener = new MultiChoiceListener();
    }
    
    protected void finishActionMode() {
        if (this.mode != null) {
            this.mode.finish();
        }
    }
    
    public void setListView(AbsListView list) {
        if (this.mode != null) {
            this.mode.finish();
        }
        this.list = list;
        this.list.setMultiChoiceModeListener(this.multiChoiceListener);
    }

    private final class MultiChoiceListener implements MultiChoiceModeListener {

        final String selected = activity
                .getString(R.string._selected);

        @Override
        public boolean onPrepareActionMode(ActionMode mode,
                Menu menu) {
            menu.clear();
            activity.getMenuInflater().inflate(
                    R.menu.browser_contextual, menu);

            final SparseBooleanArray items = list.getCheckedItemPositions();
            final int checkedCount = list.getCheckedItemCount();
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);

            final ArrayList<GenericFile> toShare = new ArrayList<GenericFile>(checkedCount);
            for (int i = 0; i < items.size(); i++) {
                final int key = items.keyAt(i);
                if (items.get(key)) {
                    final GenericFile selected = (GenericFile) list.getAdapter().getItem(key);
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
                shareIntent.setDataAndType(Uri.fromFile(selectedFile),
                        MimeTypes.getMimeType(selectedFile));
                final MenuItem share = menu.findItem(R.id.menu_share);
                ((ShareActionProvider) share.getActionProvider())
                        .setShareIntent(shareIntent);
            } else {
                final ArrayList<Uri> uris = new ArrayList<Uri>(toShare.size());
                for (final GenericFile file : toShare) {
                    uris.add(Uri.fromFile(file.toFile()));
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                final MenuItem share = menu.findItem(R.id.menu_share);
                ((ShareActionProvider) share.getActionProvider())
                        .setShareIntent(shareIntent);
            }

            if (checkedCount > 1) {
                menu.removeItem(android.R.id.edit);
                menu.removeItem(R.id.properties);
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ActionModeController.this.mode = null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ActionModeController.this.mode = mode;
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode,
                MenuItem item) {
            final SparseBooleanArray items = list
                    .getCheckedItemPositions();
            switch (item.getItemId()) {
            case android.R.id.edit:
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        final DialogFragment rename = RenameFileDialog
                                .instantiate(mode, (GenericFile) list
                                        .getAdapter().getItem(key));
                        rename.show(activity.getFragmentManager(),
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
                                .instantiate((GenericFile) list
                                            .getAdapter().getItem(key));
                        mode.finish();
                        prop.show(activity.getFragmentManager(),
                                BrowserActivity.TAG_DIALOG);
                        break;
                    }
                }
                return true;

            case R.id.menu_delete:
                final List<GenericFile> files1 = new LinkedList<GenericFile>();
                final BrowserBaseAdapter adapter = (BrowserBaseAdapter) list.getAdapter();
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files1.add(adapter.getItem(key));
                    }
                }
                final DialogFragment dialog = DeleteFilesDialog.instantiate(mode, files1);
                dialog.show(activity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
                return true;

            case android.R.id.cut:
                final GenericFile[] files = new GenericFile[list
                        .getCheckedItemCount()];
                int index = -1;
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files[++index] = (GenericFile) list.getAdapter()
                                .getItem(key);
                    }
                }
                ClipBoard.cutMove(files);
                Toast.makeText(
                        activity,
                        activity.getString(R.string.cut_)
                                + (index + 1)
                                + activity
                                        .getString(R.string._files),
                        Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;

            case android.R.id.copy:
                final GenericFile[] files2 = new GenericFile[list
                        .getCheckedItemCount()];
                int index1 = -1;
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files2[++index1] = (GenericFile) list.getAdapter()
                                .getItem(key);
                    }
                }
                ClipBoard.cutCopy(files2);
                Toast.makeText(
                        activity,
                        activity.getString(R.string.copied_)
                                + (index1 + 1)
                                + activity
                                        .getString(R.string._files),
                        Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
                
            case R.id.select_all:
                for (int i = 0; i < list.getCount(); i++) {
                    list.setItemChecked(i, true);
                }
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            mode.setTitle(list.getCheckedItemCount() + selected);
            mode.invalidate();
        }
    };
}
