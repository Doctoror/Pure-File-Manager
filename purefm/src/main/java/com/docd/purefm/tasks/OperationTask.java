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
package com.docd.purefm.tasks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.docd.purefm.operations.OperationsService;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * OperationTask looks similar to AsyncTask, but the difference is that subclasses must start an {@link com.docd.purefm.operations.OperationsService} in
 * {@link #startService(Object[])} and return an Action using which the Service was started in {@link #getServiceAction()}
 *
 * @param <Param> type of objects that will be passed to {@link #startService(Object[])}
 * @param <Result> type of object that will be delivered in {@link #onPreExecute()}
 */
public abstract class OperationTask<Param, Result> {

    @NonNull
    protected final Activity mActivity;

    @NonNull
    private final LocalBroadcastManager mBroadcastManager;

    @NonNull
    private final OperationReceiver mOperationReceiver;

    protected OperationTask(@NonNull final Activity activity) {
        mActivity = activity;
        mBroadcastManager = LocalBroadcastManager.getInstance(activity);
        mOperationReceiver = new OperationReceiver();
    }

    @SafeVarargs
    public final void execute(@NonNull final Param... params) {
        mBroadcastManager.registerReceiver(mOperationReceiver, new IntentFilter(
                OperationsService.BROADCAST_OPERATION_COMPLETED));
        onPreExecute();
        startService(params);
    }

    protected abstract void startService(@NonNull Param... params);
    protected abstract void cancel();

    @NonNull
    protected abstract String getServiceAction();

    protected void onPreExecute() {

    }

    protected void onCancelled(Result result) {

    }

    protected void onPostExecute(Result result) {

    }

    private final class OperationReceiver extends BroadcastReceiver {


        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            mBroadcastManager.unregisterReceiver(this);
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String operation = extras.getString(OperationsService.EXTRA_ACTION);
                if (getServiceAction().equals(operation)) {
                    final Class<?> resultClass = (Class<?>) extras.getSerializable(OperationsService.EXTRA_RESULT_CLASS);
                    if (resultClass == null) {
                        throw new RuntimeException("EXTRA_RESULT_CLASS must be passed");
                    }
                    final Result result;
                    if (Serializable.class.equals(resultClass)) {
                        result = (Result) extras.getSerializable(OperationsService.EXTRA_RESULT);
                    } else if (CharSequence.class.equals(resultClass)) {
                        result = (Result) extras.getCharSequence(OperationsService.EXTRA_RESULT);
                    } else {
                        throw new RuntimeException("Unexpected Result class: " + resultClass.getName());
                    }
                    if (extras.getBoolean(OperationsService.EXTRA_WAS_CANCELED, false)) {
                        onCancelled(result);
                    } else {
                        onPostExecute(result);
                    }
                }
            }
        }
    }
}
