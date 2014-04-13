package com.docd.purefm.commandline;

import com.docd.purefm.Environment;

public class BusyboxCommand extends Command {

    public BusyboxCommand(final String command) {
        super(Environment.sBusybox + " " + command);
    }
}
