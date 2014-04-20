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
package com.docd.purefm.ui.dialogs;

import org.apache.commons.io.FileUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PFMFileUtils;
import com.docd.purefm.utils.StatFsCompat;
import com.docd.purefm.utils.ThemeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public final class PartitionInfoDialog extends DialogFragment {
    
    public static DialogFragment instantiate(final GenericFile file) {
        final Bundle args = new Bundle();
        args.putSerializable(Extras.EXTRA_FILE, file);
        
        final PartitionInfoDialog d = new PartitionInfoDialog();
        d.setArguments(args);
        return d;
    }
    
    private GenericFile mFile;
    private PartitionInfoTask mTask;
    private View mView;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        if (args == null) {
            throw new RuntimeException(
                    "Arguments were not supplied. The DialogFragment should be obtained by instantiate(GenericFile) method");
        }
        this.mFile = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = this.getActivity();
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        mView = activity.getLayoutInflater().inflate(R.layout.dialog_partition_info, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(ThemeUtils.getDrawableNonNull(activity, R.attr.ic_menu_info));
        builder.setTitle(R.string.menu_partition);
        builder.setView(mView);
        builder.setNeutralButton(R.string.close, null);
        return builder.create();
        
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mTask == null) {
            mTask = new PartitionInfoTask(mView);
        }
        if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask.execute(mFile);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(false);
        }
    }

    private static final class PartitionInfo {
        final CharSequence mPath;

        final CharSequence mFsTypeText;
        final CharSequence mTotalBytesText;
        final CharSequence mBlockSizeText;
        final CharSequence mFreeBytesText;
        final CharSequence mAvailableBytesText;
        final CharSequence mUsedSpaceText;

        final long mTotalBytes;
        final long mBlockSize;
        final long mFreeBytes;
        final long mAvailableBytes;
        final long mUsedSpace;

        PartitionInfo(@NonNull final CharSequence path,
                              @Nullable final CharSequence fsTypeText,
                              final long totalBytes,
                              final long blockSize,
                              final long freeBytes,
                              final long availableBytes,
                              final long usedSpace) {
            this.mPath = path;

            this.mFsTypeText = fsTypeText;
            this.mTotalBytes = totalBytes;
            this.mBlockSize = blockSize;
            this.mFreeBytes = freeBytes;
            this.mAvailableBytes = availableBytes;
            this.mUsedSpace = usedSpace;

            this.mTotalBytesText = FileUtils.byteCountToDisplaySize(totalBytes);
            this.mBlockSizeText = Long.toString(blockSize);
            this.mFreeBytesText = FileUtils.byteCountToDisplaySize(freeBytes);
            this.mAvailableBytesText = FileUtils.byteCountToDisplaySize(availableBytes);

            if (totalBytes != 0L) {
                @SuppressWarnings("StringBufferReplaceableByString")
                final StringBuilder usage = new StringBuilder();
                usage.append(FileUtils.byteCountToDisplaySize(usedSpace));
                usage.append(' ');
                usage.append('(');
                usage.append(usedSpace * 100L / totalBytes);
                usage.append('%');
                usage.append(')');
                this.mUsedSpaceText = usage.toString();
            } else {
                this.mUsedSpaceText = null;
            }
        }
    }

    private static final class PartitionInfoTask extends
            AsyncTask<GenericFile, Void, PartitionInfo> {

        private final WeakReference<View> mViewRef;

        PartitionInfoTask(@NonNull final View view) {
            this.mViewRef = new WeakReference<>(view);
        }

        @NonNull
        @Override
        protected PartitionInfo doInBackground(final GenericFile... params) {
            final String path = PFMFileUtils.fullPath(params[0]);
            final StatFsCompat statFs = new StatFsCompat(path);
            final long valueTotal = statFs.getTotalBytes();
            final long valueAvail = statFs.getAvailableBytes();
            final long valueUsed = valueTotal - valueAvail;
            return new PartitionInfo(
                    path,
                    PFMFileUtils.resolveFileSystem(path),
                    valueTotal,
                    statFs.getBlockSizeLong(),
                    statFs.getFreeBytes(),
                    valueAvail,
                    valueUsed
            );
        }

        @Override
        protected void onPostExecute(final @NonNull PartitionInfo partitionInfo) {
            final View view = mViewRef.get();
            if (view != null) {
                final TextView title = (TextView) view.findViewById(R.id.location);
                title.setText(partitionInfo.mPath);

                if (partitionInfo.mFsTypeText != null) {
                    final TextView fs = (TextView) view.findViewById(R.id.filesystem);
                    fs.setText(partitionInfo.mFsTypeText);
                } else {
                    final View fileSystemRow = view.findViewById(R.id.filesystem_row);
                    fileSystemRow.setVisibility(View.GONE);
                }

                if (partitionInfo.mTotalBytes != 0L) {
                    final TextView total = (TextView) view.findViewById(R.id.total);
                    total.setText(partitionInfo.mTotalBytesText);
                }

                if (partitionInfo.mBlockSize != 0L) {
                    final TextView block = (TextView) view.findViewById(R.id.block_size);
                    block.setText(partitionInfo.mBlockSizeText);
                }

                if (partitionInfo.mFreeBytes != 0L) {
                    final TextView free = (TextView) view.findViewById(R.id.free);
                    free.setText(partitionInfo.mFreeBytesText);
                }

                if (partitionInfo.mAvailableBytes != 0L) {
                    final TextView avail = (TextView) view.findViewById(R.id.available);
                    avail.setText(partitionInfo.mAvailableBytesText);
                }

                if (partitionInfo.mUsedSpace != 0L) {
                    final TextView used = (TextView) view.findViewById(R.id.used);
                    used.setText(partitionInfo.mUsedSpaceText);
                }
                view.findViewById(android.R.id.progress).setVisibility(View.GONE);
                view.findViewById(R.id.content).setVisibility(View.VISIBLE);
            }
        }
    }
}
