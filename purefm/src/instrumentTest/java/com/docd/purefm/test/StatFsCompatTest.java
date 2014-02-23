package com.docd.purefm.test;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.test.AndroidTestCase;

import com.docd.purefm.utils.StatFsCompat;

import org.jetbrains.annotations.NotNull;

public final class StatFsCompatTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        final String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED) && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            throw new RuntimeException("Make sure the external storage is mounted before running this test");
        }
    }

    @Override
    protected void runTest() throws Throwable {
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final StatFs statFs = new StatFs(path);
        final StatFsCompat statFsCompat = new StatFsCompat(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // we can't test long compat until we can't control available blocks to make sure it falls under int capacity
            testStatFs(statFs, statFsCompat);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void testStatFs(@NotNull final StatFs fs, @NotNull final StatFsCompat fsc) {
        assertEquals(fs.getAvailableBlocksLong(), fsc.getAvailableBlocksLong());
        assertEquals(fs.getAvailableBytes(), fsc.getAvailableBytes());
        assertEquals(fs.getBlockCountLong(), fsc.getBlockCountLong());
        assertEquals(fs.getBlockSizeLong(), fsc.getBlockSizeLong());
        assertEquals(fs.getFreeBlocksLong(), fsc.getFreeBlocksLong());
        assertEquals(fs.getFreeBytes(), fsc.getFreeBytes());
        assertEquals(fs.getTotalBytes(), fsc.getTotalBytes());
    }
}
