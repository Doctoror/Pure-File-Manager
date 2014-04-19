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

import com.docd.purefm.Environment;
import com.docd.purefm.file.Permissions;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import android.support.annotation.NonNull;

/**
 * Contains various utils and methods of this class execute commands and deliver results.
 *
 * @author Doctoror
 */
public final class CommandLineUtils {

    private CommandLineUtils(){}

    private static final String UNIX_ESCAPE_EXPRESSION = "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)";

    /**
     * Adds escaping. Used for file paths.
     *
     * @param input Input command line param
     * @return input string with escaped characters
     */
    public static String getCommandLineString(String input) {
        return input.replaceAll(UNIX_ESCAPE_EXPRESSION, "\\\\$1");
    }

    /**
     * Returns octal-formatted permission
     *
     * @param p Permissions to generate octal format for
     * @return octal-formatted permission representation
     */
    @NonNull
    protected static String toOctalPermission(final Permissions p) {
        byte user = 00;
        byte group = 00;
        byte other = 00;
        
        if (p.ur) {
            user += 04;
        }
        if (p.uw) {
            user += 02;
        }
        if (p.ux) {
            user += 01;
        }
        
        if (p.gr) {
            group += 04;
        }
        if (p.gw) {
            group += 02;
        }
        if (p.gx) {
            group += 01;
        }
        
        if (p.or) {
            other += 04;
        }
        if (p.ow) {
            other += 02;
        }
        if (p.ox) {
            other += 01;
        }
        
        final StringBuilder perm = new StringBuilder(3);
        perm.append(user);
        perm.append(group);
        perm.append(other);
        
        return perm.toString();
    }

    public static boolean copyRecursively(@NonNull final Shell shell, @NonNull final CommandCopyRecursively command) {
        final boolean wasRemounted;
        if (command.target.startsWith(Environment.sAndroidRootDirectory.getAbsolutePath())) {
            RootTools.remount(command.target, "RW");
            wasRemounted = true;
        } else {
            wasRemounted = false;
        }
        final boolean result = CommandLine.execute(shell, command);
        if (wasRemounted) {
            RootTools.remount(command.target, "RO");
        }
        return result;
    }
    
//    public static String printPermission(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("busybox stat -c %A ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> mResources = CommandLine.executeForResult(su, command.toString());
//        return mResources == null || mResources.isEmpty() ? null : mResources.get(0);
//    }
//    
//    public static String printOwner(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("busybox stat -c %U ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> mResources = CommandLine.executeForResult(su, command.toString());
//        return mResources == null || mResources.isEmpty() ? null : mResources.get(0);
//    }
//    
//    public static String readlink(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("readlink ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> mResources = CommandLine.executeForResult(su, command.toString());
//        return mResources == null || mResources.isEmpty() ? null : mResources.get(0);
//    }
}
