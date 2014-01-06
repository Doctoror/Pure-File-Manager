package com.docd.purefm.commandline;

import java.io.File;

public final class Remove extends Command {
    
    public Remove(final File file) {
        final StringBuilder command = new StringBuilder("busybox rm -rf ");
        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
        this.command = command.toString();
    }
    
}
