package com.docd.purefm.test;

import android.test.AndroidTestCase;

import com.docd.purefm.utils.PureFMTextUtils;

import java.util.Calendar;


public class PureFMTextUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        PureFMTextUtils.init(getContext());
        testStringMonthToInt();
    }

    private void testStringMonthToInt() {
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
