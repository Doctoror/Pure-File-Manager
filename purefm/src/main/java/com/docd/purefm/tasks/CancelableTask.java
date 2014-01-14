package com.docd.purefm.tasks;

import android.os.AsyncTask;

public abstract class CancelableTask<T1, T2, T3> extends AsyncTask<T1, T2, T3> {

    public void cancel() {
        this.cancel(true);
    }
}
