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

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.operations.OperationsService;
import com.docd.purefm.ui.activities.MonitoredActivity;

import org.jetbrains.annotations.NotNull;

/**
 * Task for creating directory
 *
 * @author Doctoror
 */
public final class CreateDirectoryTask extends OperationTask<GenericFile, Boolean> {

    public CreateDirectoryTask(@NotNull MonitoredActivity activity) {
        super(activity);
    }

    @NotNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_CREATE_DIRECTORY;
    }

    @Override
    protected void startService(@NotNull GenericFile... files) {
        for (final GenericFile file : files) {
            OperationsService.createDirectory(mActivity, file);
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        super.onPostExecute(result);
        if (!result) {
            Toast.makeText(mActivity, R.string.could_not_create_dir,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void cancel() {
    }
}
