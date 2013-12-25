package com.docd.purefm.browser;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;

import android.content.Context;
import android.os.Environment;

import android.os.FileObserver;
import android.os.Handler;

import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.Cache;

public final class Browser {

    private static Handler handler;

    public interface OnNavigateListener {
        void onNavigate(GenericFile path);
        void onNavigationCompleted(GenericFile path);
    }

    private final File root;
    private final Deque<GenericFile> history;

    private FileObserver observer;
    private GenericFile path;
    private GenericFile prevPath;
    private OnNavigateListener listener;

    protected Browser(Context context) {
        if (handler == null) {
            handler = new Handler(context.getMainLooper());
        }
        this.history = new ArrayDeque<GenericFile>(15);
        this.root = File.listRoots()[0];
        final String home = Settings.getHomeDirectory(context);
        final String state = Environment.getExternalStorageState();
        if (home != null) {
            this.path = FileFactory.newFile(home);
            if (!this.path.exists()) {
                this.path = null;
            }
        }
        if (path == null && (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            this.path = FileFactory.newFile(Environment.getExternalStorageDirectory());
        }
        if (this.path == null) {
            this.path = FileFactory.newFile(this.root.getAbsolutePath());
        }
    }

    protected void setOnNavigateListener(OnNavigateListener l) {
        this.listener = l;
    }

    public GenericFile getPath() {
        return this.path;
    }
    
    public void onScanFinished(GenericFile requested) {
        this.path = requested;
        if (this.listener != null) {
            this.listener.onNavigationCompleted(requested);
        }
        if (this.observer != null) {
            this.observer.stopWatching();
        }
        this.observer = new DirectoryObserver(requested.getAbsolutePath(),
                FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF |
                FileObserver.MOVED_TO | FileObserver.MOVED_FROM | FileObserver.MOVE_SELF);
        this.observer.startWatching();
    }
    
    public void onScanCancelled() {
        if (this.prevPath != null) {
            this.path = this.prevPath;
        }
    }

    public void navigate(final GenericFile target, boolean addToHistory) {
        if (!this.path.equals(target)) {
            this.prevPath = this.path;
            this.path = target;
            if (addToHistory) {
                this.history.push(this.prevPath);
            }
            this.invalidate();
        }
    }
    
    public boolean back() {
        if (!this.history.isEmpty()) {
            GenericFile f = this.history.pop();
            while (!this.history.isEmpty() && !f.exists()) {
                f = this.history.pop();
            }
            if (f != null && f.exists() && f.isDirectory()) {
                this.navigate(f, false);
                return true;
            }
        }
        return false;
    }
    
    protected void up() {
        if (this.path.toFile().equals(this.root)) {
            return;
        }
        final String parent = this.path.getParent();
        if (parent != null) {
            GenericFile p = Cache.get(parent);
            if (p == null) {
                p = this.path.getParentFile();
            }
            if (p != null) {
                this.history.push(p);
                this.navigate(p, true);
            }
        }
    }
    
    protected void setInitialPath(File path) {
        this.path = FileFactory.newFile(path);
    }
    
    public void invalidate() {
        if (this.listener != null) {
            this.listener.onNavigate(this.path);
        }
    }

    protected boolean isRoot() {
        return this.path.toFile().equals(this.root);
    }

    private final class DirectoryObserver extends FileObserver {

        private DirectoryObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String pathString) {
            switch (event & FileObserver.ALL_EVENTS) {
                case FileObserver.DELETE_SELF:
                    handler.removeCallbacksAndMessages(null);
                    handler.post(new NavigateRunnable(Browser.this, path.getParentFile(), true));
                    break;

                case FileObserver.MOVE_SELF:
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                case FileObserver.MOVED_TO:
                    handler.removeCallbacksAndMessages(null);
                    handler.post(new InvalidateRunnable(Browser.this));
                    break;
            }
        }
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
