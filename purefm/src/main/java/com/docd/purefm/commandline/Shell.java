package com.docd.purefm.commandline;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Shell implements Closeable {

    private final Object shellLock = new Object();

    public final boolean su;

    private final Process process;

    private final BufferedReader inputStream;
    private final DataOutputStream outputStream;

    private boolean inputStreamLocked;
    private boolean outputStreamLocked;

    Shell(final boolean su) throws IOException {
        this.su = su;
        this.process = new ProcessBuilder().redirectErrorStream(false).command(su ? "su" : "sh").start();
        this.inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.outputStream = new DataOutputStream(this.process.getOutputStream());
    }

    public synchronized BufferedReader obtainInputStream() {
        synchronized(this.shellLock) {
            if (!this.outputStreamLocked) {
                throw new IllegalStateException("Should obtain InputStream only if OutputStream was obtained");
            }
            if (this.inputStreamLocked) {
                throw new IllegalStateException("inputStream was already obtained and not closed");
            }
            this.inputStreamLocked = true;
            return this.inputStream;
        }
    }

    public synchronized void releaseInputStream() {
        synchronized(this.shellLock) {
            this.inputStreamLocked = false;
            ((Object) this).notifyAll();
        }
    }

    public synchronized DataOutputStream obtainOutputStream() {
        if (this.inputStreamLocked || this.outputStreamLocked) {
            try {
                ((Object) this).wait();
            } catch (InterruptedException e) {}
            //throw new IllegalStateException("Should obtain OutputStream only after InputStream was released");
        }
        synchronized(this.shellLock) {
            if (this.outputStreamLocked) {
                throw new IllegalStateException("outputStream was already obtained and not closed");
            }
            this.outputStreamLocked = true;
            return this.outputStream;
        }
    }

    public synchronized void releaseOutputStream() {
        synchronized(this.shellLock) {
            this.outputStreamLocked = false;
        }
    }

    public void close() throws IOException {
        synchronized(this.shellLock) {
            this.inputStream.close();
            this.outputStream.close();
            try {
                this.process.destroy();
            } catch (Exception e) {
                // ErrnoException can occur
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }
}
