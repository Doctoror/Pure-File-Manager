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

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;

import org.jetbrains.annotations.NotNull;

/**
 * @author Doctoror
 *
 * Task for renaming file
 */
public final class RenameFileTask extends OperationTask<Void, CharSequence> {

    private final GenericFile mSource;
    private final String mTargetName;

    public RenameFileTask(@NotNull final MonitoredActivity activity,
                          @NotNull final GenericFile source,
                          @NotNull final String targetName) {
        super(activity);
        this.mSource = source;
        this.mTargetName = targetName;
    }

    @NotNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_RENAME;
    }

    @Override
    protected void startService(@NotNull Void... voids) {
        OperationsService.rename(mActivity, mSource, mTargetName);
    }

    @Override
    protected void cancel() {

    }

    @Override
    protected void onPostExecute(final CharSequence result) {
        if (result != null) {
            Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
        }
    }
}
