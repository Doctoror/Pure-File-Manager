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

import com.docd.purefm.utils.PureFMTextUtils;

import java.util.Calendar;

/**
 * Tests {@link com.docd.purefm.utils.PureFMTextUtils}
 *
 * @author Doctoror
 */
public final class PureFMTextUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        PureFMTextUtils.init(getContext());
        testStringMonthToInt();
    }

    private void testStringMonthToInt() throws Throwable {
        assertEquals(Calendar.JANUARY, PureFMTextUtils.stringMonthToInt("Jan"));
        assertEquals(Calendar.FEBRUARY, PureFMTextUtils.stringMonthToInt("Feb"));
        assertEquals(Calendar.MARCH, PureFMTextUtils.stringMonthToInt("Mar"));
        assertEquals(Calendar.APRIL, PureFMTextUtils.stringMonthToInt("Apr"));
        assertEquals(Calendar.MAY, PureFMTextUtils.stringMonthToInt("May"));
        assertEquals(Calendar.JUNE, PureFMTextUtils.stringMonthToInt("Jun"));
        assertEquals(Calendar.JULY, PureFMTextUtils.stringMonthToInt("Jul"));
        assertEquals(Calendar.AUGUST, PureFMTextUtils.stringMonthToInt("Aug"));
        assertEquals(Calendar.SEPTEMBER, PureFMTextUtils.stringMonthToInt("Sep"));
        assertEquals(Calendar.OCTOBER, PureFMTextUtils.stringMonthToInt("Oct"));
        assertEquals(Calendar.NOVEMBER, PureFMTextUtils.stringMonthToInt("Nov"));
        assertEquals(Calendar.DECEMBER, PureFMTextUtils.stringMonthToInt("Dec"));
    }
}
