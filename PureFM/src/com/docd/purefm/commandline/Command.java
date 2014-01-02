package com.docd.purefm.commandline;

public class Command {

    protected String command;

    protected Command() {
    }
    
    public Command(String command) {
        this.command = command;
    }
    
    @Override
    public final String toString() {
        return this.command;
    }
}
