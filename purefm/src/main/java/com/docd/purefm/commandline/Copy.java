package com.docd.purefm.commandline;

import com.docd.purefm.file.GenericFile;

public final class Copy extends Command {
    
    public Copy(final GenericFile source, final GenericFile target) {
        super(ShellHolder.getNextCommandId(), getCommandString(source, target));
    }

    private static String getCommandString(final GenericFile source, final GenericFile target) {
        final StringBuilder command = new StringBuilder("busybox cp -rf ");
        command.append(CommandLineUtils.getCommandLineString(source.getAbsolutePath()));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        return command.toString();
    }
    
}
