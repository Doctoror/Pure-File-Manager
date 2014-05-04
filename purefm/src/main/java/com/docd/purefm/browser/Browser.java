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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverCache;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.MultiListenerFileObserver;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.ui.activities.AbstractBrowserActivity;
import com.docd.purefm.utils.PFMFileUtils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Browser manages current path and navigation
 * @author Doctoror
 */
public final class Browser implements MultiListenerFileObserver.OnEventListener {

    private static final int OBSERVER_EVENTS = FileObserver.CREATE |
            FileObserver.DELETE_SELF |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF;

    private static Handler sHandler;
    private final Context mContext;
    private final Settings mSettings;

    public interface OnNavigateListener {
        void onNavigate(GenericFile path);
        void onNavigationCompleted(GenericFile path);
    }

    private final ArrayDeque<String> mHistory;

    private final FileObserverCache mObserverCache;
    private MultiListenerFileObserver mCurrentPathObserver;
    private GenericFile mCurrentPath;
    private GenericFile mPreviousPath;
    private OnNavigateListener mNavigateListener;

    private Runnable mLastRunnable;
    private boolean mHistoryEnabled;

    private final ResolveInitialPathTask mInitialPathTask;

    public Browser(@NonNull final AbstractBrowserActivity activity, final boolean historyEnabled) {
        if (sHandler == null) {
            sHandler = new Handler(activity.getMainLooper());
        }
        mContext = activity;
        mSettings = Settings.getInstance(activity);
        mHistoryEnabled = historyEnabled;
        mObserverCache = FileObserverCache.getInstance();
        mHistory = new ArrayDeque<>(historyEnabled ? 15 : 0);

        final String home = Settings.getInstance(activity).getHomeDirectory();
        mInitialPathTask = new ResolveInitialPathTask(this, mSettings, home);
        mInitialPathTask.execute();
    }

//    void setInitialPath(final GenericFile currentPath) {
//        mCurrentPath = currentPath;
//        mCurrentPathObserver = mObserverCache.getOrCreate(
//                currentPath, OBSERVER_EVENTS);
//        mCurrentPathObserver.addOnEventListener(this);
//        mCurrentPathObserver.startWatching();
//    }

    private void cancelInitialPathLoading() {
        if (mInitialPathTask.getStatus() == AsyncTask.Status.RUNNING) {
            mInitialPathTask.cancel(true);
        }
    }

    public void setHistoryEnabled(final boolean enabled) {
        if (mHistoryEnabled != enabled) {
            mHistoryEnabled = enabled;
            mHistory.clear();
        }
    }

    public Parcelable saveInstanceState() {
        return new SavedState(mHistory,
                mCurrentPath != null ? mCurrentPath.getAbsolutePath() : null,
                mPreviousPath != null ? mPreviousPath.getAbsolutePath() : null);
    }

    public void restoreState(@Nullable final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        if (savedState != null) {
            cancelInitialPathLoading();
            mHistory.clear();
            mHistory.addAll(savedState.mHistory);


            if (savedState.mCurrentPath != null) {
                final GenericFile savedStateCurrentFile = FileFactory.newFile(
                        mSettings, savedState.mCurrentPath);
                if (savedStateCurrentFile.exists() &&
                        savedStateCurrentFile.isDirectory()) {
                    mCurrentPath = savedStateCurrentFile;
                }
            }

            if (savedState.mPreviousPath != null) {
                final GenericFile savedStatePreviousFile = FileFactory.newFile(
                        mSettings, savedState.mPreviousPath);
                if (savedStatePreviousFile.exists() &&
                        savedStatePreviousFile.isDirectory()) {
                    mPreviousPath = savedStatePreviousFile;
                }
            }
            invalidate();
        }
    }

    public void setOnNavigateListener(OnNavigateListener l) {
        this.mNavigateListener = l;
    }

    @Nullable
    public GenericFile getCurrentPath() {
        return this.mCurrentPath;
    }
    
    public void onScanFinished(GenericFile requested) {
        mCurrentPath = requested;
        if (mNavigateListener != null) {
            mNavigateListener.onNavigationCompleted(requested);
        }
        if (mCurrentPathObserver != null) {
            mCurrentPathObserver.stopWatching();
            mCurrentPathObserver.removeOnEventListener(this);
        }
        if (requested.exists() && requested.isDirectory()) {
            mCurrentPathObserver = mObserverCache.getOrCreate(requested, OBSERVER_EVENTS);
            mCurrentPathObserver.addOnEventListener(this);
            mCurrentPathObserver.startWatching();
        } else {
            mHistory.remove(requested.getAbsolutePath());
            final GenericFile parent = resolveExistingParent(requested);
            navigate(parent, true);
        }
    }
    
    public void onScanCancelled(final boolean navigateToPrevious) {
        if (navigateToPrevious && mPreviousPath != null) {
            mCurrentPath = this.mPreviousPath;
        }
    }

    public boolean goBack(final boolean invalidate) {
        cancelInitialPathLoading();
        if (mPreviousPath != null) {
            mCurrentPath = this.mPreviousPath;
            if (invalidate) {
                invalidate();
            }
            return true;
        }
        return false;
    }

    @NonNull
    private GenericFile resolveExistingParent(@NonNull final GenericFile file) {
        GenericFile parent = file.getParentFile();
        if (parent == null) {
            return file;
        }
        while (!parent.exists() || !parent.isDirectory()) {
            mHistory.remove(parent.getAbsolutePath());
            final GenericFile previousParent = parent;
            parent = parent.getParentFile();
            if (parent == null) {
                return previousParent;
            }
        }
        return parent;
    }

