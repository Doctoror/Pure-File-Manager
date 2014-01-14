package com.docd.purefm.commandline;

import com.docd.purefm.Environment;

public final class BusyboxListCommand extends Command {
    public BusyboxListCommand() {
        super(Environment.busybox + " --list");
    }
}
