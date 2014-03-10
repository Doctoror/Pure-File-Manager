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
package com.docd.purefm.file;

import android.os.Looper;

import com.docd.purefm.Environment;
import com.docd.purefm.settings.Settings;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class FileFactory {
    private FileFactory() {}


    @NotNull
    public static GenericFile newFile(String path) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //throw new RuntimeException("Wrong thread");
        }
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(new File(path)) :
                new JavaFile(path);
    }

    @NotNull
    public static GenericFile newFile(File path) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Wrong thread");
        }
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(path) :
                new JavaFile(path);
    }

    @NotNull
    public static GenericFile newFile(File file, String name) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Wrong thread");
        }
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(new File(file, name)) :
                new JavaFile(file, name);
    }
}
