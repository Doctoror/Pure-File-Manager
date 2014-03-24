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
import android.util.Pair;

import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.Nullable;

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

    private static boolean sIsRootShell;
    private static Shell sShell;

    public static boolean isCurrentShellRoot() {
        return sIsRootShell;
    }

    public static synchronized void releaseShell() {
        if (sShell != null) {
            try {
                sShell.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sShell = null;
        }
    }

    /**
     * The shell is set by BrowserPagerActivity and is released when BrowserPagerActivity is destroyed
     *
     * @return shell shared Shell instance
     */
    @Nullable
    public static synchronized Shell getShell() {
        if (Settings.su && sShell != null && !sIsRootShell) {
            //resolveShell();
        }
        if (sShell == null || !Shell.isAnyShellOpen()) {
            resolveShell();
        }
        return sShell;
    }

    private static void resolveShell() {
        try {
            final Pair<Boolean, Shell> result = ShellFactory.getShell();
            if (result != null) {
                sIsRootShell = result.first;
                sShell = result.second;
            }
        } catch (IOException e) {
            Log.w("getShell() error:", e);
        }
    }
}
