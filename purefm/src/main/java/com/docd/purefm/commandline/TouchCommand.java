package com.docd.purefm.commandline;

import java.io.File;

public final class TouchCommand extends Command {
    
    public TouchCommand(final File file) {
        super(ShellHolder.getNextCommandId(), "busybox touch "
                .concat(CommandLineUtils.getCommandLineString(file.getAbsolutePath())));
    }


}
