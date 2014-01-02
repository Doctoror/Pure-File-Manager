package com.docd.purefm.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.docd.purefm.R;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.dialogs.FileExistsDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ClipBoard;

public final class PasteTaskExecutor implements OnClickListener {

    private final Activity activity;
    
    private final Browser browser;
    private final LinkedList<GenericFile> toProcess;
    private final HashMap<File, GenericFile> exist;
    
    private GenericFile current;
    
    public PasteTaskExecutor(final Activity activity, final Browser browser) {
        this.activity = activity;
        this.browser = browser;
        this.toProcess = new LinkedList<GenericFile>();
        this.exist = new HashMap<File, GenericFile>();
    }
    
    public void start() {
        final GenericFile[] contents = ClipBoard.getClipBoardContents();
        final GenericFile target = this.browser.getPath();

        if (target instanceof JavaFile) {
            for (final GenericFile file : contents) {

                if (file != null && file.exists()) {
                    
                    final File testTarget = new File(target.toFile(), file.getName());
                    if (testTarget.exists()) {
                        exist.put(testTarget, file);
                    } else {
                        toProcess.add(file);
                    }
                }
            }
        } else {
            for (final GenericFile file : contents) {
                final File tmp = new File(target.toFile(), file.getName());
                if (CommandLineUtils.exists(ShellHolder.getShell(), tmp)) {
                    exist.put(tmp, file);
                } else {
                    toProcess.add(file);
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
                toProcess.add(current);
                break;
                    
            case android.R.id.button2:
                // replace all;
                toProcess.add(current);
                for (File f : exist.keySet()) {
                    toProcess.add(exist.get(f));                        
                }
                exist.clear();
                break;
                
            case R.id.button4:
                //skip all
                exist.clear();
                break;
                
            case R.id.button5:
                //abort
                exist.clear();
                toProcess.clear();
                return;
                
        }
            
        next();
    }   
    private void next() {
        if (exist.isEmpty()) {
            
            if (toProcess.isEmpty()) {
                ClipBoard.clear();
            } else {
                final GenericFile[] files = new GenericFile[toProcess.size()];
                toProcess.toArray(files);
            
                final PasteTask task = new PasteTask(activity, browser);
                task.execute(files);
            }
            
        } else {
            final File key = exist.keySet().iterator().next();
            this.current = exist.get(key);
            exist.remove(key);
            new FileExistsDialog(activity, current.getAbsolutePath(), key.getAbsolutePath(), this, this, this, this, this).show();
        }
    }
}
