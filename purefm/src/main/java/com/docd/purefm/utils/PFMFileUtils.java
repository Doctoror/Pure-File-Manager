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
package com.docd.purefm.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandCopyRecursively;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandMove;
import com.docd.purefm.commandline.CommandRemove;
import com.docd.purefm.commandline.CommandStat;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public final class PFMFileUtils {

    private PFMFileUtils() {
    }

    public static final class FileNameFilter implements InputFilter {

        public static final int MAX_FILENAME_LENGTH = 255;

        private static final String FILENAME_PATTERN =
                "\\\\|/|:|\\*|\\?|\"|<|>|\r|\n";

        @Nullable
        @Override
        public CharSequence filter(final CharSequence source,
                                   final int start,
                                   final int end,
                                   final Spanned dest,
                                   final int dstart,
                                   final int dend) {
            if (source.length() == 0) {
                //nothing to filter
                return null;
            }
            final CharSequence sourceSubSequence = source.subSequence(start, end);
            final String sourceSubSequenceString = sourceSubSequence.toString();
            final String sourceProcessed = sourceSubSequenceString.replaceAll(FILENAME_PATTERN, "");
            if (sourceSubSequenceString.equals(sourceProcessed)) {
                // nothing filtered
                return null;
            }
            if (source instanceof Spanned) {
                // copy spans from original source
                final Spannable processed = new SpannableString(sourceProcessed);
                TextUtils.copySpansFrom((Spanned) source, start, processed.length(), null, processed, 0);
                return processed;
            }
            return sourceProcessed;
        }
    }

    /**
     * Returns canonical path or absolute path if failed
     *
     * @param file File to get full path
     * @return canonical path or absolute path if failed
     */
    @NonNull
    public static String fullPath(@NonNull final GenericFile file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Returns canonical path or absolute path if failed
     *
     * @param file File to get full path
     * @return canonical path or absolute path if failed
     */
    @NonNull
    public static String fullPath(@NonNull final File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    @NonNull
    public static String byteCountToDisplaySize(@NonNull final BigInteger size) {
        String displaySize;

        if (size.divide(FileUtils.ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(FileUtils.ONE_GB_BI)) + " GiB";
        } else if (size.divide(FileUtils.ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(FileUtils.ONE_MB_BI)) + " MiB";
        } else if (size.divide(FileUtils.ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(FileUtils.ONE_KB_BI)) + " KiB";
        } else {
            displaySize = String.valueOf(size) + " B";
        }
        return displaySize;
    }

    @Nullable
    public static String resolveFileSystem(@NonNull final GenericFile file) {
        final String path = PFMFileUtils.fullPath(file);
        return resolveFileSystem(FilenameUtils.getFullPathNoEndSeparator(path));
    }


    @Nullable
    public static String resolveFileSystem(@NonNull final String path) {
        for (final StorageHelper.Volume v : Environment.getVolumes()) {
            if (path.startsWith(v.file.getAbsolutePath())) {
                return v.fileSystem;
            }
        }

        if (!ShellHolder.getInstance().hasShell()) {
            Log.w("resolveFileSystem()", "no shell, aborting");
            return null;
        }

        final List<String> fsTypeResult = CommandLine.executeForResult(new CommandStat(path));
        return fsTypeResult == null || fsTypeResult.isEmpty() ?
                null : fsTypeResult.get(0);
    }
    
    public static void openFileInExternalApp(@NonNull final Context context,
                                             @NonNull final File target) {
        final String mime = MimeTypes.getMimeType(target);
        if (mime != null) {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(target), mime);
            final PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                throw new IllegalArgumentException("No PackageManager for context");
            }
            if (packageManager.queryIntentActivities(i, 0).isEmpty()) {
                Toast.makeText(context, R.string.no_apps_to_open, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                context.startActivity(i);
            } catch (Exception e) {
                Toast.makeText(context, context.getString(R.string.could_not_open_file_) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void moveFile(@NonNull final GenericFile source,
                                @NonNull final GenericFile target,
                                final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (target.exists()) {
                throw new FileExistsException("Target exists");
            }
            final boolean result = CommandLine.execute(new CommandMove(source, target));
            if (!result) {
                throw new IOException("Move failed");
            }
        } else {
            FileUtils.moveFile(source.toFile(), target.toFile());
        }
    }

    public static void copyFile(@NonNull final GenericFile source,
                                @NonNull final GenericFile target,
                                final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            final boolean result = CommandLine.execute(new CommandCopyRecursively(source, target));
            if (!result) {
                throw new IOException("Move failed");
            }
        } else {
            FileUtils.copyFile(source.toFile(), target.toFile());
        }
    }

    public static void copyFileToDirectory(@NonNull final GenericFile source,
                                           @NonNull final GenericFile target,
                                           final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (!target.exists()) {
                throw new FileNotFoundException("Target doesn't exist");
            }
            if (!target.isDirectory()) {
                throw new IllegalArgumentException("Target is not a directory");
            }
            final boolean result = CommandLine.execute(new CommandCopyRecursively(source, target));
            if (!result) {
                throw new IOException("Move failed");
            }
        } else {
            FileUtils.copyFileToDirectory(source.toFile(), target.toFile());
        }
    }

    public static void moveDirectory(@NonNull final GenericFile source,
                                     @NonNull final GenericFile target,
                                     final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (target.exists()) {
                throw new FileExistsException("Target exists");
            }
            final boolean result = CommandLine.execute(new CommandMove(source, target));
            if (!result) {
                throw new IOException("Move failed");
            }
        } else {
            FileUtils.moveDirectory(source.toFile(), target.toFile());
        }
    }

    public static void moveToDirectory(@NonNull final GenericFile source,
                                       @NonNull final GenericFile target,
                                       final boolean useCommandLine,
                                       final boolean createDestDir) throws IOException {

        if (useCommandLine) {
            if (!source.exists()) {
                throw new FileNotFoundException("Source '" + source + "' does not exist");
            }
            if (target.exists()) {
                if (!target.isDirectory()) {
                    throw new IOException("Target is not a directory");
                }
            } else {
                if (createDestDir) {
                    if (!target.mkdirs()) {
                        throw new IOException("Failed to create target directory");
                    }
                } else {
                    throw new FileNotFoundException("Target directory doesn't exist");
                }
            }
            final boolean result = CommandLine.execute(new CommandMove(source, target));
            if (!result) {
                throw new IOException("Moving failed");
            }
        } else {
            FileUtils.moveToDirectory(source.toFile(), target.toFile(), createDestDir);
        }
    }

    public static void copyDirectory(@NonNull final GenericFile source,
                                     @NonNull final GenericFile target,
                                     final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (!source.exists()) {
                throw new FileNotFoundException("Source '" + source + "' does not exist");
            }
            if (!source.isDirectory()) {
                throw new IOException("Source '" + source + "' exists but is not a directory");
            }
            if (source.getCanonicalPath().equals(target.getCanonicalPath())) {
                throw new IOException("Source '" + source + "' and destination '" + target +
                        "' are the same");
            }
            final boolean result = CommandLine.execute(new CommandCopyRecursively(source, target));
            if (!result) {
                throw new IOException("Copying failed");
            }
        } else {
            FileUtils.copyDirectory(source.toFile(), target.toFile());
        }
    }

    public static void copyDirectoryToDirectory(@NonNull final GenericFile source,
                                                @NonNull final GenericFile target,
                                                final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (!source.exists()) {
                throw new FileNotFoundException("Source '" + source + "' does not exist");
            }
            if (!source.isDirectory()) {
                throw new IOException("Source '" + source + "' exists but is not a directory");
            }
            if (source.getCanonicalPath().equals(target.getCanonicalPath())) {
                throw new IOException("Source '" + source + "' and destination '" + target +
                        "' are the same");
            }
            if (!target.exists()) {
                throw new FileNotFoundException("Target dir does not exist");
            }
            if (!target.isDirectory()) {
                throw new IOException("Target is not a directory");
            }
            final boolean result = CommandLine.execute(new CommandCopyRecursively(source, target));
            if (!result) {
                throw new IOException("Copying failed");
            }
        } else {
            FileUtils.copyDirectoryToDirectory(source.toFile(), target.toFile());
        }
    }

    public static void forceDelete(@NonNull final GenericFile file,
                                   final boolean useCommandLine) throws IOException {
        if (useCommandLine) {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            final boolean result = CommandLine.execute(new CommandRemove(file.toFile()));
            if (!result) {
                throw new IOException("Removing failed");
            }
        } else {
            FileUtils.forceDelete(file.toFile());
        }
    }
    
    public static final class NameComparator implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
            }
            
            if (a.isDirectory()) {
                return -1;
            }
            
            if (b.isDirectory()) {
                return 1;
            }
            
            return a.getName().compareTo(b.getName());
        }
    }
    
    public static final class NameComparatorReverse implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                final int res = String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
                if (res == 0) {
                    return 0;
                }
                if (res < 0) {
                    return 1;
                }
                return -1;
            }
            
            if (a.isDirectory()) {
                return -1;
            }
            
            if (b.isDirectory()) {
                return 1;
            }
            
            final int res = a.getName().compareTo(b.getName());
            if (res == 0) {
                return 0;
            }
            if (res < 0) {
                return 1;
            }
            return -1;
        }
    }
    
    public static final class DateComparatorAsc implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            final long date_a = a.lastModified();
            final long date_b = b.lastModified();
            
            if (date_a == date_b) {
                return 0;
            }
            
            if (date_a < date_b) {
                return -1;
            }
            
            return 1;
        }
    }
    
    public static final class DateComparatorDesc implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            final long date_a = a.lastModified();
            final long date_b = b.lastModified();
            
            if (date_a == date_b) {
                return 0;
            }
            
            if (date_a < date_b) {
                return 1;
            }
            
            return -1;
        }
    }
    
    public static final class SizeComparatorAsc implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (a.isDirectory()) {
                return -1;
            }
            
            if (b.isDirectory()) {
                return 1;
            }
            
            final long len_a = a.length();
            final long len_b = b.length();
            
            if (len_a == len_b) {
                return a.getName().compareTo(b.getName());
            }
            
            if (len_a < len_b) {
                return -1;
            }
            
            return 1;
        }
    }
    
    public static final class SizeComparatorDesc implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (a.isDirectory()) {
                return 1;
            }
            
            if (b.isDirectory()) {
                return -1;
            }
            
            final long len_a = a.length();
            final long len_b = b.length();
            
            if (len_a == len_b) {
                return a.getName().compareTo(b.getName());
            }
            
            if (len_a < len_b) {
                return 1;
            }
            
            return -1;
        }
    }
    
    public static final class TypeComparator implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            
            if (a.isDirectory() && b.isDirectory()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (a.isDirectory()) {
                return -1;
            }
            
            if (b.isDirectory()) {
                return 1;
            }
            
            final String ext_a = FilenameUtils.getExtension(a.getName());
            final String ext_b = FilenameUtils.getExtension(b.getName());
            
            if (ext_a.isEmpty() && ext_b.isEmpty()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (ext_a.isEmpty()) {
                return -1;
            }
            
            if (ext_b.isEmpty()) {
                return 1;
            }
            
            final int res = ext_a.compareTo(ext_b);
            if (res == 0) {
                return a.getName().compareTo(b.getName());
            }
            return res;
        }
    }
    
    public static final class TypeComparatorReverse implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (a.isDirectory()) {
                return -1;
            }
            
            if (b.isDirectory()) {
                return 1;
            }
            
            final String ext_a = FilenameUtils.getExtension(a.getName());
            final String ext_b = FilenameUtils.getExtension(b.getName());
            
            if (ext_a.isEmpty() && ext_b.isEmpty()) {
                return a.getName().compareTo(b.getName());
            }
            
            if (ext_a.isEmpty()) {
                return 1;
            }
            
            if (ext_b.isEmpty()) {
                return -1;
            }
            
            final int res = ext_a.compareTo(ext_b);
            if (res == 0) {
                return a.getName().compareTo(b.getName());
            }
            return res;
        }
    }
}
