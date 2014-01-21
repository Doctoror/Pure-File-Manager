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
package com.docd.purefm.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.commandline.Constants;
import com.docd.purefm.commandline.CopyCommand;
import com.docd.purefm.commandline.MoveCommand;
import com.docd.purefm.commandline.RemoveCommand;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.PureFMTextUtils;

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
    
    private boolean isSymlink;
    private boolean isDirectory;

    private CommandLineFile(File file) {
        this.file = file;
    }

    private CommandLineFile(String path) {
        this.file = new File(path);
    }

    private CommandLineFile(File parent, String line) {
        this.file = new File(parent, line);
    }

    @Override
    public boolean isSymlink() {
        return this.isSymlink;
    }

    @NotNull
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

    @NotNull
    public static CommandLineFile fromLSL(File parent, String line) {

        if (line.isEmpty()) {
            throw new IllegalArgumentException("Bad ls -lApe output: is empty");
        }

        final String[] attrs = getAttrs(line);
        for (final String attr : attrs) {
            if (attr == null) {
                throw new IllegalArgumentException("Bad ls -lApe output: attr was null");
            }
        }

        String name = attrs[LS_FILE];
        // if is symlink then resolve real path
        //String targetName = null;
        final int index = name.indexOf("->");
        if (index != -1) {
            //targetName = name.substring(index + 3).trim();
            name = name.substring(0, index).trim();
        }
        final CommandLineFile f = parent == null ? new CommandLineFile(name)
                : new CommandLineFile(parent, name);
        init(f, line);
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

    /**
     * Reads parameters from line and applies them to targetFile
     *
     * @param targetFile CommandLineFile to initialize
     * @param line ls -lApe output
     */
    private static void init(final CommandLineFile targetFile, final String line) {
        if (line.isEmpty()) {
            throw new IllegalArgumentException("Bad ls -lApe output: is empty");
        }
        final String[] attrs = getAttrs(line);
        for (final String attr : attrs) {
            if (attr == null) {
                throw new IllegalArgumentException("Bad ls -lApe output: attr was null");
            }
        }

        init(targetFile, getAttrs(line));
    }

    /**
     * Applies attrs to targetFile
     *
     * @param targetFile CommandLineFile to initialize
     * @param attrs Attributes read from ls -lApe output
     */
    private static void init(final CommandLineFile targetFile, String[] attrs) {
        final String sourceName = attrs[LS_FILE];
        final String perm = attrs[LS_PERMISSIONS];
        targetFile.isSymlink = perm.charAt(0) == 'l';
        targetFile.isDirectory = sourceName.endsWith(File.separator);

        targetFile.p = new Permissions(perm);
        targetFile.owner = Integer.parseInt(attrs[LS_USER]);
        targetFile.group = Integer.parseInt(attrs[LS_GROUP]);
        final String len = attrs[LS_FILE_SIZE];
        if (len != null && !len.isEmpty()) {
            targetFile.length = Long.parseLong(len);
        }

        final Calendar c = Calendar.getInstance(Locale.US);
        c.set(Calendar.YEAR, Integer.parseInt(attrs[LS_YEAR]));
        c.set(Calendar.MONTH, PureFMTextUtils.stringMonthToInt(attrs[LS_MONTH]));
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(attrs[LS_DAY_OF_MONTH]));

        final int index1 = attrs[LS_TIME].indexOf(':');
        final int index2 = attrs[LS_TIME].lastIndexOf(':');
        if (index1 != -1 && index2 != -1) {
            c.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(attrs[LS_TIME].substring(0, index1)));
            c.set(Calendar.MINUTE,
                    Integer.parseInt(attrs[LS_TIME].substring(index1 + 1, index2)));
            c.set(Calendar.SECOND,
                    Integer.parseInt(attrs[LS_TIME].substring(index2 + 1)));
        }

        targetFile.lastmod = c.getTimeInMillis();
        targetFile.exists = true;
        if (!targetFile.isDirectory) {
            targetFile.mimeType = MimeTypes.getMimeType(targetFile.file);
            targetFile.icon = MimeTypes.getTypeIcon(targetFile.file);
        }
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
        if (CommandLine.execute(ShellHolder.getShell(), new RemoveCommand(this.file))) {
            this.exists = false;
            this.isDirectory = false;
            this.isSymlink = false;
            this.owner = 0;
            this.group = 0;
            this.length = 0;
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
    public long lastModified() {
        return this.lastmod;
    }

    @Override
    public boolean createNewFile() {
        if (this.exists) {
            return false;
        }

        final String res = CommandLineUtils.touch(ShellHolder.getShell(), this);
        if (res == null) {
            return false;
        }
        init(this, res);
        return true;
    }

    @Override
    public boolean mkdir() {
        final String res = CommandLineUtils.mkdir(ShellHolder.getShell(), this);
        if (res == null) {
            return false;
        }
        init(this, res);
        return true;
    }

    @Override
    public boolean mkdirs() {
        final String res = CommandLineUtils.mkdirs(ShellHolder.getShell(), this);
        if (res == null) {
            return false;
        }
        init(this, res);
        return true;
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
    public int hashCode() {
        return this.file.hashCode();
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
        final Command move = new MoveCommand(this.getAbsolutePath(), newName.getAbsolutePath());
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
    public boolean copy(final GenericFile target) {
        if (!this.exists) {
            return false;
        }
        return CommandLine.execute(ShellHolder.getShell(),
                new CopyCommand(this, target));
    }

    @Override
    public boolean move(final GenericFile target) {
        if (!this.exists) {
            return false;
        }
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new MoveCommand(this, target));
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
