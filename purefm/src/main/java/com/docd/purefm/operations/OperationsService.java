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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ArrayUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * IntentService that performs file operations
 *
 * @author Doctoror
 */
public final class OperationsService extends IntentService {

    public static final String BROADCAST_OPERATION_COMPLETED = "OperationsService.broadcasts.OPERATION_COMPLETED";
    public static final String EXTRA_ACTION = "OperationsService.extras.ACTION";
    public static final String EXTRA_WAS_CANCELED = "OperationsService.extras.WAS_CANCELED";
    public static final String EXTRA_RESULT = "OperationsService.extras.RESULT";

    public static final String ACTION_PASTE = "OperationsService.actions.PASTE";
    public static final String ACTION_DELETE = "OperationsService.actions.DELETE";
    public static final String ACTION_RENAME = "OperationsService.actions.RENAME";
    public static final String ACTION_CREATE_FILE = "OperationsService.actions.CREATE_FILE";
    public static final String ACTION_CREATE_DIRECTORY = "OperationsService.actions.CREATE_DIRECTORY";

    private static final String ACTION_CANCEL_PASTE = "OperationsService.actions.cancel.PASTE";
    private static final String ACTION_CANCEL_DELETE = "OperationsService.actions.cancel.DELETE";

    private static final String EXTRA_FILES = "OperationsService.extras.FILES";
    private static final String EXTRA_FILE1 = "OperationsService.extras.FILE1";
    private static final String EXTRA_FILE2 = "OperationsService.extras.FILE2";
    private static final String EXTRA_IS_MOVE = "OperationsService.extras.IS_MOVE";

    private PasteOperation mPasteOperation;
    private DeleteOperation mDeleteOperation;

    public static void paste(@NotNull final Context context,
                             @NotNull final GenericFile target,
                             @NotNull final GenericFile[] files,
                             final boolean isMove) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_PASTE);
        intent.putExtra(EXTRA_FILE1, target);
        intent.putExtra(EXTRA_FILES, files);
        intent.putExtra(EXTRA_IS_MOVE, isMove);
        context.startService(intent);
    }

    public static void cancelPaste(@NotNull final Context context) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CANCEL_PASTE);
        context.startService(intent);
    }

    public static void delete(@NotNull final Context context,
                              @NotNull final GenericFile[] files) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_FILES, files);
        context.startService(intent);
    }

    public static void cancelDelete(@NotNull final Context context) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CANCEL_DELETE);
        context.startService(intent);
    }

    public static void rename(@NotNull final Context context,
                              @NotNull final GenericFile source,
                              @NotNull final GenericFile target) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_RENAME);
        intent.putExtra(EXTRA_FILE1, source);
        intent.putExtra(EXTRA_FILE2, target);
        context.startService(intent);
    }

    public static void createFile(@NotNull final Context context,
                                  @NotNull final GenericFile toCreate) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CREATE_FILE);
        intent.putExtra(EXTRA_FILE1, toCreate);
        context.startService(intent);
    }

    public static void createDirectory(@NotNull final Context context,
                                  @NotNull final GenericFile toCreate) {
        final Intent intent = new Intent(context, OperationsService.class);
        intent.setAction(ACTION_CREATE_DIRECTORY);
        intent.putExtra(EXTRA_FILE1, toCreate);
        context.startService(intent);
    }

    public OperationsService() {
        super("OperationsService");
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(@NotNull final Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_PASTE)) {
                onActionPaste(intent);
            } else if (action.equals(ACTION_DELETE)) {
                onActionDelete(intent);
            } else if (action.equals(ACTION_RENAME)) {
                onActionRename(intent);
            } else if (action.equals(ACTION_CREATE_FILE)) {
                onActionCreateFile(intent);
            } else if (action.equals(ACTION_CREATE_DIRECTORY)) {
                onActionCreateDirectory(intent);
            } else if (action.equals(ACTION_CANCEL_DELETE)) {
                if (mPasteOperation != null) {
                    mPasteOperation.cancel();
                }
            } else if (action.equals(ACTION_CANCEL_DELETE)) {
                if (mDeleteOperation != null) {
                    mDeleteOperation.cancel();
                }
            }
        }
    }

    // only one paste at a time can be done so it's synchronized
    private synchronized void onActionPaste(@NotNull final Intent pasteIntent) {
        final GenericFile target = (GenericFile) pasteIntent.getSerializableExtra(EXTRA_FILE1);
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
        onOperationCompleted(ACTION_PASTE,
                mPasteOperation.execute(files),
                mPasteOperation.isCanceled());
    }

    private synchronized void onActionDelete(@NotNull final Intent deleteIntent) {
        final Object[] filesObject = (Object[]) deleteIntent.getSerializableExtra(EXTRA_FILES);
        if (filesObject == null) {
            throw new RuntimeException("ACTION_DELETE intent should contain non-null EXTRA_FILES");
        }

        final GenericFile[] files = new GenericFile[filesObject.length];
        ArrayUtils.copyArrayAndCast(filesObject, files);
        mDeleteOperation = new DeleteOperation(getApplicationContext());
        onOperationCompleted(ACTION_DELETE,
                mDeleteOperation.execute(files),
                mDeleteOperation.isCanceled());
    }

    private void onActionRename(@NotNull final Intent renameIntent) {
        final GenericFile source = (GenericFile) renameIntent.getSerializableExtra(EXTRA_FILE1);
        final GenericFile target = (GenericFile) renameIntent.getSerializableExtra(EXTRA_FILE2);
        if (source == null || target == null) {
            throw new RuntimeException(
                    "ACTION_RENAME intent should contain non-null EXTRA_FILE1 and EXTRA_FILE2");
        }

        final RenameOperation renameOperation = new RenameOperation(
                getApplicationContext(), source, target);
        onOperationCompleted(ACTION_RENAME,
                renameOperation.execute(),
                renameOperation.isCanceled());
    }

    private void onActionCreateFile(@NotNull final Intent createIntent) {
        final GenericFile toCreate = (GenericFile) createIntent.getSerializableExtra(EXTRA_FILE1);
        if (toCreate == null) {
            throw new RuntimeException(
                    "ACTION_CREATE_FILE intent should contain non-null EXTRA_FILE1");
        }
        onOperationCompleted(ACTION_CREATE_FILE,
                toCreate.createNewFile(),
                false);
    }

    private void onActionCreateDirectory(@NotNull final Intent createIntent) {
        final GenericFile toCreate = (GenericFile) createIntent.getSerializableExtra(EXTRA_FILE1);
        if (toCreate == null) {
            throw new RuntimeException(
                    "ACTION_CREATE_DIRECTORY intent should contain non-null EXTRA_FILE1");
        }
        onOperationCompleted(ACTION_CREATE_DIRECTORY,
                toCreate.mkdir(),
                false);
    }

    private <T extends Serializable> void onOperationCompleted(
            @NotNull final String action, @Nullable T result, final boolean wasCanceled) {
        final Intent broadcast = createOperationCompletedIntent(action, wasCanceled);
        broadcast.putExtra(EXTRA_RESULT, result);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(broadcast);
    }

    private Intent createOperationCompletedIntent(
            @NotNull final String action, final boolean wasCanceled) {
        final Intent broadcast = new Intent(BROADCAST_OPERATION_COMPLETED);
        broadcast.putExtra(EXTRA_ACTION, action);
        broadcast.putExtra(EXTRA_WAS_CANCELED, wasCanceled);
        return broadcast;
    }
}
