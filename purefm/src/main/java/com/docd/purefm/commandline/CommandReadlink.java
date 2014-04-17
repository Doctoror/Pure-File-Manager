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

import com.stericson.RootTools.execution.Shell;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Readlink - print resolved symbolic links or canonical file names
 *
 *-f, --canonicalize
 *            canonicalize by following every symlink in every component of the given name
 *            recursively; all but the last component must exist
 *
 */
public final class CommandReadlink extends Command {

    public CommandReadlink(@NonNull final String path) {
        super("readlink -f " + path);
    }

    /**
     * Excutes readlink command and returns result
     *
     * @param shell Current Shell to execute with
     * @param path Path of file to readlink
     * @return canonical path of input file returned by readlink
     */
    @Nullable
    public static String readlink(@NonNull final Shell shell, @NonNull final String path) {
        final List<String> result = CommandLine.executeForResult(shell,
                new CommandReadlink(CommandLineUtils.getCommandLineString(path)));
        return result == null || result.isEmpty() ? null : result.get(0);
    }
}
