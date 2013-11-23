package com.docd.purefm.commandline;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.Permissions;
import com.docd.purefm.settings.Settings;

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
    
    public static boolean applyPermissions(boolean su, Permissions p, CommandLineFile target) {
        final String permission = toOctalPermission(p);
        final StringBuilder command = new StringBuilder();        
        command.append("busybox chmod ");
        command.append(permission);
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        return CommandLine.execute(new Command(su, command.toString()));
    }
    
    public static boolean canAccessRecursively(CommandLineFile target) {
        final StringBuilder command = new StringBuilder();        
        command.append("busybox ls -AR ");
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        return CommandLine.execute(new Command(false, command.toString()));
    }
    
    public static boolean exists(File target) {
        final StringBuilder command = new StringBuilder();
        command.append("busybox test -e ");
        command.append(CommandLineUtils.getCommandLineString(target.getAbsolutePath()));
        command.append(" && echo exists");
        
        final List<String> res = CommandLine.executeForResult(false, command.toString());
        return res != null && !res.isEmpty() && res.get(0).equals("exists");
    }
    
    public static String touch(boolean su, CommandLineFile target) {
        
        final List<Command> coms = new LinkedList<Command>();
        final String path = target.getAbsolutePath();
        
        coms.add(new Touch(su, target.toFile()));
        
        final StringBuilder command = new StringBuilder();        
        
        command.append("busybox ls -lApedn ");
        command.append(CommandLineUtils.getCommandLineString(path));
        coms.add(new Command(su, command.toString()));
        
        final List<String> res = CommandLine.executeForResult(su, coms);
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static String mkdir(boolean su, CommandLineFile target) {
        
        final List<Command> coms = new LinkedList<Command>();
        final String path = target.getAbsolutePath();
        
        final StringBuilder command = new StringBuilder("busybox mkdir ");
        command.append(CommandLineUtils.getCommandLineString(path));
        
        coms.add(new Command(su, command.toString()));
        
        command.setLength(0);
        command.append("busybox ls -lApedn ");
        command.append(CommandLineUtils.getCommandLineString(path));
        coms.add(new Command(su, command.toString()));
        
        final List<String> res = CommandLine.executeForResult(su, coms);
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static String mkdirs(boolean su, CommandLineFile target) {
        
        final List<Command> coms = new LinkedList<Command>();
        final String path = target.getAbsolutePath();
        
        final StringBuilder command = new StringBuilder("busybox mkdir -p ");
        command.append(CommandLineUtils.getCommandLineString(path));
        
        coms.add(new Command(su, command.toString()));
        
        command.setLength(0);
        command.append("busybox ls -lnAped ");
        command.append(CommandLineUtils.getCommandLineString(path));
        coms.add(new Command(su, command.toString()));
        
        final List<String> res = CommandLine.executeForResult(su, coms);
        if (res == null) {
            return null;
        }
        return res.isEmpty() ? null : res.get(0);
    }
    
    public static List<String> ls(boolean su, final File dir) {
        final StringBuilder command = new StringBuilder("busybox ls -n");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(su, command.toString());
    }
    
    public static List<String> lsl(boolean su, final File dir) {
        final StringBuilder command = new StringBuilder("busybox ls -lnpe");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(su, command.toString());
    }
    
    public static List<String> lsld(boolean su, final File dir) {
        final StringBuilder command = new StringBuilder("busybox ls -lnped");
        if (Settings.showHidden) {
            command.append('A');
        }
        command.append(' ');
        command.append(CommandLineUtils.getCommandLineString(dir.getAbsolutePath()));
        if (dir.isDirectory() && command.charAt(command.length() - 1) != File.separatorChar) {
            command.append(File.separatorChar);
        }
        return CommandLine.executeForResult(su, command.toString());
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
    
    public static String getFSType(File file) {
        final StringBuilder command = new StringBuilder("busybox stat -f -c \"%T\" ");
        command.append(CommandLineUtils.getCommandLineString(file.getAbsolutePath()));
        final List<String> res = CommandLine.executeForResult(false, command.toString());
        return res == null || res.isEmpty() ? "" : res.get(0);
    }
}
