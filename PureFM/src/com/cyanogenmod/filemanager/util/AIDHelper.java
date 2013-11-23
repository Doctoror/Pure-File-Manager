/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
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

package com.cyanogenmod.filemanager.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;

import com.docd.purefm.R;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A helper class with useful methods for deal with AID (Android IDs).
 */
public final class AIDHelper {

    private static final String TAG = "AIDHelper"; //$NON-NLS-1$

    private static SparseArray<String> sAids;

    /**
     * Constructor of <code>AIDHelper</code>.
     */
    private AIDHelper() {
        super();
    }

    /**
     * Method that returns the Android IDs (system + application AID)
     *
     * @param context The current context
     * @param force Force the reload of the AIDs
     * @return SparseArray<AID> The array of {@link AID}
     */
    public synchronized static SparseArray<String> getAIDs(Context context, boolean force) {
        if (sAids == null || force) {
            Properties systemAIDs = null;
            try {
                // Load the default known system identifiers
                systemAIDs = new Properties();
                systemAIDs.load(context.getResources().openRawResource(R.raw.aid));
            } catch (Exception e) {
                Log.e(TAG, "Fail to load AID raw file.", e); //$NON-NLS-1$
                return null;
            }

            // Add the default known system identifiers
            final SparseArray<String> aids = new SparseArray<String>();
            final Iterator<Object> it = systemAIDs.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                String value = systemAIDs.getProperty(key);
                int uid = Integer.parseInt(key);
                aids.put(uid, value);
            }

            // Now, retrieve all AID of installed applications
            final PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages =
                    pm.getInstalledApplications(PackageManager.GET_META_DATA);
            int cc = packages.size();
            for (int i = 0; i < cc; i++) {
                ApplicationInfo info = packages.get(i);
                int uid = info.uid;
                if (aids.indexOfKey(uid) < 0) {
                    String name = pm.getNameForUid(uid);
                    aids.put(uid, name);
                }
            }

            // Save to cached aids
            sAids = aids;
        }

        // Return the list of AIDs found
        return sAids;
    }

    /**
     * Method that returns the AID from its identifier.
     *
     * @param id The id
     * @return AID The AID, or null if not found
     */
    public static String getUserName(int id) {
        return sAids.get(id);
    }

    /**
     * Method that return AID from its user name.
     *
     * @param name The user identifier
     * @return AID The AID
     */
    public static String getUserName(String name) {
        final int len = sAids.size();
        for (int i = 0; i < len; i++) {
            final String aid = sAids.valueAt(i);
            if (aid.equals(name)) {
                return aid;
            }
        }
        return "";
    }

}
