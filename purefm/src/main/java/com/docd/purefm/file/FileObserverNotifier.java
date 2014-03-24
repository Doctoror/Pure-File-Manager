package com.docd.purefm.file;

import android.os.FileObserver;

import java.util.Collection;

/**
 * Used to create and fire events for {@link android.os.FileObserver}
 * This is necessary because in locations where superuser access is needed
 * the FileObserver can't deliver results
 */
public final class FileObserverNotifier {

    private FileObserverNotifier() {

    }

    public static void notifyCreated(final GenericFile created) {
        final GenericFile path;
        if (created.isDirectory()) {
            path = created;
        } else {
            path = created.getParentFile();
        }
        if (path != null) {
            final MultiListenerFileObserver observer = FileObserverCache.getInstance().get(path);
            if (observer != null) {
                observer.onEvent(FileObserver.CREATE, observer.getPath());
            }
        }
    }

    public static void notifyDeleted(final GenericFile deleted) {
        final MultiListenerFileObserver observer = FileObserverCache.getInstance().get(deleted);
        if (observer != null) {
            observer.onEvent(FileObserver.DELETE_SELF, observer.getPath());
        }
    }

    public static void notifyDeleted(final Collection<GenericFile> deleted) {
        final FileObserverCache cache = FileObserverCache.getInstance();
        for (final GenericFile deletedFile : deleted) {
            final MultiListenerFileObserver observer = cache.get(deletedFile);
            if (observer != null) {
                observer.onEvent(FileObserver.DELETE_SELF, observer.getPath());
            }
        }
    }
}
