package com.docd.purefm.test;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.docd.purefm.ActivityMonitor;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.TextUtil;

import java.io.File;

public final class CommandLineFileTest extends AndroidTestCase {

    private static final File testDir = new File(Environment.getExternalStorageDirectory(), "test");
    private static final File busybox = new File(Environment.getExternalStorageDirectory(), "busybox");

    private static File test1 = new File(testDir, "test1.jpg");
    private static File test2 = new File(testDir, "test2.jpg");
    private static File test3 = new File(testDir.getAbsolutePath() + "/test3dir/", "test3.jpg");

    @Override
    protected void setUp() {
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("Make sure the external storage is mounted read-write before running this test");
        }
        if (!busybox.exists()) {
            throw new RuntimeException("before running this test copy busybox to " + busybox.getAbsolutePath());
        }
        testDir.mkdirs();

        com.docd.purefm.Environment.hasBusybox = true;
        Settings.useCommandLine = true;

        // init what application inits
        final Context context = this.getContext();
        ActivityMonitor.init(context);
        com.docd.purefm.Environment.init(context);
        Settings.init(context, context.getResources());
        PreviewHolder.initialize(context);
        TextUtil.init(context);

        //create a non-empty file
        if (!CommandLine.execute(ShellHolder.getShell(), "echo \"test\" >> " + test1.getAbsolutePath())) {
            throw new RuntimeException("Failed to create test file");
        }
    }

    @Override
    protected void runTest() {
        com.docd.purefm.Environment.hasBusybox = true;
        Settings.useCommandLine = true;

        final CommandLineFile file1 = (CommandLineFile) FileFactory.newFile(test1);
        assertEquals(test1.getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(test1, file1.toFile());
        assertIsNormalFile(file1);
        assertEquals(6, file1.length());

        file1.delete();
        assertIsEmptyFile(file1);

        file1.createNewFile();
        assertIsNormalFile(file1);

        final CommandLineFile file2 = (CommandLineFile) FileFactory.newFile(test2);
        assertIsEmptyFile(file2);

        file1.move(file2);
        assertIsEmptyFile(file1);
        assertIsNormalFile(file2);

        file2.copy(file1);
        assertIsNormalFile(file1);
        assertIsNormalFile(file2);

        file1.delete();
        file2.delete();

        file1.mkdir();
        assertIsDirectory(file1);

        final CommandLineFile file3 = (CommandLineFile) FileFactory.newFile(test3);
        assertIsEmptyFile(file3);
        file3.mkdirs();
        assertIsDirectory(file3);
    }

    private void assertIsDirectory(final CommandLineFile file) {
        assertEquals(true, file.exists());
        assertEquals(true, file.canRead());
        assertEquals(false, file.isSymlink());
        assertEquals(true, file.isDirectory());
    }

    private void assertIsNormalFile(final CommandLineFile file) {
        assertEquals(true, file.exists());
        assertEquals(true, file.canRead());
        assertEquals(false, file.isSymlink());
        assertEquals(false, file.isDirectory());
    }

    private void assertIsEmptyFile(final CommandLineFile file) {
        assertEquals(false, file.exists());
        assertEquals(false, file.canRead());
        assertEquals(false, file.isSymlink());
        assertEquals(false, file.isDirectory());
        assertEquals(0, file.length());
    }

    @Override
    protected void tearDown() {
        CommandLine.execute(ShellHolder.getShell(), "rm -rf " + testDir.getAbsolutePath());
    }
}
