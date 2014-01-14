package com.docd.purefm.commandline;

import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public final class ShellFactory {
    private ShellFactory() {}

    @NotNull
    public static Shell getShell() throws IOException {
        RootTools.handlerEnabled = false;
        try {
            return RootTools.getShell(Settings.su);
        } catch (RootDeniedException e) {
            try {
                return RootTools.getShell(false);
            } catch (RootDeniedException e1) {
                throw new RuntimeException("Access denied for non-superuser shell?", e);
            } catch (TimeoutException e1) {
                throw new RuntimeException("Shell not initialized", e);
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Shell not initialized", e);
        }
    }
}
