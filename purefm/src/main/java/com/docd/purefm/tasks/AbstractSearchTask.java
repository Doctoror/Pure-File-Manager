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

import android.content.Context;
import android.os.AsyncTask;

import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;

import android.support.annotation.NonNull;

import java.util.List;

public abstract class AbstractSearchTask extends AsyncTask<String, GenericFile, Void> {

    public static AbstractSearchTask create(@NonNull final Context context,
                                            @NonNull final GenericFile startDirectory,
                                            @NonNull final SearchTaskListener listener) {
        AbstractSearchTask task = null;
        final Settings settings = Settings.getInstance(context);
        if (settings.useCommandLine()) {
            if (ShellHolder.getInstance().hasShell()) {
                task = new SearchCommandLineTask(settings, startDirectory, listener);
            }
        }
        if (task == null) {
            task = new SearchJavaTask(startDirectory, listener);
        }
        return task;
    }

    public interface SearchTaskListener {
        void onPreExecute(@NonNull AbstractSearchTask task);
        void onCancelled(@NonNull AbstractSearchTask task);
        void onProgressUpdate(@NonNull AbstractSearchTask task, GenericFile... files);
        void onPostExecute(@NonNull AbstractSearchTask task);
    }


    @NonNull
    protected final GenericFile mStartDirectory;

    @NonNull
    private final SearchTaskListener mListener;

    protected AbstractSearchTask(@NonNull final GenericFile startDirectory,
                                 @NonNull final SearchTaskListener listener) {
        mStartDirectory = startDirectory;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onPreExecute(this);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mListener.onPostExecute(this);
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        mListener.onCancelled(this);
    }

    @Override
    protected void onProgressUpdate(GenericFile... values) {
        super.onProgressUpdate(values);
        mListener.onProgressUpdate(this, values);
    }

    @NonNull
    public abstract List<String> getDeniedLocations();
}
