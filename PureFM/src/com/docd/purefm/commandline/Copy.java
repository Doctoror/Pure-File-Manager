package com.docd.purefm.commandline;

import java.io.File;


public final class Copy extends Command {

    
    public Copy(boolean su, File source, File target) {
        super(su);
        final StringBuilder command = new StringBuilder("busybox cp -rf ");
        command.append(CommandLineUtils.getCommandLineString(source.getAbsolutePath()));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        this.command = command.toString();
    }
    
}
