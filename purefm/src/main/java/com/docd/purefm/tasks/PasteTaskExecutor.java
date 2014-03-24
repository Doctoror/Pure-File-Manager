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
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandExists;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.ui.activities.MonitoredActivity;
import com.docd.purefm.ui.dialogs.FileExistsDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ClipBoard;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

public final class PasteTaskExecutor implements OnClickListener {

    private final WeakReference<MonitoredActivity> mActivityReference;
    
    private final GenericFile mTargetFile;
    private final LinkedList<GenericFile> mToProcess;
    private final HashMap<File, GenericFile> mExisting;

    private GenericFile current;
    
    public PasteTaskExecutor(@NotNull final MonitoredActivity activity,
                             @NotNull final GenericFile targetFile) {
        this.mActivityReference = new WeakReference<>(activity);
        this.mTargetFile = targetFile;
        this.mToProcess = new LinkedList<>();
        this.mExisting = new HashMap<>();
    }
    
    public void start() {
        final GenericFile[] contents = ClipBoard.getClipBoardContents();
        if (contents == null) {
            return;
        }

        final GenericFile target = this.mTargetFile;

        if (target instanceof JavaFile) {
            for (final GenericFile file : contents) {

                if (file != null && file.exists()) {
                    
                    final File testTarget = new File(target.toFile(), file.getName());
                    if (testTarget.exists()) {
                        mExisting.put(testTarget, file);
                    } else {
                        mToProcess.add(file);
                    }
                }
            }
        } else {
            final Shell shell = ShellHolder.getShell();
            if (shell != null) {
                Log.w("PasteTaskExecutor", "shell is null, skipping");
                for (final GenericFile file : contents) {
                    final File tmp = new File(target.toFile(), file.getName());
                    if (CommandLine.execute(shell, new CommandExists(tmp.getAbsolutePath()))) {
                        mExisting.put(tmp, file);
                    } else {
                        mToProcess.add(file);
                    }
                }
            }
        }
        
        next();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.R.id.button1:
                // replace
                mToProcess.add(current);
                break;
                    
            case android.R.id.button2:
                // replace all;
                mToProcess.add(current);
                for (File f : mExisting.keySet()) {
                    mToProcess.add(mExisting.get(f));
                }
                mExisting.clear();
                break;
                
            case R.id.button4:
                //skip all
                mExisting.clear();
                break;
                
            case R.id.button5:
                //abort
                mExisting.clear();
                mToProcess.clear();
                return;
                
        }
            
        next();
    }   
    private void next() {
        final MonitoredActivity activity = this.mActivityReference.get();
        if (activity != null) {
            if (mExisting.isEmpty()) {
            
                if (mToProcess.isEmpty()) {
                    ClipBoard.clear();
                } else {
                    final GenericFile[] files = new GenericFile[mToProcess.size()];
                    mToProcess.toArray(files);

                    final PasteTask task = new PasteTask(activity, this.mTargetFile);
                    task.execute(files);
                }
            
            } else {
                final File key = mExisting.keySet().iterator().next();
                this.current = mExisting.get(key);
                mExisting.remove(key);

                final Dialog dialog = new FileExistsDialog(activity, current.getAbsolutePath(),
                        key.getAbsolutePath(), this, this, this, this, this);
                if (!activity.isFinishing()) {
                    dialog.show();
                }
            }
        }
    }
}
