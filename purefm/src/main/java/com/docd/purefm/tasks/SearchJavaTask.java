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
package com.docd.purefm.tasks;

import android.os.AsyncTask;

import java.io.File;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;

import org.apache.commons.lang3.StringUtils;

public class SearchJavaTask extends AsyncTask<String, GenericFile, Void> {

    @Override
    protected Void doInBackground(String... params) {
        try {
            this.search(new File(params[1]), params[0]);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void search(final File location, final String toFind) {
        final File[] files = location.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (this.isCancelled()) {
                    return;
                }
                if (file.isDirectory()) {
                    search(file, toFind);
                }
                else if (StringUtils.containsIgnoreCase(file.getName(), toFind)) {
                    this.publishProgress(new JavaFile(file));
                }
            }
        }
    }
    
}
