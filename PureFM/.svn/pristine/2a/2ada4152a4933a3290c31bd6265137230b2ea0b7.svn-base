package com.docd.purefm.browser;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import android.content.Context;
import android.os.Environment;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.Cache;
import com.docd.purefm.utils.PureFMFileUtils;

public final class Browser {

    public interface OnNavigateListener {
        void onNavigate(GenericFile path);
        void onNavigationCompleted(GenericFile path);
    }

    private final File root;
    private final Deque<GenericFile> history;
    
    private GenericFile path;
    private GenericFile prevPath;
    private OnNavigateListener listener;

    protected Browser(Context context) {
        this.history = new ArrayDeque<GenericFile>(15);
        this.root = File.listRoots()[0];
        final String home = Settings.getHomeDirectory(context);
        final String state = Environment.getExternalStorageState();
        if (home != null) {
            this.path = PureFMFileUtils.newFile(home);
            if (!this.path.exists()) {
                this.path = null;
            }
        }
        if (path == null && (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            this.path = PureFMFileUtils.newFile(Environment.getExternalStorageDirectory());
        }
        if (this.path == null) {
            this.path = PureFMFileUtils.newFile(this.root.getAbsolutePath());
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
        this.path = PureFMFileUtils.newFile(path);
    }
    
    public void invalidate() {
        if (this.listener != null) {
            this.listener.onNavigate(this.path);
        }
    }

    protected boolean isRoot() {
        return this.path.toFile().equals(this.root);
    }

}
