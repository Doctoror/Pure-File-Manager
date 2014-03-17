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

import com.cyanogenmod.filemanager.util.AIDHelper;
import com.docd.purefm.Environment;
import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PureFMTextUtils;
import com.docd.purefm.utils.ThemeUtils;
import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;

public final class FilePropertiesDialog extends DialogFragment {

    public static FilePropertiesDialog newInstance(final @NotNull GenericFile f) {
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, f);

        final FilePropertiesDialog fpd = new FilePropertiesDialog();
        fpd.setArguments(extras);
        return fpd;
    }

    private GenericFile file;
    private PropertiesAdapter mAdapter;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        this.file = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        mAdapter = new PropertiesAdapter(activity, file);
        builder.setIcon(ThemeUtils.getDrawable(activity, R.attr.action_info));
        builder.setTitle(file.getName());
        builder.setNeutralButton(R.string.close, null);
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final FilePermissionsPagerItem fragment = (FilePermissionsPagerItem)
                        mAdapter.getItem(1);
                fragment.applyPermissions(getActivity());
            }
        });
        final View content = activity.getLayoutInflater()
                .inflate(R.layout.dialog_properties_container, null);
        this.initView(content);
        builder.setView(content);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setVisibility(View.GONE);
            }
        });
        return dialog;
    }

    private void initView(@NotNull final View view) {
        final ViewPager pager = (ViewPager) view.findViewById(R.id.tabsContainer);
        pager.setAdapter(mAdapter);

        final CompoundButton tab1 = (CompoundButton) view.findViewById(R.id.tab1);
        final CompoundButton tab2 = (CompoundButton) view.findViewById(R.id.tab2);

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tab1.setChecked(position == 0);
                tab2.setChecked(position == 1);
                ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setVisibility(position == 0 ||
                                !((FilePermissionsPagerItem) mAdapter.getItem(1)).areBoxesEnabled()
                                        ? View.GONE : View.VISIBLE);
            }
        });

        tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab1.setChecked(true);
                tab2.setChecked(false);
                pager.setCurrentItem(0);
            }
        });

        tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab2.setChecked(true);
                tab1.setChecked(false);
                pager.setCurrentItem(1);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.onStop();
    }

    private interface PagerItem {

        void onStart();

        void onStop();

        @NotNull
        View onCreateView(@NotNull LayoutInflater inflater);
    }

    static final class FilePermissionsPagerItem implements PagerItem, CompoundButton.OnCheckedChangeListener {

        private LoadFsTask mTask;
        View mView;

        FilePermissionsPagerItem(final GenericFile file) {
            mFile = file;
        }

        @NotNull
        @Override
        public View onCreateView(@NotNull final LayoutInflater inflater) {
            mView = inflater.inflate(R.layout.dialog_permissions, null);
            initView(mView);
            return mView;
        }

        @Override
        public void onStart() {
            if (mView != null) {
                if (mTask == null) {
                    mTask = new LoadFsTask(this);
                }
                if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
                    mTask.execute(mFile);
                }
            }
        }

        @Override
        public void onStop() {
            if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
                mTask.cancel(false);
            }
        }

        /**
         * Permissions that file had when FilePermissionsController was created
         */
        private Permissions mInputPermissions;

        /**
         * Currently modified permissions
         */
        private Permissions mModifiedPermissions;

        /**
         * Target file
         */
        private final GenericFile mFile;

        /**
         * User: read
         */
        private CompoundButton ur;

        /**
         * User: write
         */
        private CompoundButton uw;

        /**
         * User: execute
         */
        private CompoundButton ux;

        /**
         * Group: read
         */
        private CompoundButton gr;

        /**
         * Group: write
         */
        private CompoundButton gw;

        /**
         * Group: execute
         */
        private CompoundButton gx;

        /**
         * Others: read
         */
        private CompoundButton or;

        /**
         * Others: write
         */
        private CompoundButton ow;

        /**
         * Others: execute
         */
        private CompoundButton ox;

        private void initView(View table) {
            final TextView owner = (TextView) table.findViewById(R.id.owner);
            final TextView group = (TextView) table.findViewById(R.id.group);

            final Permissions p = mFile.getPermissions();
            this.ur = (CompoundButton) table.findViewById(R.id.uread);
            this.uw = (CompoundButton) table.findViewById(R.id.uwrite);
            this.ux = (CompoundButton) table.findViewById(R.id.uexecute);
            this.gr = (CompoundButton) table.findViewById(R.id.gread);
            this.gw = (CompoundButton) table.findViewById(R.id.gwrite);
            this.gx = (CompoundButton) table.findViewById(R.id.gexecute);
            this.or = (CompoundButton) table.findViewById(R.id.oread);
            this.ow = (CompoundButton) table.findViewById(R.id.owrite);
            this.ox = (CompoundButton) table.findViewById(R.id.oexecute);

            this.ur.setChecked(p.ur);
            this.uw.setChecked(p.uw);
            this.ux.setChecked(p.ux);
            this.gr.setChecked(p.gr);
            this.gw.setChecked(p.gw);
            this.gx.setChecked(p.gx);
            this.or.setChecked(p.or);
            this.ow.setChecked(p.ow);
            this.ox.setChecked(p.ox);

            this.ur.setOnCheckedChangeListener(this);
            this.uw.setOnCheckedChangeListener(this);
            this.ux.setOnCheckedChangeListener(this);
            this.gr.setOnCheckedChangeListener(this);
            this.gw.setOnCheckedChangeListener(this);
            this.gx.setOnCheckedChangeListener(this);
            this.or.setOnCheckedChangeListener(this);
            this.ow.setOnCheckedChangeListener(this);
            this.ox.setOnCheckedChangeListener(this);

            if (mFile instanceof CommandLineFile) {
                final CommandLineFile f = (CommandLineFile) mFile;
                final SparseArray<String> aids = AIDHelper.getAIDs(table.getContext(), false);
                if (aids != null) {
                    owner.setText(aids.get(f.getOwner()));
                    group.setText(aids.get(f.getGroup()));
                }
            } else {
                this.disableBoxes();
            }

            this.mInputPermissions = p;
            this.mModifiedPermissions = p;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.mModifiedPermissions = new Permissions(
                    this.ur.isChecked(), this.uw.isChecked(), this.ux.isChecked(),
                    this.gr.isChecked(), this.gw.isChecked(), this.gx.isChecked(),
                    this.or.isChecked(), this.ow.isChecked(), this.ox.isChecked());
        }

        private void disableBoxes() {
            this.ur.setEnabled(false);
            this.uw.setEnabled(false);
            this.ux.setEnabled(false);
            this.gr.setEnabled(false);
            this.gw.setEnabled(false);
            this.gx.setEnabled(false);
            this.or.setEnabled(false);
            this.ow.setEnabled(false);
            this.ox.setEnabled(false);
        }

        boolean areBoxesEnabled() {
            return this.ur.isEnabled();
        }

        public void applyPermissions(final Context context) {
            if (!mInputPermissions.equals(mModifiedPermissions)) {
                final ApplyTask task = new ApplyTask(context, mModifiedPermissions);
                task.execute(this.mFile);
            }
        }

        private static final class LoadFsTask extends AsyncTask<GenericFile, Void, String> {

            private final WeakReference<FilePermissionsPagerItem> mItemRef;

            LoadFsTask(@NotNull final FilePermissionsPagerItem item) {
                this.mItemRef = new WeakReference<>(item);
            }

            @Override
            protected String doInBackground(final GenericFile... params) {
                return PureFMFileUtils.resolveFileSystem(params[0]);
            }

            @Override
            protected void onPostExecute(final String fsType) {
                final FilePermissionsPagerItem item = mItemRef.get();
                if (item != null) {
                    if (fsType == null || fsType.equals("vfat") || fsType.equals("fuse")
                            || fsType.equals("ntfs") || fsType.equals("msdos") ||
                                    fsType.equals("sdcardfs")) {
                        item.disableBoxes();
                    }
                    item.mView.findViewById(android.R.id.progress).setVisibility(View.GONE);
                    item.mView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                }
            }
        }

        private static final class ApplyTask extends AsyncTask<GenericFile, Void, Boolean> {

            private final Context context;
            private final Permissions target;

            private ApplyTask(Context context, Permissions target) {
                this.context = context;
                this.target = target;
            }

            @Override
            protected Boolean doInBackground(final GenericFile... params) {
                final String path = params[0].getAbsolutePath();
                final boolean remount = Environment.needsRemount(path);
                if (remount) {
                    RootTools.remount(path, "RW");
                }
                try {
                    return params[0].applyPermissions(this.target);
                } finally {
                    if (remount) {
                        RootTools.remount(path, "RO");
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Toast.makeText(this.context, this.context.getText(result ?
                        R.string.permissions_changed : R.string.applying_failed), Toast.LENGTH_SHORT).show();
            }

        }
    }

    static final class FilePropertiesPagerItem implements PagerItem {

        private final GenericFile mFile;
        private final Resources mResources;
        private PropertiesTask mTask;
        private View mView;

        FilePropertiesPagerItem(@NotNull final GenericFile file,
                                @NotNull final Resources resources) {
            mFile = file;
            mResources = resources;
        }

        @NotNull
        @Override
        public View onCreateView(@NotNull final LayoutInflater inflater) {
            mView = inflater.inflate(R.layout.dialog_properties, null);
            return mView;
        }

        @Override
        public void onStart() {
            if (mView != null) {
                if (mTask == null) {
                    mTask = new PropertiesTask(mView, mResources);
                }
                if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
                    mTask.execute(mFile);
                }
            }
        }

        @Override
        public void onStop() {
            if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
                mTask.cancel(false);
            }
        }

        private static final class FileProperties {
            final CharSequence mFileName;
            final CharSequence mParentPath;
            final CharSequence mTypeText;
            final CharSequence mMimeTypeText;
            final CharSequence mFileSizeText;
            final CharSequence mLastModifiedText;

            private FileProperties(@NotNull final CharSequence fileName,
                                   @NotNull CharSequence parentPath,
                                   @NotNull CharSequence typeText,
                                   @Nullable CharSequence mimeTypeText,
                                   @NotNull CharSequence fileSizeText,
                                   @NotNull CharSequence lastModifiedText) {
                this.mFileName = fileName;
                this.mParentPath = parentPath;
                this.mTypeText = typeText;
                this.mMimeTypeText = mimeTypeText;
                this.mFileSizeText = fileSizeText;
                this.mLastModifiedText = lastModifiedText;
            }
        }

        private static final class PropertiesTask extends AsyncTask<GenericFile, Void, FileProperties> {
            private final WeakReference<View> mViewRef;
            private final Resources mResources;

            PropertiesTask(@NotNull final View view, @NotNull final Resources res) {
                this.mViewRef = new WeakReference<View>(view);
                this.mResources = res;
            }

            @NotNull
            @Override
            protected FileProperties doInBackground(final GenericFile... params) {
                final GenericFile file = params[0];
                GenericFile par = file.getParentFile();
                if (par == null) {
                    par = FileFactory.newFile(Environment.rootDirectory);
                }
                String parentPath;
                try {
                    parentPath = par.getCanonicalPath();
                } catch (IOException e) {
                    parentPath = par.getAbsolutePath();
                }


                final CharSequence typeText = file.isSymlink() ?
                        mResources.getText(R.string.type_symlink) :
                        file.isDirectory() ? mResources.getText(R.string.type_directory) :
                                mResources.getText(R.string.type_file);

                return new FileProperties(file.getName(),
                        parentPath,
                        typeText,
                        file.getMimeType(),
                        PureFMFileUtils.byteCountToDisplaySize(file.lengthTotal()),
                        PureFMTextUtils.humanReadableDate(file.lastModified(), file instanceof CommandLineFile));
            }

            @Override
            protected void onPostExecute(final @NotNull FileProperties fileProperties) {
                final View view = mViewRef.get();
                if (view == null) {
                    return;
                }
                final TextView name = (TextView) view.findViewById(R.id.name);
                name.setText(fileProperties.mFileName);

                final TextView parent = (TextView) view.findViewById(R.id.location);
                parent.setText(fileProperties.mParentPath);

                final TextView type = (TextView) view.findViewById(R.id.type);
                type.setText(fileProperties.mTypeText);

                if (fileProperties.mMimeTypeText != null) {
                    final TextView mime = (TextView) view.findViewById(R.id.mime);
                    mime.setText(fileProperties.mMimeTypeText);
                }

                final TextView size = (TextView) view.findViewById(R.id.size);
                size.setText(fileProperties.mFileSizeText);

                final TextView mod = (TextView) view.findViewById(R.id.modified);
                mod.setText(fileProperties.mLastModifiedText);

                view.findViewById(android.R.id.progress).setVisibility(View.GONE);
                view.findViewById(R.id.content).setVisibility(View.VISIBLE);
            }
        }
    }

    private static final class PropertiesAdapter extends PagerAdapter {

        private final LayoutInflater mLayoutInflater;
        private final GenericFile mFile;
        private final PagerItem[] mItems;

        private PropertiesAdapter(@NotNull final Activity context,
                                  @NotNull final GenericFile file) {
            mLayoutInflater = context.getLayoutInflater();
            mFile = file;
            mItems = new PagerItem[]{
                    new FilePropertiesPagerItem(mFile, context
                            .getApplicationContext().getResources()),
                    new FilePermissionsPagerItem(mFile)
            };
        }

        void onStart() {
            for (final PagerItem item : mItems) {
                item.onStart();
            }
        }

        void onStop() {
            for (final PagerItem item : mItems) {
                item.onStop();
            }
        }

        @NotNull
        PagerItem getItem(final int position) {
            return mItems[position];
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final PagerItem item = mItems[position];
            final View view = item.onCreateView(mLayoutInflater);
            container.addView(view);
            item.onStart();
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
