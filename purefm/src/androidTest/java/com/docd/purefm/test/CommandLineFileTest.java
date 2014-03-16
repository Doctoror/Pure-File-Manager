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
package com.docd.purefm.test;

import android.content.Context;
import android.os.Environment;
import android.test.InstrumentationTestCase;

import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PureFMTextUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests {@link com.docd.purefm.file.CommandLineFile}
 *
 * @author Doctoror
 */
public final class CommandLineFileTest extends InstrumentationTestCase {

    private static final File testDir = new File(Environment.getExternalStorageDirectory(), "_test_CommandLineFile");

    private static File test1 = new File(testDir, "test1.jpg");
    private static File test2 = new File(testDir, "test2.jpg");
    private static File test3 = new File(testDir.getAbsolutePath() + "/test3dir/", "test3.jpg");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("Make sure the external storage is mounted read-write before running this test");
        }
        try {
            FileUtils.forceDelete(testDir);
        } catch (IOException e) {
            //ignored
        }
        assertTrue(testDir.mkdirs());

        // prepare a test file
        try {
            FileUtils.write(test1, "test");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file: " + e);
        }
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        // init what application inits
        final Context context = getInstrumentation().getContext();
        com.docd.purefm.Environment.init(context);
        Settings.init(context, context.getResources());
        PureFMTextUtils.init(context);

        // override settings to force our test busybox
        if (!com.docd.purefm.Environment.hasBusybox()) {
            throw new RuntimeException("install busybox on a device before running this test");
        }
        Settings.useCommandLine = true;

        testAgainstJavaIoFile();
        testFileReading();
        testFileDeletion();
        testFileCreation();
        testFileMoving();
        testFileCopying();
        testMkdir();
        testMkdirs();
    }

    private void testAgainstJavaIoFile() throws Throwable {
        CommandLineFile file1 = CommandLineFile.fromFile(test1);
        testAgainstJavaIoFile(file1, test1, true);
        assertTrue(file1.delete());
        testAgainstJavaIoFile(file1, test1, false);
        assertTrue(file1.mkdir());
        testAgainstJavaIoFile(file1, test1, true);
        try {
            file1.createNewFile();
            testAgainstJavaIoFile(file1, test1, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileUtils.write(new File(test1, "test more"), "test");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file: " + e);
        }

        Settings.useCommandLine = true;
        file1 = CommandLineFile.fromFile(test1);
        testAgainstJavaIoFile(file1, test1, true);

        try {
            FileUtils.forceDelete(file1.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileUtils.write(test1, "test");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file: " + e);
        }
    }

    private static void testAgainstJavaIoFile(final CommandLineFile genericFile,
                                              final File javaFile,
                                              final boolean testDate) throws Throwable {
        assertEquals(javaFile, genericFile.toFile());
        assertEquals(javaFile.getName(), genericFile.getName());
        assertEquals(javaFile.getAbsolutePath(), genericFile.getAbsolutePath());
        assertEquals(javaFile.exists(), genericFile.exists());
        assertEquals(javaFile.canRead(), genericFile.canRead());
        assertEquals(javaFile.canWrite(), genericFile.canWrite());
        assertEquals(javaFile.canExecute(), genericFile.canExecute());
        assertEquals(javaFile.getPath(), genericFile.getPath());
        assertEquals(javaFile.getParent(), genericFile.getParent());
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
        assertEquals(javaFile.isDirectory(), genericFile.isDirectory());
        if (genericFile.isDirectory()) {
            assertTrue(listedPathsEqual(javaFile.list(), genericFile.list()));
            assertTrue(listedFilesEqual(javaFile.listFiles(), genericFile.listFiles()));
        }

        if (testDate) {
            assertEquals(PureFMTextUtils.humanReadableDate(javaFile.lastModified(), false),
                    PureFMTextUtils.humanReadableDate(genericFile.lastModified(), true));
        }
    }

    private static boolean listedFilesEqual(final File[] javaIoFiles,
                                            final GenericFile[] genericFiles) throws Throwable {
        if (javaIoFiles == null && genericFiles == null) {
            return true;
        }
        if (javaIoFiles == null) {
            return genericFiles.length == 0;
        }
        if (genericFiles == null) {
            return javaIoFiles.length == 0;
        }
        if (javaIoFiles.length != genericFiles.length) {
            return false;
        }
        for (int i = 0; i < javaIoFiles.length; i++) {
            if (!javaIoFiles[i].getAbsolutePath().equals(genericFiles[i].getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    private static boolean listedPathsEqual(final String[] javaIoFiles,
                                            final String[] genericFiles) throws Throwable {
        if (javaIoFiles == null && genericFiles == null) {
            return true;
        }
        if (javaIoFiles == null) {
            return genericFiles.length == 0;
        }
        if (genericFiles == null) {
            return javaIoFiles.length == 0;
        }
        if (javaIoFiles.length != genericFiles.length) {
            return false;
        }
        for (int i = 0; i < javaIoFiles.length; i++) {
            if (!javaIoFiles[i].equals(genericFiles[i])) {
                return false;
            }
        }
        return true;
    }

    private void testFileReading() {
        final CommandLineFile file1 = CommandLineFile.fromFile(test1);
        assertEquals(test1.getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(test1, file1.toFile());
        assertIsNormalFile(file1);
        assertEquals(4, file1.length());
    }

    private void testFileDeletion() {
        final CommandLineFile file1 = CommandLineFile.fromFile(test1);
        file1.delete();
        assertIsEmptyFile(file1);
    }

    private void testFileCreation() {
        final CommandLineFile file1 = CommandLineFile.fromFile(test1);
        try {
            file1.createNewFile();
            assertIsNormalFile(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testFileMoving() {
        final CommandLineFile file1 = CommandLineFile.fromFile(test1);
        final String file1sum = md5sum(file1.toFile());

        CommandLineFile file2 = CommandLineFile.fromFile(test2);
        assertIsEmptyFile(file2);

        file1.move(file2);
        assertIsEmptyFile(file1);

        file2 = CommandLineFile.fromFile(test2);
        assertIsNormalFile(file2);
        assertEquals(file1sum, md5sum(file2.toFile()));
    }

    private void testFileCopying() {
        CommandLineFile file1 = CommandLineFile.fromFile(test1);
        final CommandLineFile file2 = CommandLineFile.fromFile(test2);

        file2.copy(file1);
        file1 = CommandLineFile.fromFile(test1);
        assertIsNormalFile(file1);
        assertIsNormalFile(file2);
        assertEquals(md5sum(file2.toFile()), md5sum(file1.toFile()));
    }

    private void testMkdir() {
        final CommandLineFile file1 = CommandLineFile.fromFile(test1);
        file1.delete();
        assertIsEmptyFile(file1);
        assertTrue(file1.mkdir());
        assertIsDirectory(file1);
    }

    private void testMkdirs() {
        final CommandLineFile file3 = CommandLineFile.fromFile(test3);
        assertIsEmptyFile(file3);
        assertTrue(file3.mkdirs());
        assertIsDirectory(file3);
    }

    private String md5sum(final File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return new String(Hex.encodeHex(DigestUtils.md5(fis)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) try {fis.close();} catch (Exception e) {}
        }
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
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.forceDelete(testDir);
    }
}
