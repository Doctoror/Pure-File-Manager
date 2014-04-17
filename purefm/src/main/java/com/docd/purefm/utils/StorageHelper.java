package com.docd.purefm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.os.Environment;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import android.support.annotation.NonNull;

/**
 * Provides methods for working with storages
 */
public final class StorageHelper {

    //private static final String TAG = "StorageHelper";

    private StorageHelper() {
    }

    private static final String[] AVOIDED_DEVICES = new String[] {
            "rootfs", "tmpfs", "dvpts", "proc", "sysfs", "none"
    };

    private static final String[] AVOIDED_DIRECTORIES = new String[] {
            "obb", "asec"
    };

    private static final String[] DISALLOWED_FILESYSTEMS = new String[] {
            "tmpfs", "rootfs", "romfs", "devpts", "sysfs", "proc", "cgroup", "debugfs"
    };

    private static final String STORAGES_ROOT;

    static {
        final String primaryStoragePath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        final int index = primaryStoragePath.indexOf(File.separatorChar, 1);
        if (index != -1) {
            STORAGES_ROOT = primaryStoragePath.substring(0, index + 1);
        } else {
            STORAGES_ROOT = File.separator;
        }
    }

    /**
     * Returns a list of all mounted {@link StorageVolume}s
     *
     * @return list of mounted {@link StorageVolume}s
     */
    @NonNull
    public static List<Volume> getAllDevices() {
        final List<Volume> volumeList = new ArrayList<>(20);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = reader.readLine()) != null) {
                final StringTokenizer tokens = new StringTokenizer(line, " ");
                final String device = tokens.nextToken();
                final String path = tokens.nextToken();
                final String fileSystem = tokens.nextToken();

                final File file = new File(path);

                final Volume volume = createVolume(device, file, fileSystem);
                final StringTokenizer flags = new StringTokenizer(tokens.nextToken(), ",");
                while (flags.hasMoreTokens()) {
                    final String token = flags.nextToken();
                    if (token.equals("rw")) {
                        volume.mReadOnly = false;
                        break;
                    } else if (token.equals("ro")) {
                        volume.mReadOnly = true;
                        break;
                    }
                }
                volumeList.add(volume);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return volumeList;
    }

    /**
     * Sets {@link StorageVolume.Type}, removable and emulated flags and adds to
     * volumeList
     *
     * @param volumeList
     *            List to add volume to
     * @param v
     *            volume to add to list
     * @param includeUsb
     *            if false, volume with type {@link StorageVolume.Type#USB} will
     *            not be added
     * @param asFirstItem
     *            if true, adds the volume at the beginning of the volumeList
     */
    private static void setTypeAndAdd(final List<StorageVolume> volumeList,
                                      final StorageVolume v,
                                      final boolean includeUsb,
                                      final boolean asFirstItem) {
        final StorageVolume.Type type = resolveType(v);
        if (includeUsb || type != StorageVolume.Type.USB) {
            v.mType = type;
            if (v.file.equals(Environment.getExternalStorageDirectory())) {
                v.mRemovable = Environment.isExternalStorageRemovable();
            } else {
                v.mRemovable = type != StorageVolume.Type.INTERNAL;
            }
            v.mEmulated = type == StorageVolume.Type.INTERNAL;
            if (asFirstItem) {
                volumeList.add(0, v);
            } else {
                volumeList.add(v);
            }
        }
    }

    /**
     * Resolved {@link StorageVolume} type
     *
     * @param v
     *            {@link StorageVolume} to resolve type for
     * @return {@link StorageVolume} type
     */
    private static StorageVolume.Type resolveType(final StorageVolume v) {
        if (v.file.equals(Environment.getExternalStorageDirectory())
                && Environment.isExternalStorageEmulated()) {
            return StorageVolume.Type.INTERNAL;
        } else if (StringUtils.containsIgnoreCase(v.file.getAbsolutePath(), "usb")) {
            return StorageVolume.Type.USB;
        } else {
            return StorageVolume.Type.EXTERNAL;
        }
    }

    /**
     * Checks whether the array contains object
     *
     * @param array
     *            Array to check
     * @param object
     *            Object to find
     * @return true, if the given array contains the object
     */
    private static <T> boolean arrayContains(T[] array, T object) {
        for (final T item : array) {
            if (item.equals(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the path contains one of the directories
     *
     * For example, if path is /one/two, it returns true input is "one" or
     * "two". Will return false if the input is one of "one/two", "/one" or
     * "/two"
     *
     * @param path
     *            path to check for a directory
     * @param dirs
     *            directories to find
     * @return true, if the path contains one of the directories
     */
    private static boolean pathContainsDir(final String path, final String[] dirs) {
        final StringTokenizer tokens = new StringTokenizer(path, File.separator);
        while (tokens.hasMoreElements()) {
            final String next = tokens.nextToken();
            for (final String dir : dirs) {
                if (next.equals(dir)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    private static StorageHelper.Volume createVolume(
            @NonNull final String device,
            @NonNull final File file,
            @NonNull final String fileSystem) {
        // this approach considers that all storages are mounted in the same non-root directory
        boolean isStorageVolume = !STORAGES_ROOT.equals(File.separator);

        final String path = file.getAbsolutePath();
        if (isStorageVolume && !path.startsWith(STORAGES_ROOT)) {
            isStorageVolume = false;
        }

        if (isStorageVolume && arrayContains(AVOIDED_DEVICES, device)) {
            isStorageVolume = false;
        }


        if (isStorageVolume && pathContainsDir(path, AVOIDED_DIRECTORIES)) {
            isStorageVolume = false;
        }

        // ones with non-storage filesystems
        if (isStorageVolume && arrayContains(DISALLOWED_FILESYSTEMS, fileSystem)) {
            isStorageVolume = false;
        }

        // volumes that are not accessible are not storage volumes
        if (isStorageVolume && !(file.canRead() && file.canExecute())) {
            isStorageVolume = false;
        }
        return isStorageVolume ? new StorageVolume(device, file, fileSystem) :
                new Volume(device, file, fileSystem);
    }

    /**
     * Retrieves {@link StorageVolume} items from volume list
     *
     * @param allVolumes List to get {@link StorageVolume} from
     * @return {@link StorageVolume} list from items of volume list
     */
    @NonNull
    public static List<StorageVolume> getStorageVolumes(@NonNull final List<Volume> allVolumes) {
        final Map<String, List<StorageVolume>> deviceVolumeMap = new HashMap<>();
        for (final Volume v : allVolumes) {
            if (v instanceof StorageVolume) {
                List<StorageVolume> volumes = deviceVolumeMap.get(v.device);
                if (volumes == null) {
                    volumes = new ArrayList<>(3);
                    deviceVolumeMap.put(v.device, volumes);
                }
                volumes.add((StorageVolume) v);
            }
        }

        // remove external storage volumes that are the same devices
        boolean primaryStorageIncluded = false;
        final File externalStorage = Environment.getExternalStorageDirectory();
        final List<StorageVolume> storageVolumeList = new ArrayList<>();
        for (final Entry<String, List<StorageVolume>> entry : deviceVolumeMap.entrySet()) {
            final List<StorageVolume> volumes = entry.getValue();
            if (volumes.size() == 1) {
                // go ahead and add
                final StorageVolume v = volumes.get(0);
                final boolean isPrimaryStorage = v.file.equals(externalStorage);
                primaryStorageIncluded |= isPrimaryStorage;
                setTypeAndAdd(storageVolumeList, v, true, isPrimaryStorage);
                continue;
            }
            final int volumesLength = volumes.size();
            for (int i = 0; i < volumesLength; i++) {
                final StorageVolume v = volumes.get(i);
                if (v.file.equals(externalStorage)) {
                    primaryStorageIncluded = true;
                    // add as external storage and continue
                    setTypeAndAdd(storageVolumeList, v, true, true);
                    break;
                }
                // if that was the last one and it's not the default external
                // storage then add it as is
                if (i == volumesLength - 1) {
                    setTypeAndAdd(storageVolumeList, v, true, false);
                }
            }
        }

        // add primary storage if it was not found
        if (!primaryStorageIncluded) {
            final StorageVolume defaultExternalStorage = new StorageVolume("", externalStorage, "UNKNOWN");
            defaultExternalStorage.mEmulated = Environment.isExternalStorageEmulated();
            defaultExternalStorage.mType =
                    defaultExternalStorage.mEmulated ? StorageVolume.Type.INTERNAL
                            : StorageVolume.Type.EXTERNAL;
            defaultExternalStorage.mRemovable = Environment.isExternalStorageRemovable();
            defaultExternalStorage.mReadOnly =
                    Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            storageVolumeList.add(0, defaultExternalStorage);
        }
        return storageVolumeList;
    }

    /**
     * Longest names first
     */
    public static final Comparator<Volume> VOLUME_PATH_LENGTH_COMPARATOR = new Comparator<Volume>() {
        @Override
        public int compare(Volume lhs, Volume rhs) {
            return rhs.file.getAbsolutePath().length() - lhs.file.getAbsolutePath().length();
        }
    };

    /**
     * Represents Volume from /proc/mounts
     */
    public static class Volume {
        /**
         * Device name
         */
        public final String device;

        /**
         * Points to mount point of this device
         */
        public final File file;

        /**
         * File system of this device
         */
        public final String fileSystem;

        /**
         * if true, the storage is mounted as read-only
         */
        protected boolean mReadOnly;

        Volume(@NonNull final String device,
               @NonNull final File file,
               @NonNull final String fileSystem) {
            this.device = device;
            this.file = file;
            this.fileSystem = fileSystem;
        }

        /**
         * Returns true if this storage is mounted as read-only
         *
         * @return true if this storage is mounted as read-only
         */
        public boolean isReadOnly() {
            return mReadOnly;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + file.hashCode();
            return result;
        }

        /**
         * Returns true if the other object is StorageHelper and it's
         * {@link #file} matches this one's
         *
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StorageVolume other = (StorageVolume) obj;
            return file.equals(other.file);
        }

        @Override
        public String toString() {
            return file.getAbsolutePath() + (mReadOnly ? " ro " : " rw ") + fileSystem;
        }
    }

    /**
     * Represents storage volume information
     */
    public static final class StorageVolume extends Volume {

        /**
         * Represents {@link StorageVolume} type
         */
        public enum Type {
            /**
             * Device built-in internal storage. Probably points to
             * {@link Environment#getExternalStorageDirectory()}
             */
            INTERNAL,

            /**
             * External storage. Probably removable, if no other
             * {@link StorageVolume} of type {@link #INTERNAL} is returned by
             * {@link StorageHelper#getStorageVolumes(List)}, this might be
             * pointing to {@link Environment#getExternalStorageDirectory()}
             */
            EXTERNAL,

            /**
             * Removable usb storage
             */
            USB
        }

        /**
         * If true, the storage is removable
         * Defaults to true since there is no way to determine whether the volume is removable
         * except for {@link android.os.Environment#getExternalStorageDirectory()}
         */
        private boolean mRemovable = true;

        /**
         * If true, the storage is emulated
         */
        private boolean mEmulated;

        /**
         * Type of this storage
         */
        private Type mType;

        StorageVolume(@NonNull final String device,
                      @NonNull final File file,
                      @NonNull final String fileSystem) {
            super(device, file, fileSystem);
        }

        /**
         * Returns type of this storage
         *
         * @return Type of this storage
         */
        public Type getType() {
            return mType;
        }

        /**
         * Returns true if this storage is removable
         *
         * @return true if this storage is removable
         */
        public boolean isRemovable() {
            return mRemovable;
        }

        /**
         * Returns true if this storage is emulated
         *
         * @return true if this storage is emulated
         */
        public boolean isEmulated() {
            return mEmulated;
        }

        @Override
        public String toString() {
            return super.toString() + " " + mType + (mRemovable ? " R " : "")
                    + (mEmulated ? " E " : "");
        }
    }
}