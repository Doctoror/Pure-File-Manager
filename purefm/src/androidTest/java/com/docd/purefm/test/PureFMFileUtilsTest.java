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

import com.docd.purefm.utils.PFMFileUtils;

import java.math.BigInteger;

/**
 * Tests {@link com.docd.purefm.utils.PFMFileUtils}
 *
 * @author Doctoror
 */
public final class PureFMFileUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        assertEquals("0 B", PFMFileUtils.byteCountToDisplaySize(BigInteger.ZERO));
        assertEquals("1 B", PFMFileUtils.byteCountToDisplaySize(BigInteger.ONE));
        assertEquals("10 B", PFMFileUtils.byteCountToDisplaySize(BigInteger.TEN));
        assertEquals("256 B", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(256)));
        assertEquals("1023 B", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1023)));
        assertEquals("1 KiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1024)));
        assertEquals("10 KiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(10240)));
        assertEquals("1023 KiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1047552)));
        assertEquals("1 MiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1048576)));
        assertEquals("666 MiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 6.98351616e8)));
        assertEquals("1023 MiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 1.072693248e9)));
        assertEquals("1 GiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf(1073741824)));
        assertEquals("1024 GiB", PFMFileUtils.byteCountToDisplaySize(BigInteger.valueOf((long) 1.099511627776e12)));
    }
}
