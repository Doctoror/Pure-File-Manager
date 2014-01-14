package com.docd.purefm.tasks;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.RemoveCommand;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.stericson.RootTools.execution.Shell;

public final class DeleteTask extends
        AsyncTask<GenericFile, Void, Exception> {

    private final WeakReference<Activity> activity;

    private ProgressDialog dialog;

    public DeleteTask(final Activity activity) {
        this.activity = new WeakReference<Activity>(activity);
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = this.activity.get();
        if (activity != null) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(activity.getString(R.string.progress_deleting_files));
            this.dialog.setCancelable(true);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(false);
                }
            });
            if (!activity.isFinishing()) {
                this.dialog.show();
            }
        }
    }

    @Override
    protected Exception doInBackground(GenericFile... files) {
        Exception result = null;
        final List<File> filesAffected = new LinkedList<File>();
        
        if (files[0] instanceof CommandLineFile) {

            final Shell shell = ShellHolder.getShell();
            for (final GenericFile file : files) {
                final File fileFile = file.toFile();
                if (CommandLine.execute(shell, new RemoveCommand(fileFile))) {
                    filesAffected.add(fileFile);
                } else {
                    result = new Exception("Can't delete some files");
                }
            }
        } else {
            for (final GenericFile file : files) {
                if (file.delete()) {
                    filesAffected.add(file.toFile());
                } else {
                    result = new Exception("Can't delete some files");
                }
            }
        }

        final Activity activity = this.activity.get();
        if (activity != null && !filesAffected.isEmpty()) {
            MediaStoreUtils.deleteFiles(activity.getApplicationContext(), filesAffected);
            PureFMFileUtils.requestMediaScanner(activity, filesAffected);
        }
        
        return result;
    }

    @Override
    protected void onPostExecute(Exception result) {
        super.onPostExecute(result);
        this.finish(result);
    }

    @Override
    protected void onCancelled(Exception result) {
        super.onCancelled(result);
        this.finish(result);
    }

    private void finish(Exception result) {
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        final Activity activity = this.activity.get();
        if (activity != null && result != null) {
            Toast.makeText(activity, result.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
