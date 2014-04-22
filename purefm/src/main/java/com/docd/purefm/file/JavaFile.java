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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.docd.purefm.Environment;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.PFMFileUtils;
import com.docd.purefm.utils.StorageHelper;

public final class JavaFile implements GenericFile, Comparable<GenericFile> {

    private static final long serialVersionUID = -2117911719748590982L;

    @NonNull
    private final File mFile;

    @NonNull
    private final Permissions p;

    private final boolean isSymlink;

    @Nullable
    private final String mimeType;

    public JavaFile(@NonNull final File file) {
        this.mFile = file;
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
    }
    
    public JavaFile(@NonNull final File dir, @NonNull final String name) {
        this.mFile = new File(dir, name);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(mFile);
    }

    public JavaFile(@NonNull final String dirPath, @NonNull final String name) {
        this.mFile = new File(dirPath, name);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(mFile);
    }

    public JavaFile(@NonNull final String path) {
        this.mFile = new File(path);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(mFile);
    }

    public JavaFile(@NonNull final URI uri) {
        this.mFile = new File(uri);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(mFile);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    private Permissions readPermissions() {
        boolean canWrite = mFile.canWrite();
        if (canWrite) {
            final StorageHelper.StorageVolume volume = Environment.volumeOfPath(
                    PFMFileUtils.fullPath(mFile));
            if (volume != null) {
                canWrite = !Environment.isReadOnly(volume);
            }
        }
        return new Permissions(mFile.canRead(), canWrite, mFile.canExecute());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    private JavaFile[] convert(final File[] files) {
        if (files == null) {
            return null;
        }
        final JavaFile[] res = new JavaFile[files.length];
        for (int i = 0; i < files.length; i++) {
            res[i] = new JavaFile(files[i]);
        }
        return res;
    }

    private boolean detectSymlink() {
        try {
            return FileUtils.isSymlink(this.mFile);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSymlink() {
        return this.isSymlink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return this.mFile.isHidden();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public File toFile() {
        return this.mFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete() {
        if (mFile.isDirectory()) {
            try {
                FileUtils.deleteDirectory(mFile);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return this.mFile.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public JavaFile[] listFiles() {
        return convert(this.mFile.listFiles());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public JavaFile[] listFiles(final FileFilter filter) {
        return convert(this.mFile.listFiles(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public JavaFile[] listFiles(final FilenameFilter filter) {
        return convert(this.mFile.listFiles(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public JavaFile[] listFiles(final GenericFileFilter filter) {
        final File[] files = this.mFile.listFiles();
        if (files == null) {
            return null;
        }
        
        final List<JavaFile> res = new LinkedList<>();
        for (final File file : files) {
            final JavaFile f = new JavaFile(file);
            if (filter.accept(f)) {
                res.add(f);
            }
        }
        
        final JavaFile[] result = new JavaFile[res.size()];
        int i = 0;
        for (JavaFile f : res) {
            result[i++] = f;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String[] list() {
        return this.mFile.list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() {
        return this.mFile.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger lengthTotal() {
        if (mFile.exists()) {
            try {
                return FileUtils.sizeOfAsBigInteger(mFile);
            } catch (StackOverflowError e) {
                //if we have too much directories, we can get this
                return BigInteger.valueOf(-1);
            }
        }
        return BigInteger.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long lastModified() {
        return this.mFile.lastModified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createNewFile() throws IOException {
        return this.mFile.createNewFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdir() {
        return this.mFile.mkdir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdirs() {
        return this.mFile.mkdirs();
    }

    /**
     * Returns true, if this file points to the same location
     * @param arg0 File to compare to
     * @return true, if this file points to the same location
     */
    @Override
    public int compareTo(@NonNull final GenericFile arg0) {
        return this.mFile.compareTo(arg0.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getName() {
        return this.mFile.getName();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getPath() {
        return this.mFile.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getAbsolutePath() {
        return this.mFile.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getCanonicalPath() throws IOException {
        return this.mFile.getCanonicalPath();
    }

    @NonNull
    @Override
    public JavaFile getCanonicalFile() throws IOException {
        return new JavaFile(this.mFile.getCanonicalPath());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof JavaFile) {
            return ((JavaFile) obj).mFile.equals(this.mFile);
        }
        return false;
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
    @Nullable
    @Override
    public String getParent() {
        return this.mFile.getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public JavaFile getParentFile() {
        final File parent = this.mFile.getParentFile();
        if (parent == null) {
            return null;
        }
        return new JavaFile(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return this.mFile.isDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean renameTo(@NonNull final GenericFile newName) {
        return this.mFile.renameTo(newName.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Permissions getPermissions() {
        return this.p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyPermissions(final Permissions newPerm) {
        boolean result;
        result = mFile.setReadable(newPerm.ur);
        result &= mFile.setWritable(newPerm.uw);
        result &= mFile.setExecutable(newPerm.ux);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() {
        return this.mFile.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRead() {
        return this.mFile.canRead();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canWrite() {
        return this.mFile.canWrite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canExecute() {
        return this.mFile.canExecute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.mFile.toString();
    }
}
