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
package com.docd.purefm.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.AndroidTestCase;
import android.util.Pair;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.MediaStoreUtils;
import com.docd.purefm.utils.PureFMFileUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link com.docd.purefm.utils.MediaStoreUtils}
 */
public final class MediaStoreUtilsTest extends AndroidTestCase {

    private static final File EXT = Environment.getExternalStorageDirectory();
    private static final File TEST_ROOT = new File(EXT, "MediaStoreUtilsTest");
    private static final File TEST_DIR_1 = new File(TEST_ROOT, "dir one");
    private static final File TEST_DIR_2 = new File(TEST_ROOT, "dir two");
    private static final File TEST_DIR_3 = new File(TEST_ROOT, "dir three");
    private static final GenericFile TEST1 = new JavaFile(TEST_DIR_1, "image1.jpg");
    private static final GenericFile TEST2 = new JavaFile(TEST_DIR_1, "image2.jpg");
    private static final GenericFile TEST3 = new JavaFile(TEST_DIR_1, "image3.jpg");

    private static final GenericFile TEST4 = new JavaFile(TEST_DIR_2, "image1.jpg");
    private static final GenericFile TEST5 = new JavaFile(TEST_DIR_2, "image2.jpg");
    private static final GenericFile TEST6 = new JavaFile(TEST_DIR_2, "image3.jpg");

    private static final GenericFile TEST7 = new JavaFile(TEST_DIR_3, "image1.jpg");
    private static final GenericFile TEST8 = new JavaFile(TEST_DIR_3, "image2.jpg");
    private static final GenericFile TEST9 = new JavaFile(TEST_DIR_3, "image3.jpg");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        doTearDown();

        TEST_ROOT.mkdirs();
        TEST_DIR_1.mkdir();
        TEST_DIR_2.mkdir();
        TEST_DIR_3.mkdir();
        TEST1.createNewFile();

        assertTrue(TEST_ROOT.exists());
        assertTrue(TEST_DIR_1.exists());
        assertTrue(TEST_DIR_2.exists());
        assertTrue(TEST_DIR_3.exists());
        assertTrue(TEST1.exists());
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        // add test file to MediaStore
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        addToMediaStoreAsImage(resolver, TEST1);

        // check if test files were added
        assertTrue(isFileInMediaStore(resolver, TEST1));

        //test rename
        assertTrue(TEST1.renameTo(TEST2));
        MediaStoreUtils.renameFile(resolver, TEST1, TEST2);
        assertFalse(isFileInMediaStore(resolver, TEST1));
        assertTrue(isFileInMediaStore(resolver, TEST2));

        //test move to same dir
        FileUtils.moveFile(TEST2.toFile(), TEST1.toFile());

        final List<Pair<GenericFile, GenericFile>> moved = new ArrayList<>(1);
        moved.add(new Pair<>(TEST2, TEST1));
        MediaStoreUtils.moveFiles(context, moved);
        moved.clear();
        assertFalse(isFileInMediaStore(resolver, TEST2));
        assertTrue(isFileInMediaStore(resolver, TEST1));

        //test copy to same dir
        FileUtils.copyFile(TEST1.toFile(), TEST2.toFile());
        FileUtils.copyFile(TEST1.toFile(), TEST3.toFile());

        moved.add(new Pair<>(TEST1, TEST2));
        moved.add(new Pair<>(TEST1, TEST3));
        MediaStoreUtils.copyFiles(context, moved);
        moved.clear();

        assertTrue(isFileInMediaStore(resolver, TEST1));
        assertTrue(isFileInMediaStore(resolver, TEST2));
        assertTrue(isFileInMediaStore(resolver, TEST3));

        //test copy to another dir
        FileUtils.copyFile(TEST1.toFile(), TEST4.toFile());
        FileUtils.copyFile(TEST2.toFile(), TEST5.toFile());
        FileUtils.copyFile(TEST3.toFile(), TEST6.toFile());

        moved.add(new Pair<>(TEST1, TEST4));
        moved.add(new Pair<>(TEST2, TEST5));
        moved.add(new Pair<>(TEST3, TEST6));
        MediaStoreUtils.copyFiles(context, moved);
        moved.clear();

