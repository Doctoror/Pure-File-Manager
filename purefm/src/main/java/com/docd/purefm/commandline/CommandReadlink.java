package com.docd.purefm.commandline;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Readlink - print resolved symbolic links or canonical file names
 */
public final class CommandReadlink extends Command {

    public CommandReadlink(@NotNull final String path) {
        super("readlink -f " + path);
    }

    @Nullable
    public static String readlink(@NotNull final String path) {
        final List<String> result = CommandLine.executeForResult(ShellHolder.getShell(),
                new CommandReadlink(path));
        return result == null || result.isEmpty() ? null : result.get(0);
    }
}
