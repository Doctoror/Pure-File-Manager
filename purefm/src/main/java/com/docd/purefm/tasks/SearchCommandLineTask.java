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

import android.os.AsyncTask;
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
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;

public class SearchCommandLineTask extends AsyncTask<String, GenericFile, Void> {

    private static final Pattern DENIED = Pattern.compile("^find:\\s(.+):\\sPermission denied$");
    
    private List<String> denied;
    private String query;
    
    public SearchCommandLineTask() {
        this.denied = new ArrayList<String>();
    }
    
    public List<String> getDeniedLocations() {
        return this.denied;
    }
    
    @Override
    protected Void doInBackground(String... params) {
        final String what = params[0];
        this.query = what;
        
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
            process = Runtime.getRuntime().exec(Settings.su ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            for (int i = 0; i < commands.length; i++) {
                os.writeBytes(commands[i] + "\n");
                os.flush();
            }
            os.writeBytes("exit\n"); 
            os.flush();
            
            String line;
            try {
                while (!isCancelled() && (line = is.readLine()) != null) {
                    this.publishProgress(CommandLineFile.fromLSL(null, line));
                }
            } catch (EOFException e) {
                //ignore
            }
            
            try {
                while (!isCancelled() && (line = err.readLine()) != null) {
                    final Matcher denied = DENIED.matcher(line);
                    if (denied.matches()) {
                        this.denied.add(denied.group(1));
                    } 
                }
            } catch (EOFException e) {
                //ignore
            }
            process.waitFor();
        } catch (Exception e) {
            Log.w("Exception while searching", e.toString());
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (is != null) { try { is.close(); } catch (Exception e) {} }
            if (err != null) { try { err.close(); } catch (Exception e) {} }
            if (process != null) { try { process.destroy(); } catch (Exception e) {} }
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
