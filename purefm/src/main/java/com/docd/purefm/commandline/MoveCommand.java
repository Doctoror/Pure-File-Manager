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

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

/**
 * mv command
 *
 * @author Doctoror
 */
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
