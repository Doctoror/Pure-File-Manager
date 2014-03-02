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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.docd.purefm.Environment;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandChmod;
import com.docd.purefm.commandline.CommandCopyRecursively;
import com.docd.purefm.commandline.CommandDu;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandListContents;
import com.docd.purefm.commandline.CommandListFile;
import com.docd.purefm.commandline.CommandMkdir;
import com.docd.purefm.commandline.CommandMkdirs;
import com.docd.purefm.commandline.CommandMove;
import com.docd.purefm.commandline.CommandRemove;
import com.docd.purefm.commandline.CommandTouch;
import com.docd.purefm.commandline.Constants;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.PureFMTextUtils;

public final class CommandLineFile implements GenericFile,
        Comparable<GenericFile> {

    private static final long serialVersionUID = -8173533665283968040L;

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

    private final File mFile;
    private final String mCanonicalPath;
    private Permissions mPermissions;
    private long mLength;
    private long mLastmod;
    private boolean mExists;

    private int mOwner;
    private int mGroup;

    private String mMimeType;
    
    private boolean mIsSymlink;
    private boolean mIsDirectory;

    private CommandLineFile(@NotNull File file) {
        this.mFile = file;
        this.mCanonicalPath = null;
    }

    private CommandLineFile(@NotNull String path) {
        this.mFile = new File(path);
        this.mCanonicalPath = null;
    }

    private CommandLineFile(@NotNull File parent, @NotNull String name) {
        this.mFile = new File(parent, name);
        this.mCanonicalPath = null;
    }

    private CommandLineFile(@NotNull File parent, @NotNull  String name,
                            @NotNull String canonicalPath) {
        this.mFile = new File(parent, name);
        this.mCanonicalPath = canonicalPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSymlink() {
        return this.mIsSymlink;
    }

    @NotNull
    public static CommandLineFile fromFile(File file) {
        final List<String> res = CommandLine.executeForResult(ShellHolder.getShell(), new CommandListFile(file));

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
        String canonicalPath;
        // if is symlink then resolve real path
        //String targetName = null;
        final int index = name.indexOf("->");
        if (index != -1) {
            canonicalPath = name.substring(index + 3).trim();
            name = name.substring(0, index).trim();
        }
        final CommandLineFile f = parent == null ? new CommandLineFile(name)
                : new CommandLineFile(parent, name);
        init(f, line);
        return f;
    }

    @NotNull
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
        targetFile.mIsSymlink = perm.charAt(0) == 'l';
        targetFile.mIsDirectory = sourceName.endsWith(File.separator);

        targetFile.mPermissions = new Permissions(perm);
        targetFile.mOwner = Integer.parseInt(attrs[LS_USER]);
        targetFile.mGroup = Integer.parseInt(attrs[LS_GROUP]);
        final String len = attrs[LS_FILE_SIZE];
        if (len != null && !len.isEmpty()) {
            targetFile.mLength = Long.parseLong(len);
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

        targetFile.mLastmod = c.getTimeInMillis();
        targetFile.mExists = true;
        if (!targetFile.mIsDirectory) {
            targetFile.mMimeType = MimeTypes.getMimeType(targetFile.mFile);
        }
    }

    private void apply(final CommandLineFile other) {
        this.mPermissions = other.mPermissions;
        this.mLength = other.mLength;
        this.mLastmod = other.mLastmod;
        this.mExists = other.mExists;
        this.mOwner = other.mOwner;
        this.mGroup = other.mGroup;
        this.mMimeType = other.mMimeType;
        this.mIsSymlink = other.mIsSymlink;
        this.mIsDirectory = other.mIsDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getMimeType() {
        return this.mMimeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() {
        return this.mExists;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public File toFile() {
        return this.mFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete() {
        if (CommandLine.execute(ShellHolder.getShell(), new CommandRemove(this.mFile))) {
            this.mExists = false;
            this.mIsDirectory = false;
            this.mIsSymlink = false;
            this.mLastmod = 0;
            this.mLength = 0;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CommandLineFile[] listFiles() {
        final List<String> result = CommandLine.executeForResult(
                ShellHolder.getShell(), new CommandListContents(this));
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (final String f : result) {
            try {
                res.add(CommandLineFile.fromLSL(this.mFile, f));
            } catch (IllegalArgumentException e) {
                //e.printStackTrace();
                // not a valid ls -l file line
            }
        }
        final CommandLineFile[] ret = new CommandLineFile[res.size()];
        res.toArray(ret);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CommandLineFile[] listFiles(final FileFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLine.executeForResult(
                ShellHolder.getShell(),new CommandListContents(this));
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(mFile, f);
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

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CommandLineFile[] listFiles(final FilenameFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLine.executeForResult(
                ShellHolder.getShell(), new CommandListContents(this));
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(mFile, f);
                if (filter.accept(mFile, tmp.getName())) {
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

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CommandLineFile[] listFiles(final GenericFileFilter filter) {
        if (filter == null) {
            return listFiles();
        }
        final List<String> result = CommandLine.executeForResult(
                ShellHolder.getShell(), new CommandListContents(this));
        if (result == null) {
            return null;
        }
        final List<CommandLineFile> res = new ArrayList<CommandLineFile>(
                result.size());
        for (String f : result) {
            try {
                final CommandLineFile tmp = CommandLineFile.fromLSL(mFile, f);
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

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String[] list() {
        final GenericFile[] files = listFiles();
        if (files != null) {
            final String[] result = new String[files.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = files[i].getName();
                return result;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() {
        return this.mLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger lengthTotal() {
        if (mIsDirectory) {
            return BigInteger.valueOf(CommandDu.du_s(this));
        }
        return BigInteger.valueOf(mLength);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long lastModified() {
        return this.mLastmod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createNewFile() {
        if (this.mExists) {
            return false;
        }

        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new CommandTouch(mFile.getAbsolutePath()));
        if (result) {
            this.apply(CommandLineFile.fromFile(mFile));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdir() {
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new CommandMkdir(mFile.getAbsolutePath()));
        if (result) {
            this.apply(CommandLineFile.fromFile(mFile));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdirs() {
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new CommandMkdirs(mFile.getAbsolutePath()));
        if (result) {
            this.apply(CommandLineFile.fromFile(mFile));
        }
        return result;
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
        return this.mFile.compareTo(arg0.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getName() {
        return this.mFile.getName();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getPath() {
        return this.mFile.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getAbsolutePath() {
        return this.mFile.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getCanonicalPath() throws IOException {
        if (this.mCanonicalPath != null) {
            return this.mCanonicalPath;
        }
        return this.mFile.getCanonicalPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof CommandLineFile) {
            return ((CommandLineFile) obj).mFile.equals(this.mFile);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.mFile.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFreeSpace() {
        return this.mFile.getFreeSpace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalSpace() {
        return this.mFile.getTotalSpace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParent() {
        return this.mFile.getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CommandLineFile getParentFile() {
        final File parentFile = mFile.getParentFile();
        if (parentFile == null) {
            return null;
        }
        return CommandLineFile.fromFile(parentFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return this.mIsDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return this.mFile.isHidden() || getName().startsWith(".");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean renameTo(final GenericFile newName) {
        final Command move = new CommandMove(this.getAbsolutePath(), newName.getAbsolutePath());
        final boolean result = CommandLine.execute(ShellHolder.getShell(), move);
        if (result) {
            this.mExists = false;
            this.mIsDirectory = false;
            this.mIsSymlink = false;
            this.mOwner = 0;
            this.mGroup = 0;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Permissions getPermissions() {
        return this.mPermissions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyPermissions(final Permissions newPerm) {
        if (this.mPermissions.equals(newPerm)) {
            return true;
        }
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new CommandChmod(mFile.getAbsolutePath(), newPerm));
        if (result) {
            this.mPermissions = newPerm;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(final GenericFile target) {
        if (!this.mExists) {
            return false;
        }
        return CommandLine.execute(ShellHolder.getShell(),
                new CommandCopyRecursively(this, target));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(final GenericFile target) {
        if (!this.mExists) {
            return false;
        }
        final boolean result = CommandLine.execute(ShellHolder.getShell(),
                new CommandMove(this, target));
        if (result) {
            this.mExists = false;
            this.mIsDirectory = false;
            this.mIsSymlink = false;
            this.mOwner = 0;
            this.mGroup = 0;
            this.mPermissions = null;
        }
        return result;
    }

    /**
     * Returns owner id of this file
     *
     * @return owner id of this file
     */
    public int getOwner() {
        return this.mOwner;
    }

    /**
     * Returns group id of this file
     *
     * @return group id of this file
     */
    public int getGroup() {
        return this.mGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRead() {
        if (!this.mExists) {
            return false;
        }
        if (this.mOwner == Constants.SDCARD_RW ||
                this.mOwner == Constants.SDCARD_R ||
                this.mOwner == Constants.MEIDA_RW ||
                this.mOwner == Constants.MTP) {
            return this.mPermissions.ur;
        }
        if (this.mGroup == Constants.SDCARD_RW ||
                this.mGroup == Constants.SDCARD_R ||
                this.mGroup == Constants.MEIDA_RW ||
                this.mGroup == Constants.MTP) {
            return this.mPermissions.gr;
        }
        return this.mPermissions.or;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canWrite() {
        if (!this.mExists) {
            return false;
        }
        if (this.mOwner == Constants.SDCARD_RW ||
                this.mOwner == Constants.SDCARD_R || // on some devices it's still writable
                this.mOwner == Constants.MEIDA_RW ||
                this.mOwner == Constants.MTP) {
            return this.mPermissions.uw;
        }
        if (this.mGroup == Constants.SDCARD_RW ||
                this.mGroup == Constants.SDCARD_R || // on some devices it's still writable
                this.mGroup == Constants.MEIDA_RW ||
                this.mGroup == Constants.MTP) {
            return this.mPermissions.gw;
        }
        return this.mPermissions.ow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canExecute() {
        if (!this.mExists) {
            return false;
        }
        if (this.mOwner == Constants.SDCARD_RW ||
                this.mOwner == Constants.SDCARD_R ||
                this.mOwner == Constants.MEIDA_RW ||
                this.mOwner == Constants.MTP) {
            return this.mPermissions.ux;
        }
        if (this.mGroup == Constants.SDCARD_RW ||
                this.mGroup == Constants.SDCARD_R ||
                this.mGroup == Constants.MEIDA_RW ||
                this.mGroup == Constants.MTP) {
            return this.mPermissions.gx;
        }
        return this.mPermissions.ox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getAbsolutePath();
    }
}
