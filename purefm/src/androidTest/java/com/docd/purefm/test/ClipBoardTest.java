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

import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.ClipBoard;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Tests {@link com.docd.purefm.utils.ClipBoard}
 *
 * @author Doctoror
 */
public final class ClipBoardTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        Settings.useCommandLine = false;
        final GenericFile[] files = new GenericFile[] {
                FileFactory.newFile("/one"),
                FileFactory.newFile("/two"),
                FileFactory.newFile("/three")
        };
        testCopy(files);
        testMove(files);
    }

    private void testCopy(@NonNull final GenericFile[] files) throws Throwable {
        assertNull(ClipBoard.getClipBoardContents());
        ClipBoard.cutCopy(files);
        assertTrue(Arrays.equals(files, ClipBoard.getClipBoardContents()));
        assertEquals(false, ClipBoard.isMove());
        assertEquals(false, ClipBoard.isEmpty());
        ClipBoard.clear();
        assertEquals(true, ClipBoard.isEmpty());
        assertNull(ClipBoard.getClipBoardContents());
    }

    private void testMove(@NonNull final GenericFile[] files) throws Throwable {
        assertNull(ClipBoard.getClipBoardContents());
        ClipBoard.cutMove(files);
        assertTrue(Arrays.equals(files, ClipBoard.getClipBoardContents()));
        assertEquals(true, ClipBoard.isMove());
        assertEquals(false, ClipBoard.isEmpty());
        ClipBoard.clear();
        assertEquals(true, ClipBoard.isEmpty());
        assertNull(ClipBoard.getClipBoardContents());
    }
}
