package com.docd.purefm.commandline;

import java.io.File;

public final class Remove extends Command {
    
    public Remove(final File file) {
        super(ShellHolder.getNextCommandId(), toCommandString(file));
    }

    private static String toCommandString(final File file) {
        return "busybox rm -rf ".concat(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
    }
}
