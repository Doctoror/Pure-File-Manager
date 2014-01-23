/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Modified 2013 by Yaroslav Mytkalyk
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
 *
 */

package com.cyanogenmod.filemanager.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;

import com.docd.purefm.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;

/**
 * A helper class with useful methods for deal with AID (Android IDs).
 */
public final class AIDHelper {

    private static final String TAG = "AIDHelper";

    private static SparseArray<String> sAids;

    /**
     * Constructor of <code>AIDHelper</code>.
     */
    private AIDHelper() {
    }

    /**
     * Method that returns the Android IDs (system + application AID)
     *
     * @param context The current context
     * @param force Force the reload of the AIDs
     * @return SparseArray<String> The array of AIDs
     */
    @Nullable
    public synchronized static SparseArray<String> getAIDs(Context context, boolean force) {
        if (sAids == null || force) {
            Properties systemAIDs = null;
            try {
                // Load the default known system identifiers
                systemAIDs = new Properties();
                systemAIDs.load(context.getResources().openRawResource(R.raw.aid));
            } catch (Exception e) {
                Log.e(TAG, "Fail to load AID raw file.", e);
                return null;
            }

            // Add the default known system identifiers
            final SparseArray<String> aids = new SparseArray<String>();
            for (final Object key : systemAIDs.keySet()) {
                final String stringKey = (String) key;
                final String value = systemAIDs.getProperty(stringKey);
                final int uid = Integer.parseInt(stringKey);
                aids.put(uid, value);
            }

            // Now, retrieve all AID of installed applications
            final PackageManager pm = context.getPackageManager();
            final List<ApplicationInfo> packages =
                    pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (final ApplicationInfo info : packages) {
                final int uid = info.uid;
                if (aids.indexOfKey(uid) < 0) {
                    final String name = pm.getNameForUid(uid);
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
    @NotNull
    public static String getUserName(int id) {
        return sAids.get(id);
    }

    /**
     * Method that return AID from its user name.
     *
     * @param name The user identifier
     * @return AID The AID
     */
    @NotNull
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
