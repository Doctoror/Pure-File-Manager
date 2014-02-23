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
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.utils.PureFMTextUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Tests {@link com.docd.purefm.file.FileFactory}
 *
 * @author Doctoror
 */
public final class FileFactoryTest extends AndroidTestCase {

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
            //ignore
        }
        assertTrue(testDir.mkdirs());
    }

    @Override
    protected void runTest() {

        com.docd.purefm.Environment.init(getContext());

        if (!com.docd.purefm.Environment.hasBusybox()) {
            throw new RuntimeException("install busybox on a device before running this test");
        }
        Settings.useCommandLine = true;

        final GenericFile file1 = FileFactory.newFile(test1);
        assertTrue(file1 instanceof CommandLineFile);
        assertEquals(test1, file1.toFile());

        Settings.useCommandLine = false;
        final GenericFile file2 = FileFactory.newFile(test1);
        assertTrue(file2 instanceof JavaFile);
        assertEquals(test1, file2.toFile());
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
