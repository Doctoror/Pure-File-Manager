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
package com.docd.purefm.utils;

import android.util.Log;

import com.docd.purefm.file.GenericFile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Holds references to copied or moved to clipboard files
 */
public final class ClipBoard {

    private static final Object LOCK = new Object();

    /**
     * Files in clip board
     */
    private static GenericFile[] sClipBoard;
    
    /**
     * True if move, false if cutCopy
     */
    private static boolean sIsMove;

    /**
     * If true, clipBoard can't be modified
     */
    private static volatile boolean sIsLocked;
    
    private ClipBoard() {}
    
    public static synchronized boolean cutCopy(@NonNull final GenericFile[] files) {
        synchronized (LOCK) {
            if (!sIsLocked) {
                sIsMove = false;
                sClipBoard = files;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to cutCopy but clipboard is locked");
                return false;
            }
        }
    }
    
    public static synchronized boolean cutMove(@NonNull final GenericFile[] files) {
        synchronized (LOCK) {
            if (!sIsLocked) {
                sIsMove = true;
                sClipBoard = files;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to cutMove clipboard is locked");
                return false;
            }
        }
    }

    public static void lock() {
        synchronized (LOCK) {
            sIsLocked = true;
        }
    }

    public static void unlock() {
        synchronized (LOCK) {
            sIsLocked = false;
        }
    }

    @Nullable
    public static GenericFile[] getClipBoardContents() {
        synchronized (LOCK) {
            return sClipBoard;
        }
    }
    
    public static boolean isEmpty() {
        synchronized (LOCK) {
            return sClipBoard == null;
        }
    }
    
    public static boolean isMove() {
        synchronized (LOCK) {
            return sIsMove;
        }
    }
    
    public static boolean clear() {
        synchronized (LOCK) {
            if (!sIsLocked) {
                sClipBoard = null;
                sIsMove = false;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to clear, but ClipBoard is locked");
                return false;
            }
        }
    }
    
}
