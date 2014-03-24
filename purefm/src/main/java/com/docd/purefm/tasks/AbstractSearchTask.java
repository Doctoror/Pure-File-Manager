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

import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractSearchTask extends AsyncTask<String, GenericFile, Void> {

    public static AbstractSearchTask create(final SearchTaskListener listener) {
        AbstractSearchTask task = null;
        if (Settings.useCommandLine) {
            final Shell shell = ShellHolder.getShell();
            if (shell != null) {
                task = new SearchCommandLineTask(shell);
            }
        }
        if (task == null) {
            task = new SearchJavaTask();
        }
        task.setSearchTaskListener(listener);
        return task;
    }

    public interface SearchTaskListener {
        void onPreExecute(@NotNull AbstractSearchTask task);
        void onCancelled(@NotNull AbstractSearchTask task);
        void onProgressUpdate(@NotNull AbstractSearchTask task, GenericFile... files);
        void onPostExecute(@NotNull AbstractSearchTask task);
    }

    private SearchTaskListener mListener;

    public void setSearchTaskListener(@Nullable final SearchTaskListener l) {
        mListener = l;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onPreExecute(this);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null) {
            mListener.onPostExecute(this);
        }
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        if (mListener != null) {
            mListener.onCancelled(this);
        }
    }

    @Override
    protected void onProgressUpdate(GenericFile... values) {
        super.onProgressUpdate(values);
        if (mListener != null) {
            mListener.onProgressUpdate(this, values);
        }
    }

    @NotNull
    public abstract List<String> getDeniedLocations();
}
