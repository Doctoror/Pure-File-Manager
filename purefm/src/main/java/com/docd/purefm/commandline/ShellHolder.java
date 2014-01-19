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

import android.util.Log;

import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * ShellHolder holds shared Shell instance
 */
public final class ShellHolder {

    private static int commandId;

    public static int getNextCommandId() {
        return commandId++;
    }

    private ShellHolder() {}

    private static Shell shell;

    public static synchronized void releaseShell() {
        if (shell != null) {
            try {
                shell.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shell = null;
        }
    }

    /**
     * The shell is set by BrowserActivity and is released when BrowserActivity is destroyed
     *
     * @return shell shared Shell instance
     */
    @NotNull
    public static synchronized Shell getShell() {
        if (shell == null) {
            try {
                shell = ShellFactory.getShell();
            } catch (IOException e) {
                Log.w("getShell() error:", e);
            }
        }
        return shell;
    }
}
