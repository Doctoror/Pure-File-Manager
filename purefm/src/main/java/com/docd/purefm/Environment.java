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
package com.docd.purefm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.docd.purefm.commandline.CommandListBusyboxApplets;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.utils.StorageHelper;
import com.stericson.RootTools.execution.Shell;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class Environment {
    
    private Environment() {}

    @NonNull
    private static final ActivityMonitorListener sActivityMonitorListener = new ActivityMonitorListener();

    @NonNull
    public static final File sRootDirectory = File.listRoots()[0];

    @NonNull
    public static final File sAndroidRootDirectory = android.os.Environment.getRootDirectory();

    private static boolean sExternalStorageMounted;
    
    private static Context sContext;
    public static boolean sHasRoot;
    
    public static String sBusybox;

    private static List<StorageHelper.Volume> sVolumes;
    private static List<StorageHelper.StorageVolume> sStorages;
    
    public static void init(final Context context) {
        sContext = context;
        sBusybox = getUtilPath("busybox");
        if (sBusybox == null) {
            sBusybox = getUtilPath("busybox-ba");
        }
        sHasRoot = isUtilAvailable("su");
        updateExternalStorageState();
        ActivityMonitor.addOnActivitiesOpenedListener(sActivityMonitorListener);
    }

    @NonNull
    public static List<StorageHelper.Volume> getVolumes() {
        if (sVolumes == null) {
            throw new IllegalStateException("Environment was not initialized");
        }
        return sVolumes;
    }

    @NonNull
    public static List<StorageHelper.StorageVolume> getStorageVolumes() {
        if (sStorages == null) {
            throw new IllegalStateException("Environment was not initialized");
        }
        return sStorages;
    }

    public static boolean hasBusybox() {
        return sBusybox != null;
    }

    @Nullable
    public static String getBusybox() {
        return sBusybox;
    }
    
    public static boolean isExternalStorageMounted() {
        return sExternalStorageMounted;
    }
    
    @SuppressLint("SdCardPath")
    @Nullable
    public static String getUtilPath(String utilname) {
        final String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/", "/data/data/burrows.apps.busybox/app_busybox/", "/data/data/burrows.apps.busybox.paid/app_busybox/"};
        
        for (int i = 0; i < places.length; i++) {
            final File[] files = new File(places[i]).listFiles();
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    final File current = files[j];
                    if (current.getName().equals(utilname)) {
                        return current.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
    
    @SuppressLint("SdCardPath")
    public static boolean isUtilAvailable(String utilname) {
        final String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/", "/data/data/burrows.apps.busybox/app_busybox/", "/data/data/burrows.apps.busybox.paid/app_busybox/"};
        
        for (int i = 0; i < places.length; i++) {
            final String[] files = new File(places[i]).list();
            if (files != null) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].equals(utilname)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean needsRemount(final @NonNull String path) {
        if (path.equals(sRootDirectory.getAbsolutePath())) {
            return true;
        }
        if (path.startsWith(sAndroidRootDirectory.getAbsolutePath())) {
            return true;
        }
        for (final StorageHelper.Volume volume : sVolumes) {
            if (path.startsWith(volume.file.getAbsolutePath())) {
                return volume.isReadOnly();
            }
            try {
                if (path.startsWith(volume.file.getCanonicalPath())) {
                    return volume.isReadOnly();
                }
            } catch (IOException e) {
                //ignored
            }
        }
        return true;
    }

    public static boolean isBusyboxUtilAvailable(final String util) {
        if (sBusybox == null) {
            return false;
        }

        final Shell shell = ShellHolder.getShell();
        if (shell != null) {
            final List<String> result = CommandLine.executeForResult(shell,
                    new CommandListBusyboxApplets());
            if (result != null) {
                for (final String resultLine : result) {
                    if (resultLine.equals(util)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean isExternalMounted() {
        final String state = android.os.Environment.getExternalStorageState();
        return state.equals(android.os.Environment.MEDIA_MOUNTED) ||
                state.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY);
    }
        
    // ============== STORAGE LISTENER ===============
    
    static void updateExternalStorageState() {
        final boolean isExternalStorageMounted = isExternalMounted();
        if (sVolumes == null && isExternalStorageMounted || sExternalStorageMounted != isExternalStorageMounted) {
            sExternalStorageMounted = isExternalStorageMounted;
            sVolumes = StorageHelper.getAllDevices();
            sStorages = StorageHelper.getStorageVolumes(sVolumes);
            // longest names should be first to detect mount point properly
            Collections.sort(sVolumes, StorageHelper.VOLUME_PATH_LENGTH_COMPARATOR);
        }
    }
    
    static final ExternalStorageStateReceiver externalStorageStateReceiver =
            new ExternalStorageStateReceiver();
    
    static final class ExternalStorageStateReceiver extends BroadcastReceiver {
        
        static final IntentFilter intentFilter = new IntentFilter();
        
        static {
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            updateExternalStorageState();
        }
    }
    
    // =============== ACTIVITY MONITOR ===============

    private static final class ActivityMonitorListener implements ActivityMonitor.OnActivitiesOpenedListener {

        private static final Object LOCK = new Object();
        private volatile boolean isRegistered;

        @Override
        public void onActivitiesCreated() {

        }

        @Override
        public void onActivitiesDestroyed() {

        }

        @Override
        public void onActivitiesStarted() {
            synchronized (LOCK) {
                if (!this.isRegistered) {
                    sContext.registerReceiver(externalStorageStateReceiver, ExternalStorageStateReceiver.intentFilter);
                    this.isRegistered = true;
                }
            }
        }

        @Override
        public void onActivitiesStopped() {
            synchronized (LOCK) {
                if (this.isRegistered) {
                    sContext.unregisterReceiver(externalStorageStateReceiver);
                    this.isRegistered = false;
                }
            }
        }
    }
}
