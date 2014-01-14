package com.docd.purefm.utils;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.Nullable;

public final class ClipBoard {

    /**
     * Files in clip board
     */
    private static GenericFile[] clipBoard;
    
    /**
     * True if cut, false if copy
     */
    private static boolean isCut;
    
    private ClipBoard() {}
    
    public static void copy(GenericFile[] files) {
        isCut = false;
        clipBoard = files;
    }
    
    public static void cut(GenericFile[] files) {
        isCut = true;
        clipBoard = files;
    }

    @Nullable
    public static GenericFile[] getClipBoardContents() {
        return clipBoard;
    }
    
    public static boolean isEmpty() {
        return clipBoard == null;
    }
    
    public static boolean isCut() {
        return isCut;
    }
    
    public static void clear() {
        clipBoard = null;
        isCut = false;
    }
    
}
