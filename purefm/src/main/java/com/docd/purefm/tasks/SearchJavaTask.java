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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang3.StringUtils;
import android.support.annotation.NonNull;

final class SearchJavaTask extends AbstractSearchTask {

    protected SearchJavaTask(@NonNull final GenericFile startDirectory,
                             @NonNull final SearchTaskListener listener) {
        super(startDirectory, listener);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            new SearchDirectoryWalker(params[0]).walk(new File(params[1]));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final class SearchFileFilter implements FileFilter {

        private final String mToFind;

        SearchFileFilter(@NonNull final String toFind) {
            mToFind = toFind;
        }

        @Override
        public boolean accept(final File file) {
            return file.isDirectory() || StringUtils.containsIgnoreCase(file.getName(), mToFind);
        }
    }

    private final class SearchDirectoryWalker extends DirectoryWalker<File> {

        SearchDirectoryWalker(@NonNull final String toFind) {
            super(new SearchFileFilter(toFind), -1);
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results)
                throws IOException {
            publishProgress(new JavaFile(file));
        }

        @Override
        protected boolean handleIsCancelled(File file, int depth, Collection<File> results)
                throws IOException {
            return isCancelled();
        }

        void walk(@NonNull final File directory) throws IOException {
            walk(directory, null);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getDeniedLocations() {
        return Collections.EMPTY_LIST;
    }
}
