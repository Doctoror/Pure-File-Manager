package com.docd.purefm.tasks;

import java.io.File;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;

public class SearchJavaTask extends CancelableTask<String, GenericFile, Void> {

    @Override
    protected Void doInBackground(String... params) {
        final File target = new File(params[0]);
        try {
            this.search(target, params[1]);
        } catch (Throwable e) {
        }
        return null;
    }
    
    private void search(final File source, final String name) {
        final File[] files = source.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (this.isCancelled()) {
                    return;
                }
                if (files[i].isDirectory()) {
                    search(files[i], name);
                }
                else if (files[i].getName().indexOf(name) != -1) {
                    this.publishProgress(new JavaFile(files[i]));
                }
            }
        }
    }
    
}
