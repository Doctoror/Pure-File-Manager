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
 * cp command
 *
 * @author Doctoror
 */
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
