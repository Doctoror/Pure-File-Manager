package com.docd.purefm.commandline;

public class Command extends com.stericson.RootTools.execution.Command {

    public interface CommandListener {
        void commandOutput(int id, String line);
        void commandTerminated(int id, String reason);
        void commandCompleted(int id, int exitCode);
    }

    private CommandListener listener;

    public Command(int id, boolean handlerEnabled, String... command) {
        super(id, handlerEnabled, command);
    }

    public Command(int id, String... command) {
        super(id, command);
    }

    public Command(String... command) {
        super(ShellHolder.getNextCommandId(), command);
    }

    public final void setCommandListener(final CommandListener listener) {
        this.listener = listener;
    }

    @Override
    public void commandOutput(int i, String s) {
        if (this.listener != null) {
            this.listener.commandOutput(i, s);
        }
    }

    @Override
    public void commandTerminated(int i, String s) {
        if (this.listener != null) {
            this.listener.commandTerminated(i, s);
        }
    }

    @Override
    public void commandCompleted(int i, int i2) {
        if (this.listener != null) {
            this.listener.commandCompleted(i, i2);
        }
    }

    @Override
    public final String toString() {
        return this.getCommand();
    }
}
