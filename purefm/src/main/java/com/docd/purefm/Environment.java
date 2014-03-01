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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class Environment {
    
    private Environment() {}

    private static final ActivityMonitorListener activityMonitorListener = new ActivityMonitorListener();
    
    public static final File rootDirectory = File.listRoots()[0];
    public static final File externalStorageDirectory = android.os.Environment.getExternalStorageDirectory();
    public static final File androidRootDirectory = android.os.Environment.getRootDirectory();

    private static final Set<File> usbStorageDirectories = new TreeSet<File>();
    private static File secondaryStorageDirectory;
    private static boolean isExternalStorageMounted;
    
    private static Context context;
    public static boolean hasRoot;
    
    public static String busybox;
    
    public static void init(final Context context1) {
        context = context1;
        busybox = getUtilPath("busybox");
        if (busybox == null) {
            busybox = getUtilPath("busybox-ba");
        }
        hasRoot = isUtilAvailable("su");
        updateExternalStorageState();
        ActivityMonitor.addOnActivitiesOpenedListener(activityMonitorListener);
    }

    public static boolean hasBusybox() {
        return busybox != null;
    }

    @Nullable
    public static String getBusybox() {
        return busybox;
    }
    
    public static boolean isExternalStorageMounted() {
        return isExternalStorageMounted;
    }

    @Nullable
    public static File getSecondaryStorageDirectory() {
        return secondaryStorageDirectory;
    }

    @NotNull
    public static Set<File> getUsbStorageDirectories() {
        return usbStorageDirectories;
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

    public static boolean needsRemount(final @NotNull String path) {
        if (path.equals(rootDirectory.getAbsolutePath())) {
            return true;
        }
        if (path.startsWith(androidRootDirectory.getAbsolutePath())) {
            return true;
        }
        if (path.startsWith(externalStorageDirectory.getAbsolutePath())) {
            return false;
        }
        if (secondaryStorageDirectory != null) {
            if (path.startsWith(secondaryStorageDirectory.getAbsolutePath())) {
                return false;
            }
        }
        for (final File file : usbStorageDirectories) {
            if (path.startsWith(file.getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBusyboxUtilAvailable(final String util) {
        if (busybox == null) {
            return false;
        }

        final List<String> result = CommandLine.executeForResult(ShellHolder.getShell(),
                new CommandListBusyboxApplets());
        if (result != null) {
            for (final String resultLine : result) {
                if (resultLine.equals(util)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static void resolveStorages() {
        File externalStorage = android.os.Environment.getExternalStorageDirectory();
        File externalStorageParent;
        try {
            externalStorage = externalStorage.getCanonicalFile();
        } catch (IOException e) {
            //ignored
        }
        externalStorageParent = externalStorage.getParentFile();
        if (externalStorageParent != null) {
            try {
                externalStorageParent = externalStorageParent.getCanonicalFile();
            } catch (IOException e) {
                //ignored
            }
        }
        if (externalStorageParent != null) {
            final File[] files = externalStorageParent.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (!file.equals(externalStorage) && file.canRead() && file.canWrite() && file.canExecute()) {
                        final String fileName = file.getName();
                        if (StringUtils.containsIgnoreCase(fileName, "ext")) {
                            secondaryStorageDirectory = file;
                        } else if (StringUtils.containsIgnoreCase(fileName, "usb")) {
                            usbStorageDirectories.add(file);
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isExternalMounted() {
        final String state = android.os.Environment.getExternalStorageState();
        return state.equals(android.os.Environment.MEDIA_MOUNTED) ||
                state.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY);
    }
        
    // ============== STORAGE LISTENER ===============
    
    static void updateExternalStorageState() {
        isExternalStorageMounted = isExternalMounted();
        if (isExternalStorageMounted && secondaryStorageDirectory == null) {
            resolveStorages();
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
                    context.registerReceiver(externalStorageStateReceiver, ExternalStorageStateReceiver.intentFilter);
                    this.isRegistered = true;
                }
            }
        }

        @Override
        public void onActivitiesStopped() {
            synchronized (LOCK) {
                if (this.isRegistered) {
                    context.unregisterReceiver(externalStorageStateReceiver);
                    this.isRegistered = false;
                }
            }
        }
    }
}
