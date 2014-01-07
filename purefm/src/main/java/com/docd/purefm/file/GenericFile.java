package com.docd.purefm.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Serializable;

public interface GenericFile extends Serializable {

    boolean createNewFile();
    boolean mkdir();
    boolean mkdirs();
    boolean copy(GenericFile target);
    boolean move(GenericFile target);
    boolean delete();
    boolean exists();
    boolean isDirectory();
    boolean isHidden();
    boolean isSymlink();
    boolean renameTo(File newName);
    long length();
    long lastModified();
    long getFreeSpace();
    long getTotalSpace();
    int getTypeIcon();
    Permissions getPermissions();
    String getPath();
    String getMimeType();
    String getName();
    String getAbsolutePath();
    String getCanonicalPath();
    String getParent();
    String humanReadableLength();
    String humanReadableLastModified();
    GenericFile getParentFile();
    File toFile();
    GenericFile[] listFiles();
    GenericFile[] listFiles(FileFilter filter);
    GenericFile[] listFiles(FilenameFilter filter);
    GenericFile[] listFiles(GenericFileFilter filter);
    String[] list();

    
}
