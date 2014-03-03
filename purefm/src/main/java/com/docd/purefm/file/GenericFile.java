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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public interface GenericFile extends Serializable {

    /**
     * Creates a new, empty file on the file system according to the path
     * information stored in this file. This method returns true if it creates
     * a file, false if the file already existed. Note that it returns false
     * even if the file is not a file (because it's a directory, say).
     *
     * <p>Note that this method does <i>not</i> throw {@code IOException} if the file
     * already exists, even if it's not a regular file. Callers should always check the
     * return value
     *
     * @return true if the file has been created, false if it
     *         already exists.
     * @throws IOException if it's not possible to create the file.
     */
    boolean createNewFile() throws IOException;

    /**
     * Creates the directory named by this file, assuming its parents exist.
     * Use {@link #mkdirs} if you also want to create missing parents.
     *
     * <p>Note that this method does <i>not</i> throw {@code IOException} on failure.
     * Callers must check the return value. Note also that this method returns
     * false if the directory already existed. If you want to know whether the
     * directory exists on return, either use {@code (f.mkdir() || f.isDirectory())}
     * or simply ignore the return value from this method and simply call {@link #isDirectory}.
     *
     * @return {@code true} if the directory was created,
     *         {@code false} on failure or if the directory already existed.
     */
    boolean mkdir();

    /**
     * Creates the directory named by this file, creating missing parent
     * directories if necessary.
     * Use {@link #mkdir} if you don't want to create missing parents.
     *
     * <p>Note that this method does <i>not</i> throw {@code IOException} on failure.
     * Callers must check the return value. Note also that this method returns
     * false if the directory already existed. If you want to know whether the
     * directory exists on return, either use {@code (f.mkdirs() || f.isDirectory())}
     * or simply ignore the return value from this method and simply call {@link #isDirectory}.
     *
     * @return {@code true} if the directory was created,
     *         {@code false} on failure or if the directory already existed.
     */
    boolean mkdirs();

    /**
     * Moves the file to target file or directory
     *
     * @param target file or directory to move to
     * @return true if successfully moved
     */
    boolean copy(GenericFile target);

    /**
     * Moves the file to target file or directory
     *
     * @param target file or directory to move to
     * @return true if successfully moved
     */
    boolean move(GenericFile target);

    /**
     * Force-deletes this file.
     *
     * <p>Note that this method does <i>not</i> throw {@code IOException} on failure.
     * Callers must check the return value.
     *
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     */
    boolean delete();

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     *
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    boolean exists();

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     *
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     */
    boolean isDirectory();

    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For Unix
     * systems a file is considered hidden if its name starts with a ".". For
     * Windows systems there is an explicit flag in the file system for this
     * purpose.
     *
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     */
    boolean isHidden();

    /**
     * Determines whether the specified file is a Symbolic Link rather than an actual file.
     */
    boolean isSymlink();

    /**
     * Renames this file to {@code newPath}. This operation is supported for both
     * files and directories.
     *
     * <p>Many failures are possible. Some of the more likely failures include:
     * <ul>
     * <li>Write permission is required on the directories containing both the source and
     * destination paths.
     * <li>Search permission is required for all parents of both paths.
     * <li>Both paths be on the same mount point. On Android, applications are most likely to hit
     * this restriction when attempting to copy between internal storage and an SD card.
     * </ul>
     *
     * <p>Note that this method does <i>not</i> throw {@code IOException} on failure.
     * Callers must check the return value.
     *
     * @param newFile the new file.
     * @return true on success.
     */
    boolean renameTo(GenericFile newFile);

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist.
     * The result for a directory is not defined.
     *
     * @return the number of bytes in this file.
     */
    long length();

    /**
     * Returns the total length of this file in bytes.
     * Returns 0 if the file does not exist.
     * If this file is a directory, will return total directory size
     *
     * @return the number of bytes in this file or all containing file if is a directory
     */
    BigInteger lengthTotal();

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight.
     * Returns 0 if the file does not exist.
     *
     * @return the time when this file was last modified.
     */
    long lastModified();

    /**
     * Returns the number of free bytes on the partition containing this path.
     * Returns 0 if this path does not exist.
     *
     * <p>Note that this is likely to be an optimistic over-estimate and should not
     * be taken as a guarantee your application can actually write this many bytes.
     *
     */
    long getFreeSpace();

    /**
     * Returns the total size in bytes of the partition containing this path.
     * Returns 0 if this path does not exist.
     */
    long getTotalSpace();

    /**
     * Returns permissions for the file
     *
     * @return permissions for the file
     */
    @NotNull
    Permissions getPermissions();

    /**
     * Applies permissions to the file
     *
     * @param newPerm Permissions to apply
     * @return true, if the permissions was applied
     */
    boolean applyPermissions(Permissions newPerm);

    /**
     * Returns path of this file.
     *
     * @return path of this file
     */
    @NotNull
    String getPath();

    /**
     * Returns mime type of this file.
     *
     * @return mime type of this file
     */
    @Nullable
    String getMimeType();

    /**
     * Returns the name of the file or directory represented by this file.
     *
     * @return this file's name or an empty string if there is no name part in
     *         the file's path.
     */
    @NotNull
    String getName();

    /**
     * Returns the absolute path of this file. An absolute path is a path that starts at a root
     * of the file system. On Android, there is only one root: {@code /}.
     *
     * <p>A common use for absolute paths is when passing paths to a {@code Process} as
     * command-line arguments, to remove the requirement implied by relative paths, that the
     * child must have the same working directory as its parent.
     */
    @NotNull
    String getAbsolutePath();

    /**
     * Returns the canonical path of this file.
     * An <i>absolute</i> path is one that begins at the root of the file system.
     * A <i>canonical</i> path is an absolute path with symbolic links
     * and references to "." or ".." resolved. If a path element does not exist (or
     * is not searchable), there is a conflict between interpreting canonicalization
     * as a textual operation (where "a/../b" is "b" even if "a" does not exist) .
     *
     * <p>Most callers should use {@link #getAbsolutePath} instead. A canonical path is
     * significantly more expensive to compute, and not generally useful. The primary
     * use for canonical paths is determining whether two paths point to the same file by
     * comparing the canonicalized paths.
     *
     * <p>It can be actively harmful to use a canonical path, specifically because
     * canonicalization removes symbolic links. It's wise to assume that a symbolic link
     * is present for a reason, and that that reason is because the link may need to change.
     * Canonicalization removes this layer of indirection. Good code should generally avoid
     * caching canonical paths.
     *
     * @return the canonical path of this file.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @NotNull
    public String getCanonicalPath() throws IOException;

    /**
     * Returns the pathname of the parent of this file. This is the path up to
     * but not including the last name. {@code null} is returned if there is no
     * parent.
     *
     * @return this file's parent pathname or {@code null}.
     */
    @Nullable
    String getParent();

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name. {@code null} is
     * returned when there is no parent.
     *
     * @return a new file representing this file's parent or {@code null}.
     */
    @Nullable
    GenericFile getParentFile();


    /**
     * Returns java.io.File that represents this file
     *
     * @return java.io.File that represents this file
     */
    @NotNull
    File toFile();

    /**
     * Returns an array of files contained in the directory represented by this
     * file. The result is {@code null} if this file is not a directory. The
     * paths of the files in the array are absolute if the path of this file is
     * absolute, they are relative otherwise.
     *
     * @return an array of files or {@code null}.
     */
    @Nullable
    GenericFile[] listFiles();

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FileFilter and matching files are
     * returned as an array of files. Returns {@code null} if this file is not a
     * directory. If {@code filter} is {@code null} then all files match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     */
    @Nullable
    GenericFile[] listFiles(FileFilter filter);

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and files with matching
     * names are returned as an array of files. Returns {@code null} if this
     * file is not a directory. If {@code filter} is {@code null} then all
     * filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     */
    @Nullable
    GenericFile[] listFiles(FilenameFilter filter);

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FileFilter and matching files are
     * returned as an array of files. Returns {@code null} if this file is not a
     * directory. If {@code filter} is {@code null} then all files match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     */
    @Nullable
    GenericFile[] listFiles(GenericFileFilter filter);

    /**
     * Returns an array of strings with the file names in the directory
     * represented by this file. The result is {@code null} if this file is not
     * a directory.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directory are not returned as part of the list.
     *
     * @return an array of strings with file names or {@code null}.
     */
    @Nullable
    String[] list();

    /**
     * Indicates whether the current context is allowed to read from this file.
     *
     * @return {@code true} if this file can be read, {@code false} otherwise.
     */
    boolean canRead();

    /**
     * Indicates whether the current context is allowed to write to this file.
     *
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     */
    boolean canWrite();

    /**
     * Tests whether or not this process is allowed to execute this file.
     * Note that this is a best-effort result; the only way to be certain is
     * to actually attempt the operation.
     *
     * @return {@code true} if this file can be executed, {@code false} otherwise.
     */
    boolean canExecute();
}
