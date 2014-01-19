package com.docd.purefm.tasks;

import android.os.AsyncTask;

import java.io.File;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;

public class SearchJavaTask extends AsyncTask<String, GenericFile, Void> {

    @Override
    protected Void doInBackground(String... params) {
        try {
            this.search(new File(params[1]), params[0]);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void search(final File location, final String name) {
        final File[] files = location.listFiles();
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
