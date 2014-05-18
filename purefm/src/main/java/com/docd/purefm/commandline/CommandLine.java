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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Executes Shell commands and waits for termination
 *
 * @author Doctoror
 */
public final class CommandLine {

    private static final int EXIT_CODE_SUCCESS = 0;

    private CommandLine() {}

    @Nullable
    public static synchronized List<String> executeForResult(@NonNull final Command command) {
        final List<String> result = new LinkedList<>();
        final ExecutionStatus status = new ExecutionStatus();
        command.setCommandListener(new Command.CommandListener() {
            @Override
            public void commandOutput(int i, String s) {
                result.add(s);
            }

            @Override
            public void commandTerminated(int i, String s) {
                synchronized (status) {
                    status.finished = true;
                    status.notify();
                }
            }

            @Override
            public void commandCompleted(int i, int exitCode) {
                status.exitCode = exitCode;
                synchronized (status) {
                    status.finished = true;
                    status.notify();
                }
            }
        });

        if (ShellHolder.getInstance().execute(command)) {
            synchronized (status) {
                if (!status.finished) {
                    try {
                        status.wait();
                    } catch (InterruptedException e) {
                        command.terminate("Interrupted");
                    }
                }
            }
            if (status.exitCode == EXIT_CODE_SUCCESS) {
                return result;
            }
        }
        return null;
    }

    public static synchronized boolean execute(@NonNull final Command command) {
        final ExecutionStatus status = new ExecutionStatus();
        command.setCommandListener(new Command.CommandListener() {
            @Override
            public void commandOutput(int id, String line) {

            }

            @Override
            public void commandTerminated(int id, String reason) {
                synchronized (status) {
                    status.finished = true;
                    status.notify();
                }
            }

            @Override
            public void commandCompleted(int id, int exitCode) {
                status.exitCode = exitCode;
                synchronized (status) {
                    status.finished = true;
                    status.notify();
                }
            }
        });

        if (ShellHolder.getInstance().execute(command)) {
            synchronized (status) {
                if (!status.finished) {
                    try {
                        status.wait();
                    } catch (InterruptedException e) {
                        command.terminate("Interrupted");
                    }
                }
            }
            return status.exitCode == EXIT_CODE_SUCCESS;
        }
        return false;
    }

    public static boolean execute(@NonNull final String command) {
        return ShellHolder.getInstance().execute(new Command(ShellHolder.getNextCommandId(), command));
    }

    public static final class ExecutionStatus {
        public int exitCode = -1;
        public boolean finished;
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
