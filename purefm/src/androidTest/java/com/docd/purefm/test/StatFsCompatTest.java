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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.test.AndroidTestCase;

import com.docd.purefm.utils.StatFsCompat;

import android.support.annotation.NonNull;

/**
 * Tests {@link com.docd.purefm.utils.StatFsCompat}
 *
 * @author Doctoror
 */
public final class StatFsCompatTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED) && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            throw new RuntimeException("Make sure the external storage is mounted before running this test");
        }
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final StatFs statFs = new StatFs(path);
        final StatFsCompat statFsCompat = new StatFsCompat(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // we can't test long compat until we can't control available blocks to make sure it falls under int capacity
            testStatFs(statFs, statFsCompat);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void testStatFs(@NonNull final StatFs fs, @NonNull final StatFsCompat fsc) throws Throwable {
        assertEquals(fs.getAvailableBlocksLong(), fsc.getAvailableBlocksLong());
        assertEquals(fs.getAvailableBytes(), fsc.getAvailableBytes());
        assertEquals(fs.getBlockCountLong(), fsc.getBlockCountLong());
        assertEquals(fs.getBlockSizeLong(), fsc.getBlockSizeLong());
        assertEquals(fs.getFreeBlocksLong(), fsc.getFreeBlocksLong());
        assertEquals(fs.getFreeBytes(), fsc.getFreeBytes());
        assertEquals(fs.getTotalBytes(), fsc.getTotalBytes());
    }
}
