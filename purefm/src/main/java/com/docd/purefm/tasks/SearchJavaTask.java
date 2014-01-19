package com.docd.purefm.tasks;

import android.os.AsyncTask;

import java.io.File;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;

public class SearchJavaTask extends AsyncTask<String, GenericFile, Void> {

    @Override
    protected Void doInBackground(String... params) {
        final File target = new File(params[0]);
        try {
            this.search(target, params[1]);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void search(final File source, final String name) {
        final File[] files = source.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (this.isCancelled()) {
                    return;
                }
                if (file.isDirectory()) {
                    search(file, name);
                }
                else if (file.getName().contains(name)) {
                    this.publishProgress(new JavaFile(file));
                }
            }
        }
    }
    
}
