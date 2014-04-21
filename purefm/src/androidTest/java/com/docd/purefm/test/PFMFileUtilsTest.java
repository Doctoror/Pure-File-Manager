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

import android.os.Environment;
import android.test.AndroidTestCase;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.PFMFileUtils;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Tests PFMFileUtils copy and move methods
 */
public final class PFMFileUtilsTest extends AndroidTestCase {

    private static final File testDir = new File(Environment.getExternalStorageDirectory(), "_test_PFMFileUtils");

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
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        runTests(false);
        runTests(true);
    }

    private void runTests(final boolean useCommandLine) throws Throwable {
        testCopyFileToFileInTheSameDir(useCommandLine);
        testCopyFileToFileInDifferentDir(useCommandLine);
        testCopyFileToDirectory(useCommandLine);

        testMoveFileToFileInTheSameDir(useCommandLine);
        testMoveFileToFileInDifferentDir(useCommandLine);
        testMoveFileToDirectory(useCommandLine);

        testCopyEmptyDirectory(useCommandLine);
        testCopyEmptyDirectoryToDirectory(useCommandLine);

        testMoveEmptyDirectory(useCommandLine);
        testMoveEmptyDirectoryToDirectory(useCommandLine);
    }

    private void testCopyFileToFileInTheSameDir(final boolean useCommandLine) throws Throwable {
        final GenericFile fileToCopy = new JavaFile(testDir, "fileToCopy");
        assertTrue(fileToCopy.createNewFile());

        final GenericFile targetFile = new JavaFile(testDir, "targetFile");

        PFMFileUtils.copyFile(fileToCopy, targetFile, useCommandLine);

        assertTrue(fileToCopy.toFile().exists());
        assertTrue(targetFile.toFile().exists());

        assertTrue(fileToCopy.toFile().isFile());
        assertTrue(targetFile.toFile().isFile());

        assertTrue(fileToCopy.toFile().delete());
        assertTrue(targetFile.toFile().delete());
    }

    private void testMoveFileToFileInTheSameDir(final boolean useCommandLine) throws Throwable {
        final GenericFile fileToMove = new JavaFile(testDir, "fileToMove");
        assertTrue(fileToMove.createNewFile());

        final GenericFile targetFile = new JavaFile(testDir, "targetFile");

        PFMFileUtils.moveFile(fileToMove, targetFile, useCommandLine);

        assertFalse(fileToMove.exists());
        assertTrue(targetFile.exists());
        assertTrue(targetFile.toFile().isFile());

        //cleanup
        assertTrue(targetFile.toFile().delete());
    }

    private void testCopyFileToFileInDifferentDir(final boolean useCommandLine)
            throws Throwable {

        final GenericFile fileToCopy = new JavaFile(testDir, "fileToCopy");
        assertTrue(fileToCopy.createNewFile());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        final GenericFile targetFile = new JavaFile(targetDir.toFile(), "targetFile");

        PFMFileUtils.copyFile(fileToCopy, targetFile, useCommandLine);

        assertTrue(fileToCopy.exists());
        assertTrue(targetFile.exists());

        assertTrue(fileToCopy.toFile().isFile());
        assertTrue(targetFile.toFile().isFile());

        //cleanup
        assertTrue(fileToCopy.delete());
        assertTrue(targetFile.delete());
        assertTrue(targetDir.delete());
    }

    private void testMoveFileToFileInDifferentDir(final boolean useCommandLine)
            throws Throwable {

        final GenericFile fileToMove = new JavaFile(testDir, "fileToMove");
        assertTrue(fileToMove.createNewFile());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        final GenericFile targetFile = new JavaFile(targetDir.toFile(), "targetFile");

        PFMFileUtils.moveFile(fileToMove, targetFile, useCommandLine);

        assertFalse(fileToMove.toFile().exists());
        assertTrue(targetFile.toFile().exists());
        assertTrue(targetFile.toFile().isFile());

        //test if FileExistsException is thrown
        assertTrue(fileToMove.createNewFile());
        try {
            PFMFileUtils.moveFile(fileToMove, targetFile, useCommandLine);
            throw new RuntimeException("FileExistsException is not thrown");
        } catch (FileExistsException ignored) {

        }

        //cleanup
        assertTrue(fileToMove.delete());
        assertTrue(targetFile.delete());
        assertTrue(targetDir.delete());
    }

    private void testCopyFileToDirectory(final boolean useCommandLine) throws Throwable {
        final GenericFile fileToCopy = new JavaFile(testDir, "fileToCopy");
        assertTrue(fileToCopy.createNewFile());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        PFMFileUtils.copyFileToDirectory(fileToCopy, targetDir, useCommandLine);
        final GenericFile targetFile = new JavaFile(targetDir.toFile(), fileToCopy.getName());

        assertTrue(fileToCopy.exists());
        assertTrue(targetFile.exists());

        assertTrue(fileToCopy.toFile().isFile());
        assertTrue(targetFile.toFile().isFile());

        //test if IllegalArgumentException is thrown
        try {
            PFMFileUtils.copyFileToDirectory(fileToCopy, targetFile, useCommandLine);
            throw new RuntimeException("Fail: IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        //cleanup
        assertTrue(targetFile.delete());
        assertTrue(targetDir.delete());
        assertTrue(fileToCopy.delete());
    }

    private void testMoveFileToDirectory(final boolean useCommandLine)
            throws Throwable {

        final GenericFile fileToMove = new JavaFile(testDir, "fileToMove");
        assertTrue(fileToMove.createNewFile());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");

        PFMFileUtils.moveToDirectory(fileToMove, targetDir, useCommandLine, true);

        final GenericFile targetFile = new JavaFile(targetDir.toFile(), fileToMove.getName());

        assertFalse(fileToMove.exists());
        assertTrue(targetDir.exists());
        assertTrue(targetDir.isDirectory());
        assertTrue(targetFile.exists());
        assertTrue(targetFile.toFile().isFile());

        //test if IOException is thrown
        assertTrue(fileToMove.createNewFile());
        try {
            PFMFileUtils.moveToDirectory(fileToMove, targetFile, useCommandLine, false);
            throw new RuntimeException("IOException is not thrown");
        } catch (IOException ignored) {
        }

        assertTrue(targetFile.delete());
        assertTrue(targetDir.delete());

        //test if FileNotFoundException is thrown
        try {
            PFMFileUtils.moveToDirectory(fileToMove, targetFile, useCommandLine, false);
            throw new RuntimeException("FileNotFoundException is not thrown");
        } catch (FileNotFoundException ignored) {
        }

        //cleanup
        assertTrue(fileToMove.delete());
    }



    //========================= DIRECTORIES ==========================




    private void testCopyEmptyDirectory(final boolean useCommandLine)
            throws Throwable {

        final GenericFile dirToCopy = new JavaFile(testDir, "dirToCopy");
        assertTrue(dirToCopy.mkdir());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        final GenericFile copiedDir = new JavaFile(targetDir.toFile(), dirToCopy.getName());

        PFMFileUtils.copyDirectory(dirToCopy, copiedDir, useCommandLine);

        assertTrue(dirToCopy.exists());
        assertTrue(dirToCopy.isDirectory());

        assertTrue(copiedDir.exists());
        assertTrue(copiedDir.isDirectory());

        assertTrue(dirToCopy.delete());
        try {
            PFMFileUtils.copyDirectory(dirToCopy, copiedDir, useCommandLine);
            throw new RuntimeException("FileNotFoundException not thrown");
        } catch (FileNotFoundException ignored) {
        }

        assertTrue(dirToCopy.createNewFile());
        try {
            PFMFileUtils.copyDirectory(dirToCopy, copiedDir, useCommandLine);
            throw new RuntimeException("IOException not thrown");
        } catch (IOException ignored) {
        }

        try {
            PFMFileUtils.copyDirectory(copiedDir, copiedDir, useCommandLine);
            throw new RuntimeException("IOException not thrown");
        } catch (IOException ignored) {
        }

        //cleanup
        assertTrue(dirToCopy.delete());
        assertTrue(copiedDir.delete());
        assertTrue(targetDir.delete());
    }

    private void testCopyEmptyDirectoryToDirectory(final boolean useCommandLine)
            throws Throwable {

        final GenericFile dirToCopy = new JavaFile(testDir, "dirToCopy");
        assertTrue(dirToCopy.mkdir());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        PFMFileUtils.copyDirectoryToDirectory(dirToCopy, targetDir, useCommandLine);

        final GenericFile copiedDir = new JavaFile(targetDir.toFile(), dirToCopy.getName());

        assertTrue(dirToCopy.exists());
        assertTrue(dirToCopy.isDirectory());

        assertTrue(copiedDir.exists());
        assertTrue(copiedDir.isDirectory());

        assertTrue(dirToCopy.delete());
        assertTrue(copiedDir.delete());
        assertTrue(targetDir.delete());
    }

    private void testMoveEmptyDirectory(final boolean useCommandLine)
            throws Throwable {

        final GenericFile dirToMove = new JavaFile(testDir, "dirToMove");
        assertTrue(dirToMove.mkdir());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        final GenericFile movedDir = new JavaFile(targetDir.toFile(), dirToMove.getName());

        PFMFileUtils.moveDirectory(dirToMove, movedDir, useCommandLine);

        assertFalse(dirToMove.exists());
        assertTrue(movedDir.exists());
        assertTrue(movedDir.isDirectory());

        assertTrue(movedDir.delete());
        assertTrue(targetDir.delete());
    }

    private void testMoveEmptyDirectoryToDirectory(final boolean useCommandLine)
            throws Throwable {

        final GenericFile dirToMove = new JavaFile(testDir, "dirToMove");
        assertTrue(dirToMove.mkdir());

        final GenericFile targetDir = new JavaFile(testDir, "targetDir");
        assertTrue(targetDir.mkdir());

        PFMFileUtils.moveToDirectory(dirToMove, targetDir, useCommandLine, false);

        final GenericFile movedDir = new JavaFile(targetDir.toFile(), dirToMove.getName());

        assertFalse(dirToMove.exists());
        assertTrue(movedDir.exists());
        assertTrue(movedDir.isDirectory());

        assertTrue(movedDir.delete());
        assertTrue(targetDir.delete());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.forceDelete(testDir);
    }
}
