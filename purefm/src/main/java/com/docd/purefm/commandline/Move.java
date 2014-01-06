package com.docd.purefm.commandline;

import com.docd.purefm.file.GenericFile;

import java.io.File;

public final class Move extends Command {

    public Move(GenericFile source, GenericFile target) {
        this(source, target.toFile());
    }

    public Move(GenericFile source, File target) {
        final StringBuilder command = new StringBuilder("busybox mv -f ");
        command.append(CommandLineUtils.getCommandLineString(source.getAbsolutePath()));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        this.command = command.toString();
    }
    
}
