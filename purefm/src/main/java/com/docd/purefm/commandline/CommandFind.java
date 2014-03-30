/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.commandline;

import org.jetbrains.annotations.NotNull;

/**
 * find - search for files in a directory hierarchy
 *
 * -name pattern
 *            Base of file name (the path with the leading  directories  removed)  matches
 *            shell  pattern pattern
 * -iname pattern
 *            Like -name, but the match is case insensitive
 * -type c
 *            File is of type c:
 *            b      block (buffered) special
 *            c      character (unbuffered) special
 *            d      directory
 *            p      named pipe (FIFO)
 *            f      regular file
 *
 * -exec command ;
 *            Execute  command;  true if 0 status is returned.  All following arguments to
 *            find are taken to be arguments to the command until an  argument  consisting
 *            of `;' is encountered.  The string `{}' is replaced by the current file name
 *            being processed everywhere it occurs in the arguments to  the  command,  not
 *            just  in  arguments where it is alone, as in some versions of find.  Both of
 *            these constructions might need to be escaped (with a `\') or quoted to  pro‐
 *            tect  them  from expansion by the shell.  See the EXAMPLES section for exam‐
 *            ples of the use of the -exec option.  The specified command is run once  for
 *            each  matched  file.   The  command  is  executed in the starting directory.
 *            There are unavoidable security problems surrounding use of the -exec action;
 *            you should use the -execdir option instead.
 *
 */
public final class CommandFind extends BusyboxCommand {

    /**
     * Builds find command
     *
     * @param startDirectory Directory to search in
     * @param what Names of files to find
     */
    public CommandFind(@NotNull final String startDirectory,
                       @NotNull final String... what) {
        super(buildCommand(startDirectory, what));

    }

    private static String buildCommand(@NotNull final String startDirectory, @NotNull final String[] what) {
        final StringBuilder command = new StringBuilder();
        command.append("find ");
        command.append(CommandLineUtils.getCommandLineString(startDirectory));
        for (int i = 0; i < what.length; i++) {
            command.append(" -type f -iname ");
            command.append('*');
            command.append(CommandLineUtils.getCommandLineString(what[i]));
            command.append('*');
            command.append(" -exec busybox ls -lApedn {} \\;");
            if (i != what.length - 1) {
                command.append(" -o");
            }
        }
        return command.toString();
    }
}
