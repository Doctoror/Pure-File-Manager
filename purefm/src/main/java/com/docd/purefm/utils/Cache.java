package com.docd.purefm.utils;

import java.util.HashMap;
import java.util.Map;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.Nullable;

public final class Cache {

    private Cache() {}
    
    private static Map<String, GenericFile> cache;
    
    static {
        cache = new HashMap<String, GenericFile>();
    }
    
    public static void addTo(GenericFile file) {
        cache.put(file.getAbsolutePath(), file);
    }

    @Nullable
    public static GenericFile get(String key) {
        return cache.get(key);
    }
    
    public static void remove(String key) {
        cache.remove(key);
    }
    
    public static void clear() {
        cache.clear();
    }
}