    public void navigate(@NonNull final GenericFile target, final boolean addToHistory) {
        cancelInitialPathLoading();
        if (target.exists()) {
            if (target.isDirectory()) {
                if (!target.equals(mCurrentPath)) {
                    mPreviousPath = mCurrentPath;
                    mCurrentPath = target;
                    if (addToHistory && mHistoryEnabled && mPreviousPath != null) {
                        mHistory.push(mPreviousPath.getAbsolutePath());
                    }
                    this.invalidate();
                }
            } else {
                Log.w("Browser", "The target is not a directory");
                Toast.makeText(mContext, R.string.target_is_not_a_directory,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w("Browser",
                    "Trying to navigate to non-existing directory. Searching for existing parent");
            Toast.makeText(mContext, mContext.getString(R.string.directory_not_exists,
                    PFMFileUtils.fullPath(target)), Toast.LENGTH_SHORT).show();
            final GenericFile parent = resolveExistingParent(target);
            if (!parent.equals(target)) {
                navigate(parent, addToHistory);
            }
        }
    }
    
    public boolean back() {
        cancelInitialPathLoading();
        if (!this.mHistory.isEmpty()) {
            GenericFile f = FileFactory.newFile(mSettings, mHistory.pop());
            while (!this.mHistory.isEmpty() && !f.exists()) {
                f = FileFactory.newFile(mSettings, this.mHistory.pop());
            }
            if (f.exists() && f.isDirectory()) {
                this.navigate(f, false);
                return true;
            }
        }
        return false;
    }
    
    public void up() {
        cancelInitialPathLoading();
        if (this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.sRootDirectory)) {
            return;
        }
        final GenericFile parent = resolveExistingParent(mCurrentPath);
        this.mHistory.push(parent.getAbsolutePath());
        this.navigate(parent, true);
    }
    
    public void invalidate() {
        if (mNavigateListener != null && mCurrentPath != null) {
            mNavigateListener.onNavigate(mCurrentPath);
        }
    }

    public boolean isRoot() {
        return this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.sRootDirectory);
    }

    @Override
    public void onEvent(int event, String pathString) {
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.MOVE_SELF:
            case FileObserver.DELETE_SELF:
                sHandler.removeCallbacks(mLastRunnable);
                final GenericFile parent = resolveExistingParent(mCurrentPath);
                sHandler.post(mLastRunnable = new NavigateRunnable(
                        Browser.this, parent, true));
                break;

            case FileObserver.CREATE:
            case FileObserver.MOVED_TO:
                sHandler.removeCallbacks(mLastRunnable);
                sHandler.post(mLastRunnable = new InvalidateRunnable(this));
                break;
        }
    }

    private static final class SavedState implements Parcelable {
        final ArrayDeque<String> mHistory;
        final String mCurrentPath;
        final String mPreviousPath;

        SavedState(@NonNull final ArrayDeque<String> history,
                   @Nullable final String currentPath,
                   @Nullable final String previousPath) {
            this.mHistory = history;
            this.mCurrentPath = currentPath;
            this.mPreviousPath = previousPath;
        }

        @SuppressWarnings("unchecked")
        SavedState(final Parcel source) {
            this.mHistory = (ArrayDeque<String>) source.readSerializable();
            this.mCurrentPath = source.readString();
            this.mPreviousPath = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.mHistory);
            dest.writeString(this.mCurrentPath);
            dest.writeString(this.mPreviousPath);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @NonNull
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private static final class ResolveInitialPathTask extends AsyncTask<Void, Void, GenericFile> {

        private final WeakReference<Browser> mBrowserReference;
        private final String mHomeDirectory;
        private final Settings mSettings;

        private ResolveInitialPathTask(@NonNull final Browser browser,
                                       @NonNull final Settings settings,
                                       @Nullable final String homeDirectory) {
            this.mBrowserReference = new WeakReference<>(browser);
            this.mSettings = settings;
            this.mHomeDirectory = homeDirectory;
        }

        @Override
        protected GenericFile doInBackground(Void... params) {
            GenericFile initialFile = null;
            if (mHomeDirectory != null) {
                final GenericFile currentFile = FileFactory.newFile(mSettings, mHomeDirectory);
                if (currentFile.exists() && currentFile.isDirectory()) {
                    initialFile = currentFile;
                }
            }
            if (initialFile == null) {
                final String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED) ||
                        state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    initialFile = FileFactory.newFile(mSettings,
                            Environment.getExternalStorageDirectory());
                }
            }
            if (initialFile == null) {
                initialFile = FileFactory.newFile(mSettings,
                        com.docd.purefm.Environment.sRootDirectory.getAbsolutePath());
            }
            return initialFile;
        }

        @Override
        protected void onPostExecute(final GenericFile genericFile) {
            final Browser browser = mBrowserReference.get();
            if (browser != null) {
                browser.navigate(genericFile, false);
            }
        }
    }

    private static final class InvalidateRunnable implements Runnable {
        private final WeakReference<Browser> mBrowserReference;
        InvalidateRunnable(final Browser browser) {
            this.mBrowserReference = new WeakReference<>(browser);
        }

        @Override
        public void run() {
            final Browser browser = this.mBrowserReference.get();
            if (browser != null) {
                browser.invalidate();
            }
        }
    }

    private static final class NavigateRunnable implements Runnable {
        private final WeakReference<Browser> mBrowserReference;
        private final GenericFile mTarget;
        private final boolean mAddToHistory;

        NavigateRunnable(final Browser browser,
                         final GenericFile target,
                         final boolean addToHistory) {
            this.mBrowserReference = new WeakReference<>(browser);
            this.mTarget = target;
            this.mAddToHistory = addToHistory;
        }

        @Override
        public void run() {
            final Browser browser = this.mBrowserReference.get();
            if (browser != null) {
                browser.navigate(this.mTarget, this.mAddToHistory);
            }
        }
    }
}
