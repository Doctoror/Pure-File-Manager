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
package com.docd.purefm.operations;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.docd.purefm.ActivityMonitor;
import com.docd.purefm.R;
import com.docd.purefm.file.FileFactory;
import com.docd.purefm.file.FileObserverNotifier;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.services.MultiWorkerService;
import com.docd.purefm.ui.activities.BrowserPagerActivity;
import com.docd.purefm.utils.ArrayUtils;
import com.docd.purefm.utils.ClipBoard;
import com.docd.purefm.utils.MediaStoreUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * IntentService that performs file operations
 *
 * @author Doctoror
 */
public final class OperationsService extends MultiWorkerService
        implements ActivityMonitor.OnActivitiesOpenedListener {

    public static final String BROADCAST_OPERATION_COMPLETED = "OperationsService.broadcasts.OPERATION_COMPLETED";
    public static final String EXTRA_ACTION = "OperationsService.extras.ACTION";
    public static final String EXTRA_WAS_CANCELED = "OperationsService.extras.WAS_CANCELED";
    public static final String EXTRA_RESULT = "OperationsService.extras.RESULT";
    public static final String EXTRA_RESULT_CLASS = "OperationsService.extras.RESULT_CLASS";

    public static final String ACTION_PASTE = "OperationsService.actions.PASTE";
    public static final String ACTION_DELETE = "OperationsService.actions.DELETE";
    public static final String ACTION_RENAME = "OperationsService.actions.RENAME";
    public static final String ACTION_CREATE_FILE = "OperationsService.actions.CREATE_FILE";
    public static final String ACTION_CREATE_DIRECTORY = "OperationsService.actions.CREATE_DIRECTORY";

    private static final String ACTION_CANCEL_PASTE = "OperationsService.actions.cancel.PASTE";
    private static final String ACTION_CANCEL_DELETE = "OperationsService.actions.cancel.DELETE";

    private static final String EXTRA_FILE_NAME = "OperationsService.extras.FILE_NAME";
    private static final String EXTRA_FILES = "OperationsService.extras.FILES";
    private static final String EXTRA_FILE = "OperationsService.extras.FILE";
    private static final String EXTRA_IS_MOVE = "OperationsService.extras.IS_MOVE";

    private IBinder mLocalBinder;
    private OperationListener mOperationListener;

    private PasteOperation mPasteOperation;
    private DeleteOperation mDeleteOperation;

    private final Object mOperationListenerLock = new Object();

    private Handler mHandler;
    private OnOperationStartedRunnable mPendingOperationStartedRunnable;
    private OnOperationEndedRunnable mPendingOperationEndedRunnable;

    private enum EOperation {
        PASTE(0), DELETE(1);

        final int mId;

        private EOperation(final int id) {
            mId = id;
        }
    }

    public static void paste(@NotNull final Context context,
                             @NotNull final GenericFile target,
                             @NotNull final GenericFile[] files,
                             final boolean isMove) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_PASTE);
        intent.putExtra(EXTRA_FILE, target);
        intent.putExtra(EXTRA_FILES, files);
        intent.putExtra(EXTRA_IS_MOVE, isMove);
        context.startService(intent);
    }

    public static void cancelPaste(@NotNull final Context context) {
        context.startService(getCancelPasteIntent(context));
    }

    public static void delete(@NotNull final Context context,
                              @NotNull final GenericFile[] files) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_FILES, files);
        context.startService(intent);
    }

    public static void cancelDelete(@NotNull final Context context) {
        context.startService(getCancelDeleteIntent(context));
    }

    public static void rename(@NotNull final Context context,
                              @NotNull final GenericFile source,
                              @NotNull final String targetName) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_RENAME);
        intent.putExtra(EXTRA_FILE, source);
        intent.putExtra(EXTRA_FILE_NAME, targetName);
        context.startService(intent);
    }

    public static void createFile(@NotNull final Context context,
                                  @NotNull final File parent,
                                  @NotNull final String fileName) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CREATE_FILE);
        intent.putExtra(EXTRA_FILE, parent);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void createDirectory(@NotNull final Context context,
                                       @NotNull final File parent,
                                       @NotNull final String dirName) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CREATE_DIRECTORY);
        intent.putExtra(EXTRA_FILE, parent);
        intent.putExtra(EXTRA_FILE_NAME, dirName);
        context.startService(intent);
    }

    @NotNull
    private static Intent getCancelPasteIntent(@NotNull final Context context) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CANCEL_PASTE);
        return intent;
    }

    @NotNull
    private static Intent getCancelDeleteIntent(@NotNull final Context context) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CANCEL_DELETE);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper());
        ActivityMonitor.addOnActivitiesOpenedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityMonitor.removeOnActivitiesOpenedListener(this);
    }

    @Override
    protected void onHandleIntent(@NotNull final Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PASTE:
                    onActionPaste(intent);
                    break;

                case ACTION_DELETE:
                    onActionDelete(intent);
                    break;

                case ACTION_RENAME:
                    onActionRename(intent);
                    break;

                case ACTION_CREATE_FILE:
                    onActionCreateFile(intent);
                    break;

                case ACTION_CREATE_DIRECTORY:
                    onActionCreateDirectory(intent);
                    break;

                case ACTION_CANCEL_PASTE:
                    if (mPasteOperation != null) {
                        mPasteOperation.cancel(true);
                    }
                    break;

                case ACTION_CANCEL_DELETE:
                    if (mDeleteOperation != null) {
                        mDeleteOperation.cancel(true);
                    }
                    break;
            }
        }
    }

    // only one paste operation simultaneously
    private synchronized void onActionPaste(@NotNull final Intent pasteIntent) {
        final GenericFile target = (GenericFile) pasteIntent.getSerializableExtra(EXTRA_FILE);
        if (target == null) {
            throw new RuntimeException("ACTION_PASTE intent should contain non-null EXTRA_FILE1");
        }

        final Object[] filesObject = (Object[]) pasteIntent.getSerializableExtra(EXTRA_FILES);
        if (filesObject == null) {
            throw new RuntimeException("ACTION_PASTE intent should contain non-null EXTRA_FILES");
        }
        final GenericFile[] files = new GenericFile[filesObject.length];
        ArrayUtils.copyArrayAndCast(filesObject, files);
        final boolean isMove = pasteIntent.getBooleanExtra(EXTRA_IS_MOVE, false);
        mPasteOperation = new PasteOperation(getApplicationContext(), target, isMove);
        synchronized (mOperationListenerLock) {
            if (mOperationListener != null) {
                mPendingOperationStartedRunnable = new OnOperationStartedRunnable(
                        mOperationListener, getOperationMessage(EOperation.PASTE),
                                getCancelPasteIntent(getApplicationContext()));
                mHandler.removeCallbacks(mPendingOperationEndedRunnable);
                mHandler.post(mPendingOperationStartedRunnable);
            }
        }
        onOperationCompleted(ACTION_PASTE,
                mPasteOperation.execute(files),
                mPasteOperation.isCanceled());
    }

    //only one deletion operation can be done simultaneously
    private synchronized void onActionDelete(@NotNull final Intent deleteIntent) {
        final Object[] filesObject = (Object[]) deleteIntent.getSerializableExtra(EXTRA_FILES);
        if (filesObject == null) {
            throw new RuntimeException("ACTION_DELETE intent should contain non-null EXTRA_FILES");
        }

        final GenericFile[] files = new GenericFile[filesObject.length];
        ArrayUtils.copyArrayAndCast(filesObject, files);
        mDeleteOperation = new DeleteOperation(getApplicationContext());
        synchronized (mOperationListenerLock) {
            if (mOperationListener != null) {
                mPendingOperationStartedRunnable = new OnOperationStartedRunnable(
                        mOperationListener, getOperationMessage(EOperation.DELETE),
                                getCancelDeleteIntent(getApplicationContext()));
                mHandler.removeCallbacks(mPendingOperationEndedRunnable);
                mHandler.post(mPendingOperationStartedRunnable);
            }
        }
        onOperationCompleted(ACTION_DELETE,
                mDeleteOperation.execute(files),
                mDeleteOperation.isCanceled());
    }

    private void onActionRename(@NotNull final Intent renameIntent) {
        final GenericFile source = (GenericFile) renameIntent.getSerializableExtra(EXTRA_FILE);
        final String target = renameIntent.getStringExtra(EXTRA_FILE_NAME);
        if (source == null || target == null) {
            throw new RuntimeException(
                    "ACTION_RENAME intent should contain non-null EXTRA_FILE1 and EXTRA_FILE_NAME");
        }

        final RenameOperation renameOperation = new RenameOperation(
                getApplicationContext(), source, target);
        onOperationCompleted(ACTION_RENAME,
                renameOperation.execute(),
                renameOperation.isCanceled());
    }

    private void onActionCreateFile(@NotNull final Intent createIntent) {
        final File parent = (File) createIntent.getSerializableExtra(EXTRA_FILE);
        if (parent == null) {
            throw new RuntimeException(
                    "ACTION_CREATE_FILE intent should contain non-null EXTRA_FILE1");
        }
        final String fileName = createIntent.getStringExtra(EXTRA_FILE_NAME);
        final GenericFile target = FileFactory.newFile(parent, fileName);
        CharSequence message = null;
        if (target.exists()) {
            message = getText(R.string.file_exists);
        } else {
            try {
                if (!target.createNewFile()) {
                    message = getText(R.string.could_not_create_file);
                } else {
                    MediaStoreUtils.addEmptyFileOrDirectory(getContentResolver(), target);
                    FileObserverNotifier.notifyCreated(target);
                }
            } catch (IOException e) {
                message = e.getMessage();
            }
        }
        onOperationCompleted(ACTION_CREATE_FILE,
                message,
                false);
    }

    private void onActionCreateDirectory(@NotNull final Intent createIntent) {
        final File parent = (File) createIntent.getSerializableExtra(EXTRA_FILE);
        if (parent == null) {
            throw new RuntimeException(
                    "ACTION_CREATE_DIRECTORY intent should contain non-null EXTRA_FILE1");
        }
        final String fileName = createIntent.getStringExtra(EXTRA_FILE_NAME);
        final GenericFile target = FileFactory.newFile(parent, fileName);
        CharSequence message = null;
        if (target.exists()) {
            message = getText(R.string.file_exists);
        } else {
            if (!target.mkdir()) {
                message = getText(R.string.could_not_create_dir);
            } else {
                MediaStoreUtils.addEmptyFileOrDirectory(getContentResolver(), target);
                FileObserverNotifier.notifyCreated(target);
            }
        }
        onOperationCompleted(ACTION_CREATE_DIRECTORY,
                message,
                false);
    }

    private void onOperationCompleted(@NotNull final String action, @Nullable CharSequence result, final boolean wasCanceled) {
        mHandler.removeCallbacks(mPendingOperationStartedRunnable);
        synchronized (mOperationListenerLock) {
            if (mOperationListener != null) {
                mPendingOperationEndedRunnable = new OnOperationEndedRunnable(
                        mOperationListener, result);
                mHandler.post(mPendingOperationEndedRunnable);
            }
        }
        stopForeground(true);
        final Intent broadcast = createOperationCompletedIntent(action, wasCanceled);
        broadcast.putExtra(EXTRA_RESULT, result);
        broadcast.putExtra(EXTRA_RESULT_CLASS, CharSequence.class);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }

    private <T extends Serializable> void onOperationCompleted(
            @NotNull final String action, @Nullable T result, final boolean wasCanceled) {
        mHandler.removeCallbacks(mPendingOperationStartedRunnable);
        synchronized (mOperationListenerLock) {
            if (mOperationListener != null) {
                mPendingOperationEndedRunnable = new OnOperationEndedRunnable(
                        mOperationListener, result);
                mHandler.post(mPendingOperationEndedRunnable);
            }
        }
        stopForeground(true);
        final Intent broadcast = createOperationCompletedIntent(action, wasCanceled);
        broadcast.putExtra(EXTRA_RESULT, result);
        broadcast.putExtra(EXTRA_RESULT_CLASS, Serializable.class);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }

    private Intent createOperationCompletedIntent(
            @NotNull final String action, final boolean wasCanceled) {
        final Intent broadcast = new Intent(BROADCAST_OPERATION_COMPLETED);
        broadcast.putExtra(EXTRA_ACTION, action);
        broadcast.putExtra(EXTRA_WAS_CANCELED, wasCanceled);
        return broadcast;
    }

    @Override
    public void onActivitiesCreated() {
    }

    @Override
    public void onActivitiesStarted() {
        //once notification is shown, it's okay to leave it until the operation completed
    }

    @Override
    public void onActivitiesStopped() {
        if (mPasteOperation != null) {
            startForeground(EOperation.PASTE);
        } else if (mDeleteOperation != null) {
            startForeground(EOperation.DELETE);
        }
    }

    @Override
    public void onActivitiesDestroyed() {
    }

    private void startForeground(@NotNull final EOperation opeartion) {
        final Context context = getApplicationContext();
        final Notification.Builder b = new Notification.Builder(context);
        b.setContentTitle(getText(R.string.app_name));
        b.setOngoing(true);
        b.setProgress(0, 0, true);
        b.setSmallIcon(R.drawable.ic_stat_notify);
        b.setContentText(getOperationMessage(opeartion));
        b.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(
                context, BrowserPagerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        startForeground(opeartion.mId, build(b));
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @NotNull
    private static Notification build(@NotNull final Notification.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        }
        return builder.getNotification();
    }

    private CharSequence getOperationMessage(final EOperation operation) {
        switch (operation) {
            case PASTE:
                final GenericFile[] files = ClipBoard.getClipBoardContents();
                if (files != null) {
                    final int textResId = ClipBoard.isMove() ? R.plurals.progress_moving_n_files :
                            R.plurals.progress_copying_n_files;
                    return getResources().getQuantityString(
                            textResId, files.length, files.length);
                }
                break;

            case DELETE:
                return getText(R.string.progress_deleting_files);

            default:
                break;
        }
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mLocalBinder == null) {
            mLocalBinder = new LocalBinder();
        }
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        synchronized (mOperationListenerLock) {
            mOperationListener = null;
        }
        return super.onUnbind(intent);
    }

    public interface OperationListener {
        void onOperationStarted(@Nullable CharSequence operationMessage,
                                @NotNull Intent cancelIntent);

        void onOperationEnded(@Nullable Object result);
    }

    private static final class OnOperationStartedRunnable implements Runnable {

        private final CharSequence mOperationMessage;
        private final Intent mCancelIntent;

        private final OperationListener mOperationListener;

        OnOperationStartedRunnable(@NotNull OperationListener operationListener,
                                   @Nullable final CharSequence operationMessage,
                                   @NotNull final Intent cancelIntent) {
            this.mOperationListener = operationListener;
            this.mOperationMessage = operationMessage;
            this.mCancelIntent = cancelIntent;
        }

        @Override
        public void run() {
            mOperationListener.onOperationStarted(mOperationMessage, mCancelIntent);
        }
    }

    private static final class OnOperationEndedRunnable implements Runnable {

        private final Object mResult;
        private final OperationListener mOperationListener;

        OnOperationEndedRunnable(@NotNull OperationListener operationListener,
                                 @Nullable final Object result) {
            this.mOperationListener = operationListener;
            this.mResult = result;
        }

        @Override
        public void run() {
            mOperationListener.onOperationEnded(mResult);
        }
    }

    public final class LocalBinder extends Binder {

        public void setOperationListener(@Nullable final OperationListener l) {
            if (l != null) {
                if (mDeleteOperation != null) {
                    l.onOperationStarted(getOperationMessage(EOperation.DELETE),
                            getCancelDeleteIntent(getApplicationContext()));
                } else if (mPasteOperation != null) {
                    l.onOperationStarted(getOperationMessage(EOperation.PASTE),
                            getCancelPasteIntent(getApplicationContext()));
                } else {
                    l.onOperationEnded(null);
                }
            }
            synchronized (mOperationListenerLock) {
                mOperationListener = l;
            }
        }
    }
}