        assertTrue(isFileInMediaStore(resolver, TEST1));
        assertTrue(isFileInMediaStore(resolver, TEST2));
        assertTrue(isFileInMediaStore(resolver, TEST3));
        assertTrue(isFileInMediaStore(resolver, TEST4));
        assertTrue(isFileInMediaStore(resolver, TEST5));
        assertTrue(isFileInMediaStore(resolver, TEST6));

        //test move to another dir
        FileUtils.moveFile(TEST4.toFile(), TEST7.toFile());
        FileUtils.moveFile(TEST5.toFile(), TEST8.toFile());
        FileUtils.moveFile(TEST6.toFile(), TEST9.toFile());

        moved.add(new Pair<>(TEST4, TEST7));
        moved.add(new Pair<>(TEST5, TEST8));
        moved.add(new Pair<>(TEST6, TEST9));
        MediaStoreUtils.moveFiles(context, moved);
        moved.clear();

        assertFalse(isFileInMediaStore(resolver, TEST4));
        assertFalse(isFileInMediaStore(resolver, TEST5));
        assertFalse(isFileInMediaStore(resolver, TEST6));
        assertTrue(isFileInMediaStore(resolver, TEST7));
        assertTrue(isFileInMediaStore(resolver, TEST8));
        assertTrue(isFileInMediaStore(resolver, TEST9));

        //test batch delete

        final List<GenericFile> files = new ArrayList<>();
        files.add(TEST7);
        files.add(TEST8);
        assertTrue(TEST7.delete());
        assertTrue(TEST8.delete());
        MediaStoreUtils.deleteFilesOrDirectories(resolver, files);

        assertFalse(isFileInMediaStore(resolver, TEST7));
        assertFalse(isFileInMediaStore(resolver, TEST8));

        assertTrue(TEST9.delete());
        MediaStoreUtils.deleteFile(resolver, TEST9);

        assertFalse(isFileInMediaStore(resolver, TEST9));

        //test add file or dir
        final GenericFile dir2JavaFile = new JavaFile(TEST_DIR_2);
        MediaStoreUtils.addFileOrDirectory(resolver, dir2JavaFile);
        MediaStoreUtils.addFileOrDirectory(resolver, TEST4);
        MediaStoreUtils.addFileOrDirectory(resolver, TEST5);
        MediaStoreUtils.addFileOrDirectory(resolver, TEST6);

        assertTrue(isFileInMediaStore(resolver, dir2JavaFile));
        assertTrue(isFileInMediaStore(resolver, TEST4));
        assertTrue(isFileInMediaStore(resolver, TEST5));
        assertTrue(isFileInMediaStore(resolver, TEST6));

        // test delete directory with contents
        MediaStoreUtils.deleteAllFromDirectory(resolver, dir2JavaFile);
        assertFalse(isFileInMediaStore(resolver, dir2JavaFile));
        assertFalse(isFileInMediaStore(resolver, TEST4));
        assertFalse(isFileInMediaStore(resolver, TEST5));
        assertFalse(isFileInMediaStore(resolver, TEST6));
    }

    private static void addToMediaStoreAsImage(final ContentResolver resolver, final GenericFile file) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Files.FileColumns.DATA, PureFMFileUtils.fullPath(file));
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Files.FileColumns.MIME_TYPE, "image/jpeg");
        resolver.insert(MediaStore.Files.getContentUri("external"), values);
    }

    private static boolean isFileInMediaStore(final ContentResolver resolver, final GenericFile file) {
        final Uri uri = MediaStoreUtils.getContentUri(file);
        final Pair<String, String[]> selection = MediaStoreUtils.dataSelection(file);
        final Cursor c = resolver.query(uri, new String[] {MediaStore.Files.FileColumns._ID},
                selection.first, selection.second, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0) != 0;
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        doTearDown();
    }

    private void doTearDown() throws Exception {
        if (TEST_ROOT.exists()) {
            FileUtils.forceDelete(TEST_ROOT);
        }
        MediaStoreUtils.deleteAllFromDirectory(getContext().getContentResolver(), new JavaFile(TEST_ROOT));
    }
}
