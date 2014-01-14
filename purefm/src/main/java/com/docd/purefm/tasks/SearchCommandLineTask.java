package com.docd.purefm.tasks;

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

public class SearchCommandLineTask extends CancelableTask<String, GenericFile, Void> {

    private static final Pattern DENIED = Pattern.compile("^find:\\s(.+):\\sPermission denied$");
    
    private boolean su;
    
    private List<String> denied;
    private String query;
    
    public SearchCommandLineTask() {
        this.denied = new ArrayList<String>();
    }
    
    public SearchCommandLineTask(boolean su) {
        this();
        this.su = su;
    }
    
    public List<String> getDeniedLocations() {
        return this.denied;
    }
    
    public String getSearchedQuery() {
        return this.query;
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
        try {
            final Process process = Runtime.getRuntime().exec(this.su ? "su" : "sh");
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
                while ((line = is.readLine()) != null) {
                    this.publishProgress(CommandLineFile.fromLSL(null, line));
                }
            } catch (EOFException e) {}
            
            try {
                while ((line = err.readLine()) != null) {
                    final Matcher denied = DENIED.matcher(line);
                    if (denied.matches()) {
                        this.denied.add(denied.group(1));
                    } 
                }
            } catch (EOFException e) {}
            process.waitFor();
        } catch (Exception e) {
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (is != null) { try { is.close(); } catch (Exception e) {} }
            if (err != null) { try { err.close(); } catch (Exception e) {} }
        }
        return null;
    }
    
    private String buildFindCommand(String location, String what, StringBuilder command) {
        command.setLength(0);
        command.append("find ");
        command.append(CommandLineUtils.getCommandLineString(location));
        command.append(" -type f -name ");
        command.append('*');
        command.append(CommandLineUtils.getCommandLineString(what));
        command.append('*');
        command.append(" -exec busybox ls -lApedn {} \\;");
        return command.toString();
    }

}
