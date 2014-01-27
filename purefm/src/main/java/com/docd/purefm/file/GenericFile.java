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
    boolean renameTo(GenericFile newFile);
    long length();
    long lastModified();
    long getFreeSpace();
    long getTotalSpace();
    Permissions getPermissions();
    String getPath();
    String getMimeType();
    String getName();
    String getAbsolutePath();
    String getCanonicalPath();
    String getParent();
    GenericFile getParentFile();
    File toFile();
    GenericFile[] listFiles();
    GenericFile[] listFiles(FileFilter filter);
    GenericFile[] listFiles(FilenameFilter filter);
    GenericFile[] listFiles(GenericFileFilter filter);
    String[] list();

    
}
