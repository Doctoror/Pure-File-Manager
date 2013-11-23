package com.docd.purefm.commandline;

import java.io.File;


public final class Move extends Command {

    
    public Move(boolean su, File source, File target) {
        super(su);
        final StringBuilder command = new StringBuilder("busybox mv -f ");
        command.append(CommandLineUtils.getCommandLineString(source.getAbsolutePath()));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        this.command = command.toString();
    }
    
}
