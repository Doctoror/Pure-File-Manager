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
import android.test.AndroidTestCase;

import com.docd.purefm.ActivityMonitor;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.TextUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class CommandLineFileTest extends AndroidTestCase {

    private static final File testDir = new File(Environment.getExternalStorageDirectory(), "test");

    private static File test1 = new File(testDir, "test1.jpg");
    private static File test2 = new File(testDir, "test2.jpg");
    private static File test3 = new File(testDir.getAbsolutePath() + "/test3dir/", "test3.jpg");

    @Override
    protected void setUp() {
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("Make sure the external storage is mounted read-write before running this test");
        }
        try {
            FileUtils.forceDelete(testDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        testDir.mkdirs();

        // init what application inits
        final Context context = this.getContext();
        ActivityMonitor.init(context);
        com.docd.purefm.Environment.init(context);
        Settings.init(context, context.getResources());
        PreviewHolder.initialize(context);
        TextUtil.init(context);

        // prepare a test file
        try {
            FileUtils.write(test1, "test");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file: " + e);
        }
    }

    @Override
    protected void runTest() {
        // override settings to force our test busybox
        if (!com.docd.purefm.Environment.hasBusybox()) {
            throw new RuntimeException("install busybox on a device before running this test");
        }
        Settings.useCommandLine = true;

        CommandLineFile file1 = (CommandLineFile) FileFactory.newFile(test1);
        assertEquals(test1.getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(test1, file1.toFile());
        assertIsNormalFile(file1);
        assertEquals(4, file1.length());

        System.out.println("going to delete");
        file1.delete();
        assertIsEmptyFile(file1);

        file1.createNewFile();
        assertIsNormalFile(file1);
        final String file1sum = md5sum(file1.toFile());

        CommandLineFile file2 = (CommandLineFile) FileFactory.newFile(test2);
        assertIsEmptyFile(file2);

        file1.move(file2);
        assertIsEmptyFile(file1);

        file2 = (CommandLineFile) FileFactory.newFile(test2);
        assertIsNormalFile(file2);
        assertEquals(file1sum, md5sum(file2.toFile()));

        file2.copy(file1);
        file1 = (CommandLineFile) FileFactory.newFile(test1);
        assertIsNormalFile(file1);
        assertIsNormalFile(file2);
        assertEquals(file1sum, md5sum(file1.toFile()));

        file1.delete();
        file2.delete();

        file1.mkdir();
        assertIsDirectory(file1);

        final CommandLineFile file3 = (CommandLineFile) FileFactory.newFile(test3);
        assertIsEmptyFile(file3);
        file3.mkdirs();
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
    protected void tearDown() {
        try {
            FileUtils.forceDelete(testDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
