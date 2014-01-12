package com.docd.purefm.commandline;

import java.io.File;

public final class Touch extends Command {
    
    public Touch(final File file) {
        super(ShellHolder.getNextCommandId(), "busybox touch "
                .concat(CommandLineUtils.getCommandLineString(file.getAbsolutePath())));
    }


}
