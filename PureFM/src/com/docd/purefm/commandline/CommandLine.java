package com.docd.purefm.commandline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public final class CommandLine {
    
    private CommandLine() {}
    
    public static List<String> executeForResult(boolean superuser, String command) {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader is = null;
        boolean success;
        final List<String> result = new LinkedList<String>();
        try {
            process = Runtime.getRuntime().exec(superuser ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            os.writeBytes(command + "\n");
            os.flush();

            os.writeBytes("exit\n"); 
            os.flush();
            
            String line;
            try {
                while ((line = is.readLine()) != null) {
                    result.add(line);
                }
            } catch (EOFException e) {}
            success = process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (is != null) { try { is.close(); } catch (Exception e) {} }
            if (process != null) { try { process.destroy(); } catch (Exception e) {} }
        }
        return success ? result : null;
    }
    
    public static boolean execute(final List<Command> coms) {
        boolean needsu = false;
        for (Command c : coms) {
            if (c.useSu()) {
                needsu = true;
                break;
            }
        }
        return execute(needsu, coms);
    }
    
    public static List<String> executeForResult(final List<Command> coms) {
        boolean needsu = false;
        for (Command c : coms) {
            if (c.useSu()) {
                needsu = true;
                break;
            }
        }
        return executeForResult(needsu, coms);
    }
    
    public static List<String> executeForResult(boolean superuser, List<Command> coms) {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader is = null;
        boolean success;
        final List<String> result = new LinkedList<String>();
        try {
            process = Runtime.getRuntime().exec(superuser ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for (Command command : coms) {
                os.writeBytes(command.toString() + "\n");
            }
            os.flush();

            os.writeBytes("exit\n"); 
            os.flush();
            
            String line;
            try {
                while ((line = is.readLine()) != null) {
                    result.add(line);
                }
            } catch (EOFException e) {}
            success = process.waitFor() == 0;
        } catch (Exception e) {
            success = false;
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (is != null) { try { is.close(); } catch (Exception e) {} }
            if (process != null) { try { process.destroy(); } catch (Exception e) {} }
        }
        return success ? result : null;
    }
    
    public static boolean execute(boolean superuser, List<Command> commands) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(superuser ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (Command command : commands) {
                os.writeBytes(command.toString() + "\n");
            }
            os.flush();

            os.writeBytes("exit\n"); 
            os.flush();
            
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (process != null) { try { process.destroy(); } catch (Exception e) {} }
        }
        return false;
    }
    
    public static boolean execute(Command command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(command.useSu() ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command.toString() + "\n");
            os.flush();

            os.writeBytes("exit\n"); 
            os.flush();
            
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) { try { os.close(); } catch (Exception e) {} }
            if (process != null) { try { process.destroy(); } catch (Exception e) {} }
        }
        return false;
    }
}
