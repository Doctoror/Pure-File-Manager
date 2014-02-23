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

import android.test.ApplicationTestCase;

import com.docd.purefm.PureFM;
import com.stericson.RootTools.RootTools;

/**
 * Tests {@link com.docd.purefm.PureFM} Application
 *
 * @author Doctoror
 */
public final class PureFMApplicationTest extends ApplicationTestCase<PureFM> {

    public PureFMApplicationTest(Class<PureFM> applicationClass) {
        super(PureFM.class);
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        assertTrue(PureFM.THEME_ID_LIGHT != PureFM.THEME_ID_DARK);
        assertFalse(RootTools.handlerEnabled);
    }
}
