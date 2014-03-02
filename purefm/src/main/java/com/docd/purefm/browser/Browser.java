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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public interface OnNavigateListener {
        void onNavigate(GenericFile path);
        void onNavigationCompleted(GenericFile path);
    }

    private final ArrayDeque<String> mHistory;

    private FileObserverCache mObserverCache;
    private MultiListenerFileObserver mCurrentPathObserver;
    private GenericFile mCurrentPath;
    private GenericFile mPreviousPath;
    private OnNavigateListener mNavigateListener;

    private Runnable mLastRunnable;
    private boolean mHistoryEnabled;

    public Browser(@NotNull final AbstractBrowserActivity activity, final boolean historyEnabled) {
        if (sHandler == null) {
            sHandler = new Handler(activity.getMainLooper());
        }
        mContext = activity;
        mHistoryEnabled = historyEnabled;
        mObserverCache = FileObserverCache.getInstance();
        mHistory = new ArrayDeque<String>(historyEnabled ? 15 : 0);
        final String home = Settings.getHomeDirectory(activity);
        final String state = Environment.getExternalStorageState();
        if (home != null) {
            mCurrentPath = FileFactory.newFile(home);
            if (!mCurrentPath.exists()) {
                mCurrentPath = null;
            }
        }
        if (mCurrentPath == null && (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            mCurrentPath = FileFactory.newFile(Environment.getExternalStorageDirectory());
        }
        if (mCurrentPath == null) {
            mCurrentPath = FileFactory.newFile(com.docd.purefm.Environment.rootDirectory.getAbsolutePath());
        }
        if (mCurrentPath != null) {
            mCurrentPathObserver = mObserverCache.getOrCreate(
                    mCurrentPath.getAbsolutePath(), OBSERVER_EVENTS);
            mCurrentPathObserver.addOnEventListener(this);
            mCurrentPathObserver.startWatching();
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

    public void restoreState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        if (savedState != null) {
            mHistory.clear();
            mHistory.addAll(savedState.mHistory);


            if (savedState.mCurrentPath != null) {
                final GenericFile savedStateCurrentFile = FileFactory.newFile(
                        savedState.mCurrentPath);
                if (savedStateCurrentFile.exists() &&
                        savedStateCurrentFile.isDirectory()) {
                    mCurrentPath = savedStateCurrentFile;
                }
            }

            if (savedState.mPreviousPath != null) {
                final GenericFile savedStatePreviousFile = FileFactory.newFile(
                        savedState.mPreviousPath);
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
            mCurrentPathObserver = mObserverCache.getOrCreate(requested.getAbsolutePath(), OBSERVER_EVENTS);
            mCurrentPathObserver.addOnEventListener(this);
            mCurrentPathObserver.startWatching();
        } else {
            mHistory.remove(requested);
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
        if (mPreviousPath != null) {
            mCurrentPath = this.mPreviousPath;
            if (invalidate) {
                invalidate();
            }
            return true;
        }
        return false;
    }

    private GenericFile resolveExistingParent(final GenericFile file) {
        GenericFile parent = file.getParentFile();
        if (parent == null) {
            return file;
        }
        while (!parent.exists() || !parent.isDirectory()) {
            mHistory.remove(parent);
            final GenericFile previousParent = parent;
            parent = parent.getParentFile();
            if (parent == null) {
                return previousParent;
            }
        }
        return parent;
    }

    public void navigate(final GenericFile target, boolean addToHistory) {
        if (target.exists()) {
            if (target.isDirectory()) {
                if (!this.mCurrentPath.equals(target)) {
                    mPreviousPath = this.mCurrentPath;
                    mCurrentPath = target;
                    if (addToHistory) {
                        this.mHistory.push(mPreviousPath.getAbsolutePath());
                    }
                    this.invalidate();
                }
            } else {
                Log.w("Browser", "The target is not a directory");
                Toast.makeText(mContext, R.string.target_is_not_a_directory, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w("Browser", "Trying to navigate to non-existing directory");
            Toast.makeText(mContext, R.string.directory_not_exists, Toast.LENGTH_SHORT).show();
        }
    }
    
    public boolean back() {
        if (!this.mHistory.isEmpty()) {
            GenericFile f = FileFactory.newFile(mHistory.pop());
            while (!this.mHistory.isEmpty() && !f.exists()) {
                f = FileFactory.newFile(this.mHistory.pop());
            }
            if (f.exists() && f.isDirectory()) {
                this.navigate(f, false);
                return true;
            }
        }
        return false;
    }
    
    public void up() {
        if (this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.rootDirectory)) {
            return;
        }
        final GenericFile parent = resolveExistingParent(mCurrentPath);
        this.mHistory.push(parent.getAbsolutePath());
        this.navigate(parent, true);
    }
    
    public void invalidate() {
        if (this.mNavigateListener != null) {
            this.mNavigateListener.onNavigate(this.mCurrentPath);
        }
    }

    public boolean isRoot() {
        return this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.rootDirectory);
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

        SavedState(@NotNull ArrayDeque<String> history,
                   @Nullable final String currentPath,
                   @Nullable final String previousPath) {
            this.mHistory = history;
            this.mCurrentPath = currentPath;
            this.mPreviousPath = previousPath;
        }

        @SuppressWarnings("Unchecked")
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
            @NotNull
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

    private static final class InvalidateRunnable implements Runnable {
        private final WeakReference<Browser> browser;
        InvalidateRunnable(final Browser browser) {
            this.browser = new WeakReference<Browser>(browser);
        }

        @Override
        public void run() {
            final Browser browser1 = this.browser.get();
            if (browser1 != null) {
                browser1.invalidate();
            }
        }
    }

    private static final class NavigateRunnable implements Runnable {
        private final WeakReference<Browser> browser;
        private final GenericFile target;
        private final boolean addToHistory;

        NavigateRunnable(final Browser browser,
                         final GenericFile target,
                         final boolean addToHistory) {
            this.browser = new WeakReference<Browser>(browser);
            this.target = target;
            this.addToHistory = addToHistory;
        }

        @Override
        public void run() {
            final Browser browser1 = this.browser.get();
            if (browser1 != null) {
                browser1.navigate(this.target, this.addToHistory);
            }
        }
    }
}
