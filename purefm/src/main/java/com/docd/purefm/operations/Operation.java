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

/**
 * Operation that is performed by {@link com.docd.purefm.operations.OperationsService}
 *
 * @param <Param> type of array that will be passed to {@link #execute(Object[])}
 * @param <Result> type that is returned by  {@link #execute(Object[])}
 *
 * @author Doctoror
 */
abstract class Operation<Param, Result> {

    private volatile boolean mCanceled;
    private Thread mOperationThread;

    protected Operation() {
    }

    public final void cancel(final boolean interrupt) {
        mCanceled = true;
        if (interrupt) {
            mOperationThread.interrupt();
        }
    }

    protected final boolean isCanceled() {
        return mCanceled;
    }

    public final Result execute(Param... params) {
        mOperationThread = Thread.currentThread();
        return doInBackground(params);
    }

    protected abstract Result doInBackground(Param... params);

}
