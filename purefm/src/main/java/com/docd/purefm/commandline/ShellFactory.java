package com.docd.purefm.commandline;

import com.docd.purefm.settings.Settings;

import java.io.IOException;

public final class ShellFactory {
    private ShellFactory() {}

    public static Shell getShell() throws IOException {
        try {
            return new Shell(Settings.su);
        } catch (Shell.NotInitializedException e) {
            // can be thrown if superuser denied
            try {
                return new Shell(false);
            } catch (Shell.NotInitializedException e1) {
                throw new RuntimeException("Shell not initialized", e);
            }
        }
    }
}
