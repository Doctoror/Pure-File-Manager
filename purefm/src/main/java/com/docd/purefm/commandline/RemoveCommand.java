package com.docd.purefm.commandline;

import java.io.File;

public final class RemoveCommand extends Command {
    
    public RemoveCommand(final File file) {
        super(ShellHolder.getNextCommandId(), "busybox rm -rf ".concat(
                CommandLineUtils.getCommandLineString(file.getAbsolutePath())));
    }
}
