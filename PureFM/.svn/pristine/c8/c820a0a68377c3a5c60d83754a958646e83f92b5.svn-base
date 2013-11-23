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
import com.docd.purefm.browser.Browser;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.PureFMFileUtils;

final class PasteTask extends AsyncTask<GenericFile, Void, Exception> {

    private final Activity activity;
    private final Browser browser;

    private ProgressDialog dialog;

    protected PasteTask(Activity activity, Browser browser) {
        this.activity = activity;
        this.browser = browser;
    }

    private CharSequence getTitle() {
        final StringBuilder title = new StringBuilder();
        if (ClipBoard.isCut()) {
            title.append(activity.getString(R.string.progress_moving));
        } else {
            title.append(activity.getString(R.string.progress_copying));
        }
        title.append(ClipBoard.getClipBoardContents().length);
        title.append(activity.getString(R.string._files));
        return title.toString();
    }

    @Override
    protected void onPreExecute() {
        this.dialog = new ProgressDialog(this.activity);
        this.dialog.setMessage(this.getTitle());
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
    protected Exception doInBackground(GenericFile... contents) {
        final List<File> filesAffected = new LinkedList<File>();
        
        final boolean isCut = ClipBoard.isCut();
        final GenericFile target = this.browser.getPath();
        Exception result = null;

        if (target instanceof JavaFile) {
            processJavaFiles(contents, target, isCut, filesAffected);
        } else {
            processCommandLineFiles(contents, target, isCut, filesAffected);
        }

        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Extras.BROADCAST_REFRESH));
        return result;
    }
    
    private Exception processJavaFiles(GenericFile[] contents, GenericFile target, boolean isCut, List<File> filesAffected) {
        Exception result = null;
        for (int i = 0; i < contents.length; i++) {
            if (this.isCancelled()) {
                return result;
            }

            if (contents[i] != null && contents[i].exists()) {
                
                if (isCut) {
                    if (contents[i].move(target)) {
                        filesAffected.add(contents[i].toFile());
                        filesAffected.add(target.toFile());
                    } else {
                        result = new Exception("Failed to move " + contents[i].getName() + " to " + target.getName());
                    }
                } else {
                    if (contents[i].copy(target)) {
                        filesAffected.add(target.toFile());
                    } else {
                        result = new Exception("Failed to copy " + contents[i].getName() + " to " + target.getName());
                    }
                }
            }
        }
        return result;
    }
    
    private Exception processCommandLineFiles(GenericFile[] contents, GenericFile target, boolean isCut, List<File> filesAffected) {
        Exception result = null;
        final List<Command> commands = new LinkedList<Command>();
        final CommandLineFile[] cont = new CommandLineFile[contents.length];
        for (int i = 0; i < cont.length; i++) {
            cont[i] = (CommandLineFile) contents[i];
        }
        final CommandLineFile t = (CommandLineFile) target;
        for (int i = 0; i < cont.length; i++) {
            commands.add(isCut ? cont[i].getMoveCommand(t) :
                cont[i].getCopyCommand(t));
        }
        if (CommandLine.execute(commands)) {
            for (int i = 0; i < contents.length; i++) {
                if (isCut) {
                    filesAffected.add(contents[i].toFile());
                }
                filesAffected.add(target.toFile());
            }
            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Extras.BROADCAST_REFRESH));
        } else {
            if (isCut) {
                result = new Exception("Failed to move selected files");
            } else {
                result = new Exception("Failed to copy selected files");
            }
        }
        PureFMFileUtils.requestMediaScanner(activity, filesAffected);
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
        if (result != null) {
            Toast.makeText(activity, result.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        ClipBoard.clear();
        this.browser.invalidate();
    }

}
