package com.docd.purefm.test;


import android.test.AndroidTestCase;

import com.docd.purefm.utils.ArrayUtils;

public final class ArrayUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        testEmpty();
        test();
    }

    private void testEmpty() {
        ArrayUtils.copyArrayAndCast(new CharSequence[0], new String[0]);
    }

    private void test() {
        final String[] test = new String[] {
                "abc", "DeF", "123", "~!@#$%^&*()_+"
        };
        final CharSequence[] test1 = new CharSequence[test.length];
        ArrayUtils.copyArrayAndCast(test, test1);
        for (int i = 0; i < test.length; i++) {
            assertEquals(test[i], test1[i].toString());
        }
    }
}
