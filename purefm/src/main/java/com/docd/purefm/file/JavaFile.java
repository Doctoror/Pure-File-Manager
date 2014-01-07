package com.docd.purefm.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.docd.purefm.utils.Cache;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.TextUtil;

public final class JavaFile implements GenericFile, Comparable<GenericFile> {

    private static final long serialVersionUID = -2117911719748590982L;
    
    private final File file;
    private final Permissions p;
    private final boolean isSymlink; 
    private final String mimeType;
    private final int icon;
    private final String readableLastMod;
    private final String readableLength;
    
    public JavaFile(File file) {
        this.file = file;
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
        this.icon = MimeTypes.getTypeIcon(file);
        this.readableLastMod = TextUtil.readableDate(this.lastModified());
        this.readableLength = FileUtils.byteCountToDisplaySize(this.length());
        Cache.addTo(this);
    }
    
    public JavaFile(File dir, String name) {
        this.file = new File(dir, name);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
        this.icon = MimeTypes.getTypeIcon(file);
        this.readableLastMod = TextUtil.readableDate(this.lastModified());
        this.readableLength = FileUtils.byteCountToDisplaySize(this.length());
        Cache.addTo(this);
    }

    public JavaFile(String dirPath, String name) {
        this.file = new File(dirPath, name);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
        this.icon = MimeTypes.getTypeIcon(file);
        this.readableLastMod = TextUtil.readableDate(this.lastModified());
        this.readableLength = FileUtils.byteCountToDisplaySize(this.length());
        Cache.addTo(this);
    }

    public JavaFile(String path) {
        this.file = new File(path);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
        this.icon = MimeTypes.getTypeIcon(file);
        this.readableLastMod = TextUtil.readableDate(this.lastModified());
        this.readableLength = FileUtils.byteCountToDisplaySize(this.length());
        Cache.addTo(this);
    }

    public JavaFile(URI uri) {
        this.file = new File(uri);
        this.p = this.readPermissions();
        this.isSymlink = this.detectSymlink();
        this.mimeType = MimeTypes.getMimeType(file);
        this.icon = MimeTypes.getTypeIcon(file);
        this.readableLastMod = TextUtil.readableDate(this.lastModified());
        this.readableLength = FileUtils.byteCountToDisplaySize(this.length());
        Cache.addTo(this);
    }
    
    private Permissions readPermissions() {
        return new Permissions(this.file.canRead(), this.file.canWrite(), this.file.canExecute());
    }
    
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
            return FileUtils.isSymlink(this.file);
        } catch (IOException e) {
            return false;
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
    public boolean isSymlink() {
        return this.isSymlink;
    }
    
    @Override
    public boolean isHidden() {
        return this.file.isHidden();
    }
    
    @Override
    public File toFile() {
        return this.file;
    }

    @Override
    public boolean copy(GenericFile target) {
        try {
            if (this.file.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(this.file, target.toFile());
            } else {
                FileUtils.copyFileToDirectory(this.file, target.toFile());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean move(GenericFile target) {
        try {
            if (this.file.isDirectory()) {
                FileUtils.moveDirectoryToDirectory(this.file, target.toFile(), false);
            } else {
                FileUtils.moveFileToDirectory(this.file, target.toFile(), false);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public boolean delete() {
        if (this.file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(this.file);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return this.file.delete();
        }
    }
    
    @Override
    public JavaFile[] listFiles() {
        return convert(this.file.listFiles());
    }

    @Override
    public JavaFile[] listFiles(final FileFilter filter) {
        return convert(this.file.listFiles(filter));
    }

    @Override
    public JavaFile[] listFiles(final FilenameFilter filter) {
        return convert(this.file.listFiles(filter));
    }
    
    @Override
    public JavaFile[] listFiles(final GenericFileFilter filter) {
        final File[] files = this.file.listFiles(); 
        if (files == null) {
            return null;
        }
        
        final List<JavaFile> res = new LinkedList<JavaFile>();
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

    @Override
    public String[] list() {
        return this.file.list();
    }
    
    @Override
    public long length() {
        return this.file.length();
    }

    @Override
    public long lastModified() {
        return this.file.lastModified();
    }
    
    @Override
    public String humanReadableLength() {
        return this.readableLength;
    }

    @Override
    public String humanReadableLastModified() {
        return this.readableLastMod;
    }

    @Override
    public boolean createNewFile() {
        try {
            return this.file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean mkdir() {
        return this.file.mkdir();
    }

    @Override
    public boolean mkdirs() {
        return this.file.mkdirs();
    }

    /**
     * Returns true, if this file points to the same location
     * @param arg0 File to compare to
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
        if (obj instanceof JavaFile) {
            return ((JavaFile) obj).file.equals(this.file);
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
    public JavaFile getParentFile() {
        return new JavaFile(this.file.getParentFile());
    }

    @Override
    public boolean isDirectory() {
        return this.file.isDirectory();
    }
    
    @Override
    public boolean renameTo(File newName) {
        return this.file.renameTo(newName);
    }

    @Override
    public Permissions getPermissions() {
        return this.p;
    }

    @Override
    public boolean exists() {
        return this.file.exists();
    }

}
