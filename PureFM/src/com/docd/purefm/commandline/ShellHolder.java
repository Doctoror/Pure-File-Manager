package com.docd.purefm.commandline;

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
        return shell;
    }
}
