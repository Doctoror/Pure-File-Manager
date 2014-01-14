package com.docd.purefm.tasks;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.commandline.Command;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CopyCommand;
import com.docd.purefm.commandline.MoveCommand;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.CommandLineFile;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.ArrayUtils;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

final class PasteTask extends AsyncTask<GenericFile, Void, Exception> {

    private final WeakReference<Activity> activity;
    private final GenericFile targetPath;

    private ProgressDialog dialog;

    protected PasteTask(Activity activity, GenericFile targetPath) {
        this.activity = new WeakReference<Activity>(activity);
        this.targetPath = targetPath;
    }

    @NotNull
    private CharSequence getTitle() {
        final Activity activity = this.activity.get();
        if (activity != null) {
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
        return "";
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = this.activity.get();
        if (activity != null) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(this.getTitle());
            this.dialog.setCancelable(true);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            if (!activity.isFinishing()) {
                this.dialog.show();
            }
        }
    }

    @Override
    protected Exception doInBackground(GenericFile... contents) {
        final List<File> filesDeleted = new LinkedList<File>();
        final List<File> filesCreated = new LinkedList<File>();
        
        final boolean isCut = ClipBoard.isCut();
        final GenericFile target = this.targetPath;
        final Exception result;

        if (target instanceof JavaFile) {
            result = processJavaFiles(contents, target, isCut, filesCreated, filesDeleted);
        } else {
            result = processCommandLineFiles(contents, target, isCut, filesCreated, filesDeleted);
        }

        final Activity activity = this.activity.get();
        if (activity != null) {
            final Context context = activity.getApplicationContext();
            if (!filesDeleted.isEmpty()) {
                MediaStoreUtils.deleteFiles(context, filesDeleted);
            }
            if (!filesCreated.isEmpty()) {
                PureFMFileUtils.requestMediaScanner(context, filesCreated);
            }
        }

        return result;
    }
    
    private Exception processJavaFiles(GenericFile[] contents, GenericFile target, boolean isCut, List<File> filesCreated, List<File> filesDeleted) {
        Exception result = null;
        for (final GenericFile current : contents) {
            if (this.isCancelled()) {
                return result;
            }

            if (current != null && current.exists()) {
                
                if (isCut) {
                    if (current.move(target)) {
                        filesDeleted.add(current.toFile());
                        filesCreated.add(target.toFile());
                    } else {
                        result = new Exception("Failed to move " + current.getName() + " to " + target.getName());
                    }
                } else {
                    if (current.copy(target)) {
                        filesCreated.add(target.toFile());
                    } else {
                        result = new Exception("Failed to copy " + current.getName() + " to " + target.getName());
                    }
                }
            }
        }
        return result;
    }
    
    private Exception processCommandLineFiles(GenericFile[] contents, GenericFile target, boolean isCut, List<File> filesCreated, List<File> filesDeleted) {
        Exception result = null;
        final CommandLineFile[] cont = new CommandLineFile[contents.length];
        ArrayUtils.copyArrayAndCast(contents, cont);
        final CommandLineFile t = (CommandLineFile) target;
        final String targetPath = target.getAbsolutePath();
        final boolean wasRemounted;
        if (targetPath.startsWith(Environment.androidRootDirectory.getAbsolutePath())) {
            RootTools.remount(targetPath, "RW");
            wasRemounted = true;
        } else {
            wasRemounted = false;
        }

        final Shell shell = ShellHolder.getShell();
        for (final CommandLineFile current : cont) {
            final Command command = (isCut ? new MoveCommand(current, t) :
                    new CopyCommand(current, t));

            final boolean res = CommandLine.execute(shell, command);
            if (res) {
                if (isCut) {
                    filesDeleted.add(current.toFile());
                }
                filesCreated.add(current.toFile());
            } else {
                result = new Exception("Failed to move some files");
            }

        }
        if (wasRemounted) {
            RootTools.remount(targetPath, "RO");
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
        if (activity != null) {
            if (result != null) {
                Toast.makeText(activity, result.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            ClipBoard.clear();
        }
    }

}
