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

import com.docd.purefm.utils.PFMTextUtils;

import java.util.Calendar;

/**
 * Tests {@link com.docd.purefm.utils.PFMTextUtils}
 *
 * @author Doctoror
 */
public final class PureFMTextUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        PFMTextUtils.init(getContext());
        testStringMonthToInt();
    }

    private void testStringMonthToInt() throws Throwable {
        assertEquals(Calendar.JANUARY, PFMTextUtils.stringMonthToInt("Jan"));
        assertEquals(Calendar.FEBRUARY, PFMTextUtils.stringMonthToInt("Feb"));
        assertEquals(Calendar.MARCH, PFMTextUtils.stringMonthToInt("Mar"));
        assertEquals(Calendar.APRIL, PFMTextUtils.stringMonthToInt("Apr"));
        assertEquals(Calendar.MAY, PFMTextUtils.stringMonthToInt("May"));
        assertEquals(Calendar.JUNE, PFMTextUtils.stringMonthToInt("Jun"));
        assertEquals(Calendar.JULY, PFMTextUtils.stringMonthToInt("Jul"));
        assertEquals(Calendar.AUGUST, PFMTextUtils.stringMonthToInt("Aug"));
        assertEquals(Calendar.SEPTEMBER, PFMTextUtils.stringMonthToInt("Sep"));
        assertEquals(Calendar.OCTOBER, PFMTextUtils.stringMonthToInt("Oct"));
        assertEquals(Calendar.NOVEMBER, PFMTextUtils.stringMonthToInt("Nov"));
        assertEquals(Calendar.DECEMBER, PFMTextUtils.stringMonthToInt("Dec"));
    }
}
