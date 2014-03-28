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
package com.docd.purefm.tasks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.execution.Shell;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

final class SearchCommandLineTask extends AbstractSearchTask {

    private static final Pattern DENIED = Pattern.compile("^find:\\s(.+):\\sPermission denied$");

    private final Shell mShell;
    private final List<String> mDenied;

    public SearchCommandLineTask(@NotNull final Shell shell) {
        this.mShell = shell;
        this.mDenied = new ArrayList<>();
    }

    @Override
    @NotNull
    public List<String> getDeniedLocations() {
        return this.mDenied;
    }
    
    @Override
    protected Void doInBackground(String... params) {
        final String what = params[0];

        final StringBuilder command = new StringBuilder();
        final String[] commands = new String[params.length - 1];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = this.buildFindCommand(params[i + 1], what, command);
        }
        command.setLength(0);
        
        DataOutputStream os = null;
        BufferedReader is = null;
        BufferedReader err = null;
        Process process = null;
        try {
            //TODO use mShell instead
            process = Runtime.getRuntime().exec(Settings.su ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            for (final String commandItem : commands) {
                os.writeBytes(commandItem + "\n");
                os.flush();
            }
            os.writeBytes("exit\n"); 
            os.flush();
            
            String line;
            try {
                while (!isCancelled() && (line = is.readLine()) != null) {
                    this.publishProgress(CommandLineFile.fromLSL(mShell, null, line));
                }
            } catch (EOFException e) {
                //ignore
            }
            
            try {
                while (!isCancelled() && (line = err.readLine()) != null) {
                    final Matcher denied = DENIED.matcher(line);
                    if (denied.matches()) {
                        this.mDenied.add(denied.group(1));
                    } 
                }
            } catch (EOFException e) {
                //ignore
            }
            process.waitFor();
        } catch (Exception e) {
            Log.w("Exception while searching", e.toString());
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(err);
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) {
                    //ignored
                }
            }
        }
        return null;
    }
    
    private String buildFindCommand(String location, String what, StringBuilder command) {
        command.setLength(0);
        command.append("find ");
        command.append(CommandLineUtils.getCommandLineString(location));
        command.append(" -type f -iname ");
        command.append('*');
        command.append(CommandLineUtils.getCommandLineString(what));
        command.append('*');
        command.append(" -exec busybox ls -lApedn {} \\;");
        return command.toString();
    }

}
