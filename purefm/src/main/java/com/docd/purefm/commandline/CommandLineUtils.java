package com.docd.purefm.commandline;

import java.io.File;
import java.util.List;

import com.docd.purefm.Environment;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.execution.Shell;

public final class CommandLineUtils {

    private CommandLineUtils(){}
    
    public static String getCommandLineString(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\'", "\\\'")
                .replace("`", "\\`")
                .replace(" ", "\\ ");
    }
    
    protected static String toOctalPermission(Permissions p) {
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
        
        final StringBuilder perm = new StringBuilder();
        perm.append(user);
        perm.append(group);
        perm.append(other);
        
        return perm.toString();
    }
    
    public static boolean applyPermissions(final Shell shell, Permissions p, CommandLineFile target) {
        final String permission = toOctalPermission(p);
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" chmod ");
        command.append(permission);
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        return CommandLine.execute(shell, new Command(command.toString()));
    }
    
    public static boolean exists(final Shell shell, File target) {
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" test -e ");
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        command.append(" && echo exists");
        
        final List<String> res = CommandLine.executeForResult(shell, new Command(command.toString()));
        return res != null && !res.isEmpty() && res.get(0).equals("exists");
    }
    
    public static String touch(final Shell shell, final CommandLineFile target) {
        final String path = target.getAbsolutePath();
        if (!CommandLine.execute(shell, new Touch(target.toFile()))) {
            return null;
        }
        
        final StringBuilder command = new StringBuilder(50);        
        command.append(Environment.busybox);
        command.append(" ls -lApedn ");
        command.append(CommandLineUtils.getCommandLineString(path));

        final List<String> res = CommandLine.executeForResult(shell, new Command(command.toString()));
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static String mkdir(final Shell shell, CommandLineFile target) {
        
        final String path = target.getAbsolutePath();
        
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" mkdir ");
        command.append(CommandLineUtils.getCommandLineString(path));

        if (!CommandLine.execute(shell, command.toString())) {
            return null;
        }
        command.setLength(0);
        command.setLength(50);
        command.append(Environment.busybox);
        command.append(" ls -lApedn ");
        command.append(CommandLineUtils.getCommandLineString(path));
        
        final List<String> res = CommandLine.executeForResult(shell, new Command(command.toString()));
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static String mkdirs(final Shell shell, CommandLineFile target) {
        final String path = target.getAbsolutePath();
        
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" mkdir -p ");
        command.append(CommandLineUtils.getCommandLineString(path));

        if (!CommandLine.execute(shell, command.toString())) {
            return null;
        }
        command.setLength(0);
        command.setLength(50);
        command.append(Environment.busybox);
        command.append(" ls -lnAped ");
        command.append(CommandLineUtils.getCommandLineString(path));

        final List<String> res = CommandLine.executeForResult(shell, new Command(command.toString()));
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static List<String> ls(final Shell shell, final File dir) {
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" ls -n");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(shell, new Command(command.toString()));
    }
    
    public static List<String> lsl(final Shell shell, final File dir) {
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" ls -lnpe");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(shell, new Command(command.toString()));
    }
    
    public static List<String> lsld(final Shell shell, final File dir) {
        final StringBuilder command = new StringBuilder(50);
        command.append(Environment.busybox);
        command.append(" ls -lnped");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(shell, new Command(command.toString()));
    }

    public static boolean isMSDOSFS(final File file) {
        final String res = CommandLineUtils.getFSType(ShellHolder.getShell(), file);
        return res.equals("msdos") || res.equals("vfat");
    }
    
//    public static String printPermission(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("busybox stat -c %A ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> res = CommandLine.executeForResult(su, command.toString());
//        return res == null || res.isEmpty() ? null : res.get(0);
//    }
//    
//    public static String printOwner(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("busybox stat -c %U ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> res = CommandLine.executeForResult(su, command.toString());
//        return res == null || res.isEmpty() ? null : res.get(0);
//    }
//    
//    public static String readlink(final boolean su, final File file) {
//        final StringBuilder command = new StringBuilder("readlink ");
//        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
//        final List<String> res = CommandLine.executeForResult(su, command.toString());
//        return res == null || res.isEmpty() ? null : res.get(0);
//    }
    
    public static String getFSType(final Shell shell, File file) {
        final StringBuilder command = new StringBuilder(80);
        command.append(Environment.busybox);
        command.append(" stat -f -c \"%T\" ");
        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
        final List<String> res = CommandLine.executeForResult(shell, new Command(command.toString()));
        return res == null || res.isEmpty() ? "" : res.get(0);
    }
}
