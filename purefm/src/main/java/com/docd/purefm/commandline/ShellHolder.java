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
package com.docd.purefm.commandline;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.execution.Shell;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * ShellHolder holds shared Shell instance
 */
public final class ShellHolder {

    public interface OnShellChangedListener {
        void onShellChanged(@Nullable Shell shell, boolean isRootShell);
    }

    private static final int REASK_FOR_ROOT_SHELL_THRESHOLD = 6;

    private static final Object sInstanceLock = new Object();
    private static ShellHolder sInstance;

    private static int sCommandId;

    public static int getNextCommandId() {
        return sCommandId++;
    }

    @NonNull
    public static ShellHolder getInstance() {
        if (sInstance == null) {
            synchronized (sInstanceLock) {
                if (sInstance == null) {
                    sInstance = new ShellHolder();
                }
            }
        }
        return sInstance;
    }

    private final Collection<WeakReference<OnShellChangedListener>> sListeners =
            new LinkedList<>();

    private final Object mShellLock = new Object();

    private final Handler mHandler;

    private boolean mIsRootShell;
    private Shell mShell;
    private int mSkipReaskForRootShellCount;

    private ShellHolder() {
        mHandler = new ShellHolderHandler(this);
    }

    public void addOnShellChangedListener(@NonNull final OnShellChangedListener listener) {
        final Set<WeakReference<OnShellChangedListener>> toRemove = new HashSet<>();
        try {
            for (final WeakReference<OnShellChangedListener> ref : sListeners) {
                final OnShellChangedListener l = ref.get();
                if (l == null) {
                    toRemove.add(ref);
                } else if (l == listener) {
                    //already contains
                    return;
                }
            }
            sListeners.add(new WeakReference<>(listener));
        } finally {
            sListeners.removeAll(toRemove);
        }
    }

    public void removeOnShellChangedListener(@NonNull final OnShellChangedListener listener) {
        final Set<WeakReference<OnShellChangedListener>> toRemove = new HashSet<>();
        try {
            for (final WeakReference<OnShellChangedListener> ref : sListeners) {
                final OnShellChangedListener l = ref.get();
                if (l == null || l == listener) {
                    toRemove.add(ref);
                }
            }
        } finally {
            sListeners.removeAll(toRemove);
        }
    }

    public boolean isCurrentShellRoot() {
        return mIsRootShell;
    }

    public void releaseShell(final boolean notifyListeners) {
        synchronized (mShellLock) {
            releaseShellAsync(notifyListeners);
        }
    }

    private void releaseShellAsync(final boolean notifyListeners) {
        if (mShell != null) {
            try {
                mShell.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mShell = null;
            mIsRootShell = false;
            if (notifyListeners) {
                mHandler.removeMessages(ShellHolderHandler.MESSAGE_NOTIFY_LISTENERS);
                mHandler.sendEmptyMessage(ShellHolderHandler.MESSAGE_NOTIFY_LISTENERS);
            }
        }
    }

    /**
     * The shell is set by BrowserPagerActivity and is released when BrowserPagerActivity
     * is destroyed.
     * Returns current global shell. If current shell is non-root, but root shell is requested,
     * it will ask for root shell every #REASK_FOR_ROOT_SHELL_THRESHOLD calls.
     *
     * @return shell shared Shell instance
     */
    @Nullable
    public Shell getShell() {
        synchronized (mShellLock) {
            final boolean suEnabled = Settings.getInstance().isSuEnabled();
            if (suEnabled && mShell != null && !mIsRootShell) {
                if (mSkipReaskForRootShellCount >= REASK_FOR_ROOT_SHELL_THRESHOLD) {
                    mSkipReaskForRootShellCount = 0;
                    final Pair<Boolean, Shell> result = ShellFactory.getRootShell();
                    applyResult(result);
                } else {
                    mSkipReaskForRootShellCount++;
                }
            }
            if (!suEnabled && mShell != null && mIsRootShell) {
                openNewShell();
            }
            if (mShell == null || !Shell.isAnyShellOpen()) {
                openNewShell();
            }
            return mShell;
        }
    }

    /**
     * Opens new shell. This method is called from {@link #getShell()} and is already synchronized
     * with {@link #mShellLock}
     */
    private void openNewShell() {
        try {
            final Pair<Boolean, Shell> result = ShellFactory.getShell();
            applyResult(result);
        } catch (IOException e) {
            Log.w("getShell() error:", e);
        }
    }

    /**
     * Applies shell result. This method is called from {@link #getShell()} and is already
     * synchronized with {@link #mShellLock}
     *
     * @param result {@link ShellFactory#getShell} result
     */
    private void applyResult(@Nullable final Pair<Boolean, Shell> result) {
        if (result != null) {
            releaseShellAsync(false);
            mIsRootShell = result.first;
            mShell = result.second;
            mSkipReaskForRootShellCount = 0;
            mHandler.removeMessages(ShellHolderHandler.MESSAGE_NOTIFY_LISTENERS);
            mHandler.sendEmptyMessage(ShellHolderHandler.MESSAGE_NOTIFY_LISTENERS);
        }
    }

    void notifyListeners() {
        synchronized (mShellLock) {
            for (final WeakReference<OnShellChangedListener> ref : sListeners) {
                final OnShellChangedListener l = ref.get();
                if (l != null) {
                    l.onShellChanged(mShell, mIsRootShell);
                }
            }
        }
    }

    private static final class ShellHolderHandler extends Handler {

        static final int MESSAGE_NOTIFY_LISTENERS = 13;

        private final WeakReference<ShellHolder> mShellHolderReference;

        ShellHolderHandler(@NonNull final ShellHolder shellHolder) {
            super(Looper.getMainLooper());
            this.mShellHolderReference = new WeakReference<>(shellHolder);
        }

        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == MESSAGE_NOTIFY_LISTENERS) {
                final ShellHolder holder = mShellHolderReference.get();
                if (holder != null) {
                    holder.notifyListeners();
                }
            } else {
                super.handleMessage(msg);
            }
        }
    }
}
