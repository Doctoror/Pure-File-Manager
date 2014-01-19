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

import org.jetbrains.annotations.Nullable;

/**
 * Holds references to copied or moved to clipboard files
 */
public final class ClipBoard {

    private static final Object LOCK = new Object();

    /**
     * Files in clip board
     */
    private static GenericFile[] clipBoard;
    
    /**
     * True if move, false if cutCopy
     */
    private static boolean isMove;

    /**
     * If true, clipBoard can't be modified
     */
    private static volatile boolean isLocked;
    
    private ClipBoard() {}
    
    public static synchronized boolean cutCopy(GenericFile[] files) {
        synchronized (LOCK) {
            if (!isLocked) {
                isMove = false;
                clipBoard = files;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to cutCopy but clipboard is locked");
                return false;
            }
        }
    }
    
    public static synchronized boolean cutMove(GenericFile[] files) {
        synchronized (LOCK) {
            if (!isLocked) {
                isMove = true;
                clipBoard = files;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to cutMove clipboard is locked");
                return false;
            }
        }
    }

    public static void lock() {
        synchronized (LOCK) {
            isLocked = true;
        }
    }

    public static void unlock() {
        synchronized (LOCK) {
            isLocked = false;
        }
    }

    @Nullable
    public static GenericFile[] getClipBoardContents() {
        synchronized (LOCK) {
            return clipBoard;
        }
    }
    
    public static boolean isEmpty() {
        synchronized (LOCK) {
            return clipBoard == null;
        }
    }
    
    public static boolean isMove() {
        synchronized (LOCK) {
            return isMove;
        }
    }
    
    public static boolean clear() {
        synchronized (LOCK) {
            if (!isLocked) {
                clipBoard = null;
                isMove = false;
                return true;
            } else {
                Log.w("ClipBoard", "Trying to clear, but ClipBoard is locked");
                return false;
            }
        }
    }
    
}
