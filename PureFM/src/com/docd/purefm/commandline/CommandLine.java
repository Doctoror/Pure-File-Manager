package com.docd.purefm.commandline;

import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

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
            final String terminationLine = writeCommand(command, shell.obtainOutputStream());
            inputStreamToStringList(shell.obtainInputStream(), terminationLine, result);
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
            final String terminationLine = writeCommand(command, shell.obtainOutputStream());
            return cleanInputStream(shell.obtainInputStream(),terminationLine);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            shell.releaseOutputStream();
            shell.releaseInputStream();
        }
    }

    private static String writeCommand(final String command, final DataOutputStream outputStream) throws IOException {
        /*
         * Termination string should also contain symbols not allowed in filesystems to reduce
         * possibility of misinterpreting the stdout output with termination string using ls command
         */
        final String terminationLine = "/:" + new String(Hex.encodeHex(DigestUtils.md5(command)));
        outputStream.writeBytes(appendTerminationCommands(command, terminationLine));
        outputStream.flush();
        return terminationLine;
    }

    private static String appendTerminationCommands(final String command, final String terminationString) {
        return command + ";echo \"" + terminationString + "\";echo $?\n";
    }

    public static boolean execute(final Shell shell, Command command) {
        return execute(shell, command.toString());
    }

    private static boolean cleanInputStream(final BufferedReader reader, final String terminationLine) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.equals(terminationLine));
        final String errorCode = reader.readLine();
        try {
            return Integer.parseInt(errorCode) == 0;
        } catch (NumberFormatException e) {
            Log.w("inputStreamToStringList", "Expected error code, but received \'" + errorCode + "\'");
            return false;
        }
    }

    private static boolean inputStreamToStringList(final BufferedReader reader, final String terminationLine, final List<String> target) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.equals(terminationLine)) {
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
