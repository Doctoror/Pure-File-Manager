package com.docd.purefm.commandline;

import com.docd.purefm.file.GenericFile;

public final class Copy extends Command {

    
    public Copy(final GenericFile source, final GenericFile target) {
        final StringBuilder command = new StringBuilder("busybox cp -rf ");
        command.append(CommandLineUtils.getCommandLineString(source.getAbsolutePath()));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        this.command = command.toString();
    }
    
}
