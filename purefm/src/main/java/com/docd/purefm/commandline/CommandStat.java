package com.docd.purefm.commandline;

public final class CommandStat extends BusyboxCommand {

    public CommandStat(final String path) {
        super("stat -f -c \"%T\" " + CommandLineUtils.getCommandLineString(path));
    }
}
