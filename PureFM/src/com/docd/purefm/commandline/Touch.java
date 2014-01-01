package com.docd.purefm.commandline;

import java.io.File;

public final class Touch extends Command {
    
    public Touch(final File file) {
        final StringBuilder command = new StringBuilder("busybox touch ");
        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
        this.command = command.toString();
    }
}
