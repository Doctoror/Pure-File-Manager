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

import android.test.AndroidTestCase;

import com.docd.purefm.Environment;
import com.docd.purefm.utils.StorageHelper;

import java.io.File;

/**
 * Tests {@link com.docd.purefm.Environment}
 *
 * @author Doctoror
 */
public final class EnvironmentTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        Environment.init(getContext());
        assertEquals(android.os.Environment.getRootDirectory(),
                Environment.androidRootDirectory);
        assertEquals(File.listRoots()[0], Environment.rootDirectory);
        final File primaryStorage = android.os.Environment.getExternalStorageDirectory();
        if (primaryStorage != null) {
            boolean primaryStorageFound = false;
            for (final StorageHelper.StorageVolume v : Environment.getStorageVolumes()) {
                if (v.file.equals(primaryStorage)) {
                    primaryStorageFound = true;
                    break;
                }
            }
            assertTrue(primaryStorageFound);
        }
    }
}
