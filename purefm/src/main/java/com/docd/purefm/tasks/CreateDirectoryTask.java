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

import android.app.Activity;
import android.widget.Toast;

import com.docd.purefm.operations.OperationsService;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Task for creating directory
 *
 * @author Doctoror
 */
public final class CreateDirectoryTask extends OperationTask<String, CharSequence> {

    @NonNull
    private final File mParent;

    public CreateDirectoryTask(@NonNull final Activity activity,
                               @NonNull final File parent) {
        super(activity);
        mParent = parent;
    }

    @NonNull
    @Override
    protected String getServiceAction() {
        return OperationsService.ACTION_CREATE_DIRECTORY;
    }

    @Override
    protected void startService(@NonNull String... fileNames) {
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
