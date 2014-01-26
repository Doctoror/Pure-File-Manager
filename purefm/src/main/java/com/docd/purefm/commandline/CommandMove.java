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
 * mv command - move or rename files
 * -f do not prompt before overwriting
 *
 * @author Doctoror
 */
public final class CommandMove extends BusyboxCommand {

    public final String source;
    public final String target;

    /**
     * Builds move command
     *
     * @param source Source file
     * @param target Destination file
     */
    public CommandMove(GenericFile source, GenericFile target) {
        this(source.getAbsolutePath(), target.getAbsolutePath());
    }

    /**
     * Builds move command
     *
     * @param source Source file path
     * @param target Destination file path
     */
    public CommandMove(String source, String target) {
        super("mv -f " + CommandLineUtils.getCommandLineString(
                source) + " " + CommandLineUtils.getCommandLineString(target));
        this.source = source;
        this.target = target;
    }
}
