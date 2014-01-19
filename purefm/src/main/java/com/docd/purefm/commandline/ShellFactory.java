/*
 * Licensed to Yaroslav Mytkalyk under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.docd.purefm.commandline;

import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public final class ShellFactory {
    private ShellFactory() {}

    @NotNull
    public static Shell getShell() throws IOException {
        RootTools.handlerEnabled = false;
        try {
            return RootTools.getShell(Settings.su);
        } catch (RootDeniedException e) {
            try {
                return RootTools.getShell(false);
            } catch (RootDeniedException e1) {
                throw new RuntimeException("Access denied for non-superuser shell?", e);
            } catch (TimeoutException e1) {
                throw new RuntimeException("Shell not initialized", e);
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Shell not initialized", e);
        }
    }
}
