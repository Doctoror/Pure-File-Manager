package com.docd.purefm.settings;

import java.io.File;

public final class Environment {

    public static boolean hasBusybox;
    public static boolean hasRoot;
    
    public static void init() {
        hasBusybox = isUtilAvailable("busybox");
        hasRoot = isUtilAvailable("su");
    }
    
    public static boolean isUtilAvailable(String utilname) {
        final String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/" };
        
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
    
    private Environment() {}
}
