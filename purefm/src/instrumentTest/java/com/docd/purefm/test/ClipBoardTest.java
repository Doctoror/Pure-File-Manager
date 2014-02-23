package com.docd.purefm.test;

import android.test.AndroidTestCase;

import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.ClipBoard;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ClipBoardTest extends AndroidTestCase {


    @Override
    protected void runTest() throws Throwable {
        Settings.useCommandLine = false;
        final GenericFile[] files = new GenericFile[] {
                FileFactory.newFile("/one"),
                FileFactory.newFile("/two"),
                FileFactory.newFile("/three")
        };
        testCopy(files);
        testMove(files);
    }

    private void testCopy(@NotNull final GenericFile[] files) {
        assertNull(ClipBoard.getClipBoardContents());
        ClipBoard.cutCopy(files);
        assertTrue(Arrays.equals(files, ClipBoard.getClipBoardContents()));
        assertEquals(false, ClipBoard.isMove());
        assertEquals(false, ClipBoard.isEmpty());
        ClipBoard.clear();
        assertEquals(true, ClipBoard.isEmpty());
        assertNull(ClipBoard.getClipBoardContents());
    }

    private void testMove(@NotNull final GenericFile[] files) {
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
