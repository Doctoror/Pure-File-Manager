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

import android.widget.Toast;

import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Task for creating directory
 *
 * @author Doctoror
 */
public final class CreateDirectoryTask extends OperationTask<String, CharSequence> {

    @NotNull
    private final File mParent;

    public CreateDirectoryTask(@NotNull final MonitoredActivity activity,
                               @NotNull final File parent) {
        super(activity);
        mParent = parent;
    }

    @NotNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_CREATE_DIRECTORY;
    }

    @Override
    protected void startService(@NotNull String... fileNames) {
        for (final String fileName : fileNames) {
            OperationsService.createDirectory(mActivity, mParent, fileName);
        }
    }

    @Override
    protected void onPostExecute(final CharSequence result) {
        super.onPostExecute(result);
        if (result != null) {
            Toast.makeText(mActivity, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void cancel() {
    }
}
