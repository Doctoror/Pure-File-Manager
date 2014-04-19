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


import android.support.annotation.NonNull;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;

import java.io.File;

/**
 * @author Doctoror
 *
 * ls - list directory contents
 *
 * -l long listing format
 * -n list numeric user and group IDs
 * -p append / indicator to directories
 * -e list full date and time
 * -A inclide entries which start with . but exclude . and ..
 */
public final class CommandListContents extends BusyboxCommand {

    public CommandListContents(@NonNull final GenericFile dir,
                               @NonNull final Settings settings) {
        super(buildCommand(dir, settings));
    }

    private static String buildCommand(@NonNull final GenericFile file,
                                       @NonNull final Settings settings) {
        if (!file.isDirectory()) {
            throw new RuntimeException("You should pass a directory here");
        }
        final StringBuilder command = new StringBuilder(50);
        command.append("ls -lnpe");
        if (settings.showHidden()) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
        if (command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return command.toString();
    }
}
