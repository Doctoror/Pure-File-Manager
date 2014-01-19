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
 * Base Command. Extends Stericson Command and adds a CommandListener
 * support that can be set instead of custom class creation
 * @author Doctoror
 */
public class Command extends com.stericson.RootTools.execution.Command {

    public interface CommandListener {
        void commandOutput(int id, String line);
        void commandTerminated(int id, String reason);
        void commandCompleted(int id, int exitCode);
    }

    private CommandListener listener;

    public Command(int id, boolean handlerEnabled, String... command) {
        super(id, handlerEnabled, command);
    }

    public Command(int id, String... command) {
        super(id, command);
    }

    public Command(String... command) {
        super(ShellHolder.getNextCommandId(), command);
    }

    public final void setCommandListener(final CommandListener listener) {
        this.listener = listener;
    }

    @Override
    public void commandOutput(int i, String s) {
        if (this.listener != null) {
            this.listener.commandOutput(i, s);
        }
    }

    @Override
    public void commandTerminated(int i, String s) {
        if (this.listener != null) {
            this.listener.commandTerminated(i, s);
        }
    }

    @Override
    public void commandCompleted(int i, int i2) {
        if (this.listener != null) {
            this.listener.commandCompleted(i, i2);
        }
    }

    @Override
    public final String toString() {
        return this.getCommand();
    }
}
