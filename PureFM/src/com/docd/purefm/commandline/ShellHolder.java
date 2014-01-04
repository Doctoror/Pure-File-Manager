package com.docd.purefm.commandline;

import android.util.Log;

import java.io.IOException;

/**
 * ShellHolder holds shared Shell instance
 */
public final class ShellHolder {
    private ShellHolder() {}

    private static Shell shell;

    public static synchronized void setShell(final Shell shell) {
        ShellHolder.shell = shell;
    }

    /**
     * The shell is set by BrowserActivity and is released when BrowserActivity is destroyed
     *
     * @return shell shared Shell instance
     */
    public static synchronized Shell getShell() {
        if (shell == null) {
            try {
                shell = ShellFactory.getShell();
            } catch (IOException e) {
                Log.w("getShell()", e);
            }
        }
        return shell;
    }
}
