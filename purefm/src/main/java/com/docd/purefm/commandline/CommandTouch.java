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

/**
 * Update the access and modification times of each FILE to the current time.
 * A FILE argument that does not exist is created empty, unless -c or -h is supplied.
 * A  FILE  argument string of - is handled specially and causes touch to change the times of the file
 * associated with standard output.
 *
 * @author Doctoror
 */
public final class CommandTouch extends BusyboxCommand {

    /**
     * Builds new touch command
     *
     * @param path File to update
     */
    public CommandTouch(final String path) {
        super("touch "
                .concat(CommandLineUtils.getCommandLineString(path)));
    }
}
