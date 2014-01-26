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

/**
 * cp command - copy files and directories
 *
 * -f, --force
 *            if  an  existing  destination file cannot be opened, remove it and try again (this option is
 *            ignored when the -n option is also used)
 *
 * -p same as --preserve=mode,ownership,timestamps
 * -r copy directories recursively
 *
 * @author Doctoror
 */
public final class CommandCopyRecursively extends BusyboxCommand {

    public final String source;
    public final String target;

    /**
     * Builds copy command
     *
     * @param source Source file
     * @param target Destination file
     */
    public CommandCopyRecursively(final GenericFile source, final GenericFile target) {
        this(source.getAbsolutePath(), target.getAbsolutePath());
    }

    /**
     * Builds copy command
     *
     * @param source Source file path
     * @param target Destination file path
     */
    public CommandCopyRecursively(final String source, final String target) {
        super("cp -rfp " + CommandLineUtils.getCommandLineString(source) +
                " " + CommandLineUtils.getCommandLineString(target));
        this.source = source;
        this.target = target;
    }

}
