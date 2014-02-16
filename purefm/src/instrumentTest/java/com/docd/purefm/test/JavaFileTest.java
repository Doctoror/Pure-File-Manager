package com.docd.purefm.test;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.docd.purefm.ActivityMonitor;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.PureFMTextUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class JavaFileTest extends AndroidTestCase {

    private static final File testDir = new File(Environment.getExternalStorageDirectory(), "test");

    private static File test1 = new File(testDir, "test1.jpg");

    @Override
    protected void setUp() {
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("Make sure the external storage is mounted read-write before running this test");
        }
        try {
            FileUtils.forceDelete(testDir);
        } catch (IOException e) {

        }
        testDir.mkdirs();

        // init what application inits
        final Context context = this.getContext();
        ActivityMonitor.init(context);
        com.docd.purefm.Environment.init(context);
        Settings.init(context, context.getResources());
        PreviewHolder.initialize(context);
        PureFMTextUtils.init(context);

        // prepare a test file
        try {
            FileUtils.write(test1, "test");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file: " + e);
        }
    }

    @Override
    protected void runTest() {
        Settings.useCommandLine = false;
        test();
        test1.delete();
        test();
        try {
            test1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        test();
        test1.delete();
        test1.mkdir();
        test();
    }

    private void test() {
        final JavaFile file1 = (JavaFile) FileFactory.newFile(test1);
        testAgainstJavaIoFile(file1, test1);
    }

    private static void testAgainstJavaIoFile(final JavaFile genericFile, final File javaFile) {
        assertEquals(javaFile, genericFile.toFile());
        assertEquals(javaFile.getName(), genericFile.getName());
        assertEquals(javaFile.getAbsolutePath(), genericFile.getAbsolutePath());
        assertEquals(javaFile.canRead(), genericFile.canRead());
        assertEquals(javaFile.canWrite(), genericFile.canWrite());
        assertEquals(javaFile.canExecute(), genericFile.canExecute());
        assertEquals(javaFile.exists(), genericFile.exists());
        assertEquals(javaFile.getPath(), genericFile.getPath());
        assertEquals(javaFile.getParent(), genericFile.getParent());
        assertEquals(javaFile.length(), genericFile.length());
        final File parentFile;
        final GenericFile genericParentFile = genericFile.getParentFile();
        if (genericParentFile == null) {
            parentFile = null;
        } else {
            parentFile = genericParentFile.toFile();
        }
        assertEquals(javaFile.getParentFile(), parentFile);
        assertEquals(javaFile.length(), genericFile.length());
        try {
            assertEquals(FileUtils.isSymlink(javaFile), genericFile.isSymlink());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(javaFile.getCanonicalPath(), genericFile.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(javaFile.length(), genericFile.length());
        assertEquals(javaFile.lastModified(), genericFile.lastModified());
        assertEquals(javaFile.isDirectory(), genericFile.isDirectory());
        assertTrue(Arrays.equals(javaFile.list(), genericFile.list()));
        assertTrue(Arrays.equals(javaFile.listFiles(), genericFile.listFiles()));
    }
}
