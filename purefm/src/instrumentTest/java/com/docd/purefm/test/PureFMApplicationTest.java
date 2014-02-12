package com.docd.purefm.test;

import android.test.ApplicationTestCase;

import com.docd.purefm.PureFM;
import com.stericson.RootTools.RootTools;

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
