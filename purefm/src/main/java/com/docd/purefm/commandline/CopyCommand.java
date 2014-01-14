package com.docd.purefm.commandline;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

public final class CopyCommand extends Command {

    public final String source;
    public final String target;
    
    public CopyCommand(final GenericFile source, final GenericFile target) {
        this(source.getAbsolutePath(), target.getAbsolutePath());
    }

    public CopyCommand(final String source, final String target) {
        super(ShellHolder.getNextCommandId(), getCommandString(source, target));
        this.source = source;
        this.target = target;
    }

    @NotNull
    private static String getCommandString(final String source, final String target) {
        final StringBuilder command = new StringBuilder("busybox cp -rfp ");
        command.append(CommandLineUtils.getCommandLineString(source));
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target));
        return command.toString();
    }
    
}
