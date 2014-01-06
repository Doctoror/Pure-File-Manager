package com.docd.purefm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public final class ActivityMonitor
{
    private static final long DELAY_CLOSED = 300;
    
    private static Handler handler;
    private static int opened;
    
    public interface OnActivitiesOpenedListener {
        void onActivitiesOpen();
        void onActivitiesClosed();
    }
    
    private static List<OnActivitiesOpenedListener> listeners;
    
    static {
        listeners = new ArrayList<OnActivitiesOpenedListener>();
    }
    
    public static void addOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        listeners.add(l);
    }
    
    public static void removeOnActivitiesOpenedListener(OnActivitiesOpenedListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        listeners.remove(l);
    }
    
    public static void init(final Context context) {
        handler = new Handler();
        new LooperThread().start();
    }
    
    public static boolean areActivitiesOpened() {
        return opened != 0;
    }
        
    private static final class LooperThread extends Thread {
        
        protected static Handler handler;
        
        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            Looper.loop();
        }
    }
    
    private static final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                listeners.get(i).onActivitiesClosed();
            }
        }
    };
    
    public static synchronized void onStart(Activity a) {
        if (handler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        handler.removeCallbacksAndMessages(stopRunnable);
        if (LooperThread.handler != null) {
            LooperThread.handler.removeCallbacksAndMessages(stopRunnable);
        }
        opened++;
        final int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).onActivitiesOpen();
        }
    }
    
    public static synchronized void onStop(Activity a) {
        if (handler == null) {
            throw new IllegalStateException("ActivityMonitor was not initialized");
        }
        handler.removeCallbacksAndMessages(stopRunnable);
        if (LooperThread.handler != null) {
            LooperThread.handler.removeCallbacksAndMessages(stopRunnable);
        }
        opened--;
        if (opened < 0) {
            throw new IllegalStateException("Number of opened activities is less than zero. Check whether you call onStart in all activities");
        }
        if (opened == 0) {
            if (LooperThread.handler != null) {
                LooperThread.handler.postDelayed(stopRunnable, DELAY_CLOSED);
            } else {
                handler.postDelayed(stopRunnable, DELAY_CLOSED);
            }
        }
    }
}