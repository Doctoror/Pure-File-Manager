package com.docd.purefm.test;

import android.test.AndroidTestCase;

import com.docd.purefm.utils.PureFMFileUtils;

import java.math.BigInteger;

public final class PureFMFileUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        assertEquals("0 B", PureFMFileUtils.byteCountToDisplaySize(BigInteger.ZERO));
        assertEquals("1 B", PureFMFileUtils.byteCountToDisplaySize(BigInteger.ONE));
        assertEquals("10 B", PureFMFileUtils.byteCountToDisplaySize(BigInteger.TEN));
        assertEquals("256 B", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(256)));
        assertEquals("1023 B", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1023)));
        assertEquals("1 KiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1024)));
        assertEquals("10 KiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(10240)));
        assertEquals("1023 KiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1047552)));
        assertEquals("1 MiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1048576)));
        assertEquals("666 MiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 6.98351616e8)));
        assertEquals("1023 MiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 1.072693248e9)));
        assertEquals("1 GiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1073741824)));
        assertEquals("1024 GiB", PureFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 1.099511627776e12)));
    }
}
