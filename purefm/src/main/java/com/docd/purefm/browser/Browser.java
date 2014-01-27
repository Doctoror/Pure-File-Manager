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

import org.jetbrains.annotations.NotNull;

/**
 * Browser manages current path and navigation
 * @author Doctoror
 */
public final class Browser implements MultiListenerFileObserver.OnEventListener {

    private static final int OBSERVER_EVENTS = FileObserver.CREATE |
            FileObserver.DELETE_SELF |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF;

    private static Handler handler;
    private final Context mContext;

    public interface OnNavigateListener {
        void onNavigate(GenericFile path);
        void onNavigationCompleted(GenericFile path);
    }

    private final ArrayDeque<GenericFile> mHistory;

    private FileObserverCache mObserverCache;
    private MultiListenerFileObserver mCurrentPathObserver;
    private GenericFile mCurrentPath;
    private GenericFile mPreviousPath;
    private OnNavigateListener mNavigateListener;

    private Runnable mLastRunnable;

    protected Browser(final BrowserActivity activity) {
        if (handler == null) {
            handler = new Handler(activity.getMainLooper());
        }
        mContext = activity;
        this.mObserverCache = FileObserverCache.getInstance();
        this.mHistory = new ArrayDeque<GenericFile>(15);
        final String home = Settings.getHomeDirectory(activity);
        final String state = Environment.getExternalStorageState();
        if (home != null) {
            this.mCurrentPath = FileFactory.newFile(home);
            if (!this.mCurrentPath.exists()) {
                this.mCurrentPath = null;
            }
        }
        if (mCurrentPath == null && (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            this.mCurrentPath = FileFactory.newFile(Environment.getExternalStorageDirectory());
        }
        if (this.mCurrentPath == null) {
            this.mCurrentPath = FileFactory.newFile(com.docd.purefm.Environment.rootDirectory.getAbsolutePath());
        }
        if (this.mCurrentPath != null) {
            this.mCurrentPathObserver = mObserverCache.getOrCreate(
                    this.mCurrentPath.getAbsolutePath(), OBSERVER_EVENTS);
            this.mCurrentPathObserver.addOnEventListener(this);
            this.mCurrentPathObserver.startWatching();
        }
    }

    public Parcelable saveInstanceState() {
        return new SavedState(mHistory, mCurrentPath, mPreviousPath);
    }

    public void restoreState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        if (savedState != null) {
            mHistory.clear();
            mHistory.addAll(savedState.mHistory);
            if (savedState.mCurrentPath != null &&
                    savedState.mCurrentPath.exists() &&
                    savedState.mCurrentPath.isDirectory()) {
                mCurrentPath = savedState.mCurrentPath;
            }
            if (savedState.mPreviousPath != null &&
                    savedState.mPreviousPath.exists() &&
                    savedState.mPreviousPath.isDirectory()) {
                mPreviousPath = savedState.mPreviousPath;
            }
            invalidate();
        }
    }

    protected void setOnNavigateListener(OnNavigateListener l) {
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
        while (!parent.exists() || !parent.isDirectory()) {
            mHistory.remove(parent);
            parent = parent.getParentFile();
        }
        return parent;
    }

    public void navigate(final GenericFile target, boolean addToHistory) {
        if (target.exists()) {
            if (target.isDirectory()) {
                if (!this.mCurrentPath.equals(target)) {
                    this.mPreviousPath = this.mCurrentPath;
                    this.mCurrentPath = target;
                    if (addToHistory) {
                        this.mHistory.push(this.mPreviousPath);
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
            GenericFile f = this.mHistory.pop();
            while (!this.mHistory.isEmpty() && !f.exists()) {
                f = this.mHistory.pop();
            }
            if (f != null && f.exists() && f.isDirectory()) {
                this.navigate(f, false);
                return true;
            }
        }
        return false;
    }
    
    protected void up() {
        if (this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.rootDirectory)) {
            return;
        }
        final GenericFile parent = resolveExistingParent(mCurrentPath);
        this.mHistory.push(parent);
        this.navigate(parent, true);
    }
    
    public void invalidate() {
        if (this.mNavigateListener != null) {
            this.mNavigateListener.onNavigate(this.mCurrentPath);
        }
    }

    protected boolean isRoot() {
        return this.mCurrentPath.toFile().equals(com.docd.purefm.Environment.rootDirectory);
    }

    @Override
    public void onEvent(int event, String pathString) {
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.MOVE_SELF:
            case FileObserver.DELETE_SELF:
                handler.removeCallbacks(mLastRunnable);
                final GenericFile parent = resolveExistingParent(mCurrentPath);
                handler.post(mLastRunnable = new NavigateRunnable(
                        Browser.this, parent, true));
                break;

            case FileObserver.CREATE:
            case FileObserver.MOVED_TO:
                handler.removeCallbacks(mLastRunnable);
                handler.post(mLastRunnable = new InvalidateRunnable(this));
                break;
        }
    }

    private static final class SavedState implements Parcelable {
        final ArrayDeque<GenericFile> mHistory;
        final GenericFile mCurrentPath;
        final GenericFile mPreviousPath;

        SavedState(ArrayDeque<GenericFile> mHistory, GenericFile mCurrentFile, GenericFile mPreviousFile) {
            this.mHistory = mHistory;
            this.mCurrentPath = mCurrentFile;
            this.mPreviousPath = mPreviousFile;
        }

        @SuppressWarnings("Unchecked")
        SavedState(final Parcel source) {
            this.mHistory = (ArrayDeque<GenericFile>) source.readSerializable();
            this.mCurrentPath = (GenericFile) source.readSerializable();
            this.mPreviousPath = (GenericFile) source.readSerializable();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.mHistory);
            dest.writeSerializable(this.mCurrentPath);
            dest.writeSerializable(this.mPreviousPath);
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
