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

import com.docd.purefm.utils.PureFMFileUtils;

import java.math.BigInteger;

/**
 * Tests {@link com.docd.purefm.utils.PureFMFileUtils}
 *
 * @author Doctoror
 */
public final class PureFMFileUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
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
