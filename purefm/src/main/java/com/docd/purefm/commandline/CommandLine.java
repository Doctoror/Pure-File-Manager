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

import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Executes Shell commands and waits for termination
 *
 * @author Doctoror
 */
public final class CommandLine {
    
    private CommandLine() {}

    @Nullable
    public static synchronized List<String> executeForResult(final Shell shell, final Command command) {
        final List<String> result = new LinkedList<String>();
        final ExecutionStatus status = new ExecutionStatus();
        command.setCommandListener(new Command.CommandListener() {
            @Override
            public void commandOutput(int i, String s) {
                result.add(s);
            }

            @Override
            public void commandTerminated(int i, String s) {
                status.finished = true;
                command.notify();
            }

            @Override
            public void commandCompleted(int i, int i2) {
                status.finished = true;
                command.notify();
            }
        });

        try {
            shell.add(command);
            synchronized (command) {
                if (!status.finished) {
                    try {
                        command.wait();
                    } catch (InterruptedException e) {
                        command.terminate("Interrupted");
                    }
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized boolean execute(final Shell shell, final Command command) {
        final ExecutionStatus status = new ExecutionStatus();
        command.setCommandListener(new Command.CommandListener() {
            @Override
            public void commandOutput(int id, String line) {

            }

            @Override
            public void commandTerminated(int id, String reason) {
                status.finished = true;
                command.notify();
            }

            @Override
            public void commandCompleted(int id, int exitCode) {
                status.finished = true;
                status.exitCode = exitCode;
                command.notify();
            }
        });

        try {
            shell.add(command);
            synchronized (command) {
                if (!status.finished) {
                    try {
                        command.wait();
                    } catch (InterruptedException e) {
                        command.terminate("Interrupted");
                    }
                }
            }
            return status.exitCode == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean execute(final Shell shell, final String command) {
        return execute(shell, new Command(ShellHolder.getNextCommandId(), command));
    }

    private static final class ExecutionStatus {
        int exitCode;
        boolean finished;
    }

//    private static String writeCommand(final String command, final DataOutputStream outputStream) throws IOException {
//        /*
//         * Termination string should also contain symbols not allowed in filesystems to reduce
//         * possibility of misinterpreting the stdout output with termination string using ls command
//         */
//        final String terminationLine = "/:" + new String(Hex.encodeHex(DigestUtils.md5(command)));
//        outputStream.writeBytes(appendTerminationCommands(command, terminationLine));
//        outputStream.flush();
//        return terminationLine;
//    }
//
//    private static String appendTerminationCommands(final String command, final String terminationString) {
//        return command + ";echo \"" + terminationString + "\";echo $?\n";
//    }
//
//    private static boolean cleanInputStream(final BufferedReader reader, final String terminationLine) throws IOException {
//        String line;
//        while ((line = reader.readLine()) != null && !line.equals(terminationLine));
//        final String errorCode = reader.readLine();
//        try {
//            return Integer.parseInt(errorCode) == 0;
//        } catch (NumberFormatException e) {
//            Log.w("inputStreamToStringList", "Expected error code, but received \'" + errorCode + "\'");
//            return false;
//        }
//    }
//
//    private static boolean inputStreamToStringList(final BufferedReader reader, final String terminationLine, final List<String> target) throws IOException {
//        String line;
//        while ((line = reader.readLine()) != null && !line.equals(terminationLine)) {
//            target.add(line);
//        }
//        final String errorCode = reader.readLine();
//        try {
//            return Integer.parseInt(errorCode) == 0;
//        } catch (NumberFormatException e) {
//            Log.w("inputStreamToStringList", "Expected error code, but received \'" + errorCode + "\'");
//            return false;
//        }
//    }
}
