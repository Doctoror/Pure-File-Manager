package com.docd.purefm.commandline;

public class Command {

    protected String command;
    private boolean su;
    
    protected Command(boolean su) {
        this.su = su;
    }
    
    public Command(boolean su, String command) {
        this(su);
        this.command = command;
    }
    
    public final boolean useSu() {
        return this.su;
    }
    
    @Override
    public final String toString() {
        return this.command;
    }
}
