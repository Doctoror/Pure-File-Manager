package com.docd.purefm.commandline;

import com.docd.purefm.settings.Settings;

import java.io.IOException;

public final class ShellFactory {
    private ShellFactory() {}

    private static Shell shell;

    public static Shell getShell() throws IOException {
        if (shell != null) {
            if (Settings.su == shell.su) {
                return shell;
            }
            shell.close();
        }
        return new Shell(Settings.su);
    }
}
