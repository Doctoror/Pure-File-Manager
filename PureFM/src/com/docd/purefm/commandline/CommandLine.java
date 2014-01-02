package com.docd.purefm.commandline;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class CommandLine {
    
    private CommandLine() {}
    
    public static List<String> executeForResult(final Shell shell, String command) {
        final List<String> result = new LinkedList<String>();
        try {
            final DataOutputStream outputStream = shell.obtainOutputStream();

            outputStream.writeBytes(command + Shell.OUTPUT_TERMINATION_COMMAND_POSTFIX);
            outputStream.flush();

            inputStreamToStringList(shell.obtainInputStream(), result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shell.releaseOutputStream();
            shell.releaseInputStream();
        }
        return null;
    }

    public static boolean execute(final Shell shell, String command) {
        try {
            final DataOutputStream outputStream = shell.obtainOutputStream();
            outputStream.writeBytes(command + Shell.OUTPUT_TERMINATION_COMMAND_POSTFIX);
            outputStream.flush();
            return cleanInputStream(shell.obtainInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            shell.releaseOutputStream();
            shell.releaseInputStream();
        }
    }

    public static boolean execute(final Shell shell, Command command) {
        return execute(shell, command.toString());
    }


    private static boolean cleanInputStream(final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.equals(Shell.OUTPUT_TERMINATION_STRING));
        final String errorCode = reader.readLine();
        try {
            return Integer.parseInt(errorCode) == 0;
        } catch (NumberFormatException e) {
            Log.w("inputStreamToStringList", "Expected error code, but received \'" + errorCode + "\'");
            return false;
        }
    }

    private static boolean inputStreamToStringList(final BufferedReader reader, final List<String> target) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.equals(Shell.OUTPUT_TERMINATION_STRING)) {
            target.add(line);
            System.out.println("Read: " + line);
        }
        final String errorCode = reader.readLine();
        try {
            return Integer.parseInt(errorCode) == 0;
        } catch (NumberFormatException e) {
            Log.w("inputStreamToStringList", "Expected error code, but received \'" + errorCode + "\'");
            return false;
        }
    }
}
