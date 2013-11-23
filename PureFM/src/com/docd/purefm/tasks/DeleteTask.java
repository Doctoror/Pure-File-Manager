package com.docd.purefm.tasks;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.LocalBroadcastManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.Remove;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;

public final class DeleteTask extends
        AsyncTask<GenericFile, Void, Exception> {

    private final Activity activity;

    private ProgressDialog dialog;

    public DeleteTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        this.dialog = new ProgressDialog(this.activity);
        this.dialog.setMessage(activity
                .getString(R.string.progress_deleting_files));
        this.dialog.setCancelable(true);
        this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(false);
            }
        });
        if (!this.activity.isFinishing()) {
            this.dialog.show();
        }
    }

    @Override
    protected Exception doInBackground(GenericFile... files) {
        Exception result = null;
        final List<File> filesAffected = new LinkedList<File>();
        
        if (files[0] instanceof CommandLineFile) {
            
            final List<Command> commands = new LinkedList<Command>();
            
            for (GenericFile file : files) {
                commands.add(new Remove(false, file.toFile()));
            }
           
            if (CommandLine.execute(commands)) {
                for (GenericFile file : files) {
                    filesAffected.add(file.toFile());
                }
            } else if (Settings.su) {
                commands.clear();
                for (GenericFile file : files) {
                    commands.add(new Remove(true, file.toFile()));
                }
                if (CommandLine.execute(commands)) {
                    for (GenericFile file : files) {
                        filesAffected.add(file.toFile());
                    }
                } else {
                    result = new Exception("Can't delete selected files");
                }
            } else {
                result = new Exception("Can't delete selected files");
            }
        } else {
            for (GenericFile file : files) {
                if (file.delete()) {
                    filesAffected.add(file.toFile());
                } else {
                    result = new Exception("Can't delete " + file.getName());
                }
            }
        }
        
        if (!filesAffected.isEmpty()) {
            MediaStoreUtils.deleteFiles(activity.getApplicationContext(), filesAffected);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Extras.BROADCAST_REFRESH));
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
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Extras.BROADCAST_REFRESH));
        if (result != null) {
            Toast.makeText(activity, result.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
