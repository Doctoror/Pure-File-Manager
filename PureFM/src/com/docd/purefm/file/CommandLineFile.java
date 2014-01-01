package com.docd.purefm.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.commandline.Constants;
import com.docd.purefm.commandline.Copy;
import com.docd.purefm.commandline.Move;
import com.docd.purefm.commandline.Remove;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.utils.Cache;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.TextUtil;

public final class CommandLineFile implements GenericFile,
        Comparable<GenericFile> {

    private static final long serialVersionUID = -8173533665283968040L;

    private static final String FS_ROOT_PERMISSIONS = "drwxr-xr-x";

    private static final int LS_PERMISSIONS = 0;
    // private static final int LS_NUMLINKS = 1;
    private static final int LS_USER = 2;
    private static final int LS_GROUP = 3;
    private static final int LS_FILE_SIZE = 4;
    // private static final int LS_DAY_OF_WEEK = 5;
    private static final int LS_MONTH = 6;
    private static final int LS_DAY_OF_MONTH = 7;
    private static final int LS_TIME = 8;
    private static final int LS_YEAR = 9;
    private static final int LS_FILE = 10;

    private final File file;
    private Permissions p;
    private long length;
    private long lastmod;
    private boolean exists;

    private int owner;
    private int group;
    private int icon;
    
    private String mimeType;
    private String readableLength;
    private String readableLastMod;
    
    private boolean isSymlink;
    private boolean isDirectory;
    private boolean isMSDOS;

    private CommandLineFile(File file) {
        this.file = file;
    }

    private CommandLineFile(String path) {
        this.file = new File(path);
    }

    private CommandLineFile(File parent, String line) {
        this.file = new File(parent, line);
    }

    public void setFileSize(long size) {
        this.length = size;
    }

    @Override
    public boolean isSymlink() {
        return this.isSymlink;
    }

    public static CommandLineFile fromFile(File file) {
        if (file.equals(File.listRoots()[0])) {
            final CommandLineFile f = new CommandLineFile(file);
            f.owner = 0;
            f.group = 0;
            f.p = new Permissions(FS_ROOT_PERMISSIONS);
            f.isDirectory = true;
            f.isSymlink = false;
            f.exists = true;
            return f;
        }

        final List<String> res = CommandLineUtils.lsld(ShellHolder.getShell(), file);

        if (res == null || res.isEmpty()) {
            // file not yet exists
            return new CommandLineFile(file);
        }

        return fromLSL(null, res.get(0));
    }

    public static CommandLineFile fromLSL(File parent, String line) {

        if (line.isEmpty()) {
            throw new IllegalArgumentException("Bad ls -lApe output");
        }

        final String[] attr = getAttrs(line);
        for (int i = 0; i < attr.length; i++) {
            if (attr[i] == null) {
                throw new IllegalArgumentException("Bad ls -lApe output");
            }
        }

        String name = attr[LS_FILE];
        String target = null;
        final int index = name.indexOf("->");
        if (index != -1) {
            target = name.substring(index + 3).trim();
            name = name.substring(0, index).trim();
        }
        final CommandLineFile f = parent == null ? new CommandLineFile(name)
                : new CommandLineFile(parent, name);
        final String perm = attr[LS_PERMISSIONS];
        f.isSymlink = perm.charAt(0) == 'l';
        if (target == null) {
            f.isDirectory = perm.charAt(0) == 'd';
        } else {
            f.isDirectory = target.endsWith("/");
        }

        f.p = new Permissions(perm);
        f.owner = Integer.parseInt(attr[LS_USER]);
        f.group = Integer.parseInt(attr[LS_GROUP]);
        final String len = attr[LS_FILE_SIZE];
        if (len != null && !len.isEmpty()) {
            f.length = Long.parseLong(len);
            f.readableLength = FileUtils.byteCountToDisplaySize(f.length);
        }

        final Calendar c = Calendar.getInstance(Locale.US);
        c.set(Calendar.YEAR, Integer.parseInt(attr[LS_YEAR]));
        c.set(Calendar.MONTH, TextUtil.stringMonthToInt(attr[LS_MONTH]));
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(attr[LS_DAY_OF_MONTH]));

        final int index1 = attr[LS_TIME].indexOf(':');
        final int index2 = attr[LS_TIME].lastIndexOf(':');
        if (index1 != -1 && index2 != -1) {
            c.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(attr[LS_TIME].substring(0, index1)));
            c.set(Calendar.MINUTE,
                    Integer.parseInt(attr[LS_TIME].substring(index1 + 1, index2)));
            c.set(Calendar.SECOND,
                    Integer.parseInt(attr[LS_TIME].substring(index2 + 1)));
        }

        f.lastmod = c.getTimeInMillis();
        f.readableLastMod = TextUtil.readableDate(f.lastmod);
        f.exists = true;
        if (!f.isDirectory) {
            f.mimeType = MimeTypes.getMimeType(f.file);
            f.icon = MimeTypes.getTypeIcon(f.file);
        }
        
        //final String res = CommandLineUtils.getFSType(ShellHolder.getShell(), f.file);
        //f.isMSDOS = res.equals("msdos") || res.equals("vfat");
        Cache.addTo(f);
        return f;
    }

    private static String[] getAttrs(String string) {
        if (string.length() < 44) {
            throw new IllegalArgumentException("Bad ls -lApe output: " + string);
        }
        final char[] chars = string.toCharArray();

        final String[] results = new String[11];
        int ind = 0;
        final StringBuilder current = new StringBuilder();

        Loop: for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
            case ' ':
            case '\t':
                if (current.length() != 0) {
                    results[ind] = current.toString();
                    ind++;
                    current.setLength(0);
                    if (ind == 10) {
                        results[ind] = string.substring(i).trim();
                        break Loop;
                    }
                }
                break;

            default:
                current.append(chars[i]);
                break;
            }
        }

        return results;
    }

    private boolean init(String line) {
        final String[] attr = getAttrs(line);
        for (int i = 0; i < attr.length; i++) {
            if (attr[i] == null) {
                return false;
            }
        }

        final String perm = attr[LS_PERMISSIONS];
        this.isSymlink = perm.charAt(0) == 'l';
        this.isDirectory = perm.charAt(0) == 'd';

        this.p = new Permissions(perm);
        this.owner = Integer.parseInt(attr[LS_USER]);
        this.group = Integer.parseInt(attr[LS_GROUP]);
        final String len = attr[LS_FILE_SIZE];
        if (len != null && !len.isEmpty()) {
            this.length = Long.parseLong(len);
            this.readableLength = FileUtils.byteCountToDisplaySize(this.length);
        }

        final Calendar c = Calendar.getInstance(Locale.US);
        c.set(Calendar.YEAR, Integer.parseInt(attr[LS_YEAR]));
        c.set(Calendar.MONTH, TextUtil.stringMonthToInt(attr[LS_MONTH]));
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(attr[LS_DAY_OF_MONTH]));

        final int index1 = attr[LS_TIME].indexOf(':');
        final int index2 = attr[LS_TIME].lastIndexOf(':');
        if (index1 != -1 && index2 != -1) {
            c.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(attr[LS_TIME].substring(0, index1)));
            c.set(Calendar.MINUTE,
                    Integer.parseInt(attr[LS_TIME].substring(index1 + 1, index2)));
            c.set(Calendar.SECOND,
                    Integer.parseInt(attr[LS_TIME].substring(index2 + 1)));
        }

        this.lastmod = c.getTimeInMillis();
        this.readableLastMod = TextUtil.readableDate(this.lastmod);
        this.exists = true;
        if (!this.isDirectory) {
            this.mimeType = MimeTypes.getMimeType(this.file);
            this.icon = MimeTypes.getTypeIcon(this.file);
        }
        //final String res = CommandLineUtils.getFSType(ShellHolder.getShell(), this.file);
        //this.isMSDOS = res.equals("msdos") || res.equals("vfat");
        Cache.addTo(this);
        return true;
    }

    @Override
    public String getMimeType() {
        return this.mimeType;
    }
    
    @Override
    public int getTypeIcon() {
        return this.icon;
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public File toFile() {
        return this.file;
    }

    @Override
    public boolean delete() {
        if (CommandLine.execute(ShellHolder.getShell(), new Remove(this.file))) {
            this.exists = false;
            this.isDirectory = false;
            this.isSymlink = false;
            this.owner = 0;
            this.group = 0;
            this.p = null;
            return true;
        }
        return false;
    }

    @Override
    public CommandLineFile[] listFiles() {
        final List<String> result = CommandLineUtils.lsl(ShellHolder.getShell(), this.file);
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                res.add(CommandLineFile.fromLSL(this.file, f));
            } catch (IllegalArgumentException e) {
                //e.printStackTrace();
                // not a valid ls -l file line
            }
        }
        final CommandLineFile[] ret = new CommandLineFile[res.size()];
        res.toArray(ret);
        return ret;
    }

    @Override
    public CommandLineFile[] listFiles(final FileFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLineUtils.lsl(ShellHolder.getShell(), this.file);
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(this.file,
                        f);
                if (filter.accept(tmp.toFile())) {
                    res.add(tmp);
                }
            } catch (IllegalArgumentException e) {
                // not a valid ls -l file line
            }
        }
        final CommandLineFile[] ret = new CommandLineFile[res.size()];
        res.toArray(ret);
        return ret;
    }

    @Override
    public CommandLineFile[] listFiles(final FilenameFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLineUtils.lsl(ShellHolder.getShell(), this.file);
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(this.file,
                        f);
                if (filter.accept(this.file, tmp.getName())) {
                    res.add(tmp);
                }
            } catch (IllegalArgumentException e) {
                // not a valid ls -l file line
            }
        }
        final CommandLineFile[] ret = new CommandLineFile[res.size()];
        res.toArray(ret);
        return ret;
    }
    
    @Override
    public CommandLineFile[] listFiles(final GenericFileFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLineUtils.lsl(ShellHolder.getShell(), this.file);
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(this.file,
                        f);
                if (filter.accept(tmp)) {
                    res.add(tmp);
                }
            } catch (IllegalArgumentException e) {
                // not a valid ls -l file line
            }
        }
        final CommandLineFile[] ret = new CommandLineFile[res.size()];
        res.toArray(ret);
        return ret;
    }

    @Override
    public String[] list() {
        final List<String> res = CommandLineUtils.lsl(ShellHolder.getShell(), this.file);
        if (res != null) {
            final String[] resul = new String[res.size()];
            res.toArray(resul);
            return resul;
        }
        return null;
    }

    @Override
    public long length() {
        return this.length;
    }
    
    @Override
    public String readableLength() {
        return this.readableLength;
    }

    @Override
    public long lastModified() {
        return this.lastmod;
    }
    
    @Override
    public String readableLastModified() {
        return this.readableLastMod;
    }

    @Override
    public boolean createNewFile() {
        if (this.exists) {
            return false;
        }

        final String res = CommandLineUtils.touch(ShellHolder.getShell(), this);
        System.out.println("touch: " + res);
        if (res == null) {
            return false;
        }
        return this.init(res);
    }

    @Override
    public boolean mkdir() {
        final String res = CommandLineUtils.mkdir(ShellHolder.getShell(), this);
        if (res == null) {
            return false;
        }
        return this.init(res);
    }

    @Override
    public boolean mkdirs() {
        final String res = CommandLineUtils.mkdirs(ShellHolder.getShell(), this);
        if (res == null) {
            return false;
        }
        return this.init(res);
    }

    /**
     * Returns true, if this file points to the same location
     * 
     * @param arg0
     *            File to compare to
     * @return true, if this file points to the same location
     */
    @Override
    public int compareTo(GenericFile arg0) {
        return this.file.compareTo(arg0.toFile());
    }

    @Override
    public String getName() {
        return this.file.getName();
    }

    @Override
    public String getPath() {
        return this.file.getPath();
    }

    @Override
    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    @Override
    public String getCanonicalPath() {
        try {
            return this.file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CommandLineFile) {
            return ((CommandLineFile) obj).file.equals(this.file);
        }
        return false;
    }

    @Override
    public long getFreeSpace() {
        return this.file.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return this.file.getTotalSpace();
    }

    @Override
    public String getParent() {
        return this.file.getParent();
    }

    @Override
    public CommandLineFile getParentFile() {
        return CommandLineFile.fromFile(this.file.getParentFile());
    }

    @Override
    public boolean isDirectory() {
        return this.isDirectory;
    }
    
    @Override
    public boolean isHidden() {
        return this.file.isHidden();
    }

    @Override
    public boolean renameTo(final File newName) {
        final Command move = new Move(this, newName);
        final boolean result = CommandLine.execute(ShellHolder.getShell(), move);
        if (result) {
            this.exists = false;
            this.isDirectory = false;
            this.isSymlink = false;
            this.owner = 0;
            this.group = 0;
        }
        return result;
    }

    @Override
    public Permissions getPermissions() {
        return this.p;
    }
    
    public boolean applyPermissions(Permissions newPerm) {
        if (this.p.equals(newPerm)) {
            return true;
        }
        final boolean result = CommandLineUtils.applyPermissions(ShellHolder.getShell(), newPerm, this);
        if (result) {
            this.p = newPerm;
        }
        return result;
    }

    @Override
    public boolean copy(GenericFile target) {
        if (!this.exists) {
            return false;
        }
        return CommandLine.execute(ShellHolder.getShell(),
                new Copy(this, target));
    }

    @Override
    public boolean move(GenericFile target) {
        if (!this.exists) {
            return false;
        }
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new Move(this, target));
        if (result) {
            this.exists = false;
            this.isDirectory = false;
            this.isSymlink = false;
            this.owner = 0;
            this.group = 0;
            this.p = null;
        }
        return result;
    }
    
    public int getOwner() {
        return this.owner;
    }
    
    public int getGroup() {
        return this.group;
    }
    
    //public boolean isMSMDOS() {
    //    return this.isMSDOS;
    //}

    public boolean canRead() {
        if (!this.exists) {
            return false;
        }
        if (this.group == Constants.GID_SDCARD) {
            return this.p.gr;
        }
        return this.p.or;
    }

    protected boolean canWrite() {
        if (this.group == Constants.GID_SDCARD) {
            return this.p.gw;
        }
        return this.p.ow;
    }

    protected boolean canExecute() {
        if (!this.exists) {
            return false;
        }
        if (this.group == Constants.GID_SDCARD) {
            return this.p.gx;
        }
        return this.p.ox;
    }
}
