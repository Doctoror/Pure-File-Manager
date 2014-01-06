package com.docd.purefm.utils;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.settings.Settings;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

public final class PureFMFileUtils {

    private PureFMFileUtils() {
    }
    
    private static final String FORBIDDEN_CHARS = "/?<>\\:*|\"";
    
    private static final InputFilter FILENAME_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, 
                Spanned dest, int dstart, int dend)
        {
            for (int i = start; i < end; i++) { 
                if (FORBIDDEN_CHARS.indexOf(source.charAt(i)) != -1) {
                    return "";
                }
            }
            return source;
        }
    };
    
    public static InputFilter[] FILENAME_FILTERS = new InputFilter[] {
        FILENAME_FILTER
    };
    
    /**
     * Determines if a given filename is valid
     *  
     * @param name Name to test
     * @return true, if given name is a valid file name
     */
    public static boolean isValidFileName(String name) {
        if (name.length() == 0 || name.startsWith(".")) {
            return false;
        }
        final Pattern p = Pattern.compile("[^\\\\/:*?\"<>|\\r\\n]*");
        return p.matcher(name).matches();
    }
    
    public static void requestMediaScanner(Context context, List<File> files) {
        final String[] paths = new String[files.size()];
        int i = 0;
        for (File file : files) {
            paths[i] = file.getAbsolutePath();
            i++;
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }
    
    public static void openFile(final Context context, final File target) {
        final String mime = MimeTypes.getMimeType(target);
        if (mime != null) {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(target), mime);
            if (context.getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
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
    
    public static final class NameComparator implements Comparator<GenericFile> {
        
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
            
            return a.getName().compareTo(b.getName());
        }
    }
    
    public static final class NameComparatorReverse implements Comparator<GenericFile> {
        
        @Override
        public int compare(final GenericFile a, final GenericFile b) {
            if (a.isDirectory() && b.isDirectory()) {
                final int res = a.getName().compareTo(b.getName());
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
