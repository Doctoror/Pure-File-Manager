package com.docd.purefm.commandline;

import java.io.File;

public final class Delete extends Command {
    
    public Delete(boolean su, File target) {
        super(su);
        final StringBuilder command = new StringBuilder("busybox rm -r ");
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        this.command = command.toString();
    }

}
