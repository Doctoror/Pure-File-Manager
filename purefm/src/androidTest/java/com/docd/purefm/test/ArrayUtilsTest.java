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

import com.docd.purefm.utils.ArrayUtils;

/**
 * Tests {@link com.docd.purefm.utils.ArrayUtils}
 *
 * @author Doctoror
 */
public final class ArrayUtilsTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
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
