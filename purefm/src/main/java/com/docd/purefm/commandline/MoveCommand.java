package com.docd.purefm.commandline;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

public final class MoveCommand extends Command {

    public final String source;
    public final String target;

    public MoveCommand(GenericFile source, GenericFile target) {
        this(source.getAbsolutePath(), target.getAbsolutePath());
    }

    public MoveCommand(String source, String target) {
        super(ShellHolder.getNextCommandId(), toCommandString(source, target));
        this.source = source;
        this.target = target;
    }

    @NotNull
    private static String toCommandString(final String source, final String target) {
        final StringBuilder command = new StringBuilder("busybox mv -f ");
        command.append(CommandLineUtils.getCommandLineString(source));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target));
        return command.toString();
    }
}
