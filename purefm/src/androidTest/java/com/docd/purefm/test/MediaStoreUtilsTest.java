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
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.AndroidTestCase;
import android.util.Pair;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.file.JavaFile;
import com.docd.purefm.utils.MediaStoreUtils;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link com.docd.purefm.utils.MediaStoreUtils}
 */
public final class MediaStoreUtilsTest extends AndroidTestCase {

    private static final File EXT = Environment.getExternalStorageDirectory();
    private static final File TEST_ROOT = new File(EXT, "MediaStoreUtilsTest");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertTrue(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));

        TEST_ROOT.mkdir();
        assertTrue(TEST_ROOT.exists());
        assertTrue(TEST_ROOT.isDirectory());
        doTearDown();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        final ContentResolver resolver = getContext().getContentResolver();
        testAddAndDeleteFile(resolver);
        testRenameFile(resolver);
        testRenameEmptyDirectory(resolver);
        testMoveFile(resolver);
    }

    private void testAddAndDeleteFile(@NotNull final ContentResolver resolver) throws Throwable {
        final GenericFile test = new JavaFile(TEST_ROOT, "test1.txt");
        assertFalse(isFileInMediaStore(resolver, test));
        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test);
        assertTrue(isFileInMediaStore(resolver, test));
        MediaStoreUtils.deleteFileOrDirectory(resolver, test);
        assertFalse(isFileInMediaStore(resolver, test));
    }

    private void testRenameFile(@NotNull final ContentResolver resolver) throws Throwable {
        final GenericFile test1 = new JavaFile(TEST_ROOT, "test2.txt");
        final GenericFile test2 = new JavaFile(TEST_ROOT, "test3.txt");
        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test1);
        assertTrue(isFileInMediaStore(resolver, test1));
        MediaStoreUtils.renameFileOrDirectory(resolver, test1, test2);
        assertFalse(isFileInMediaStore(resolver, test1));
        assertTrue(isFileInMediaStore(resolver, test2));

        MediaStoreUtils.deleteFileOrDirectory(resolver, test2);
        assertFalse(isFileInMediaStore(resolver, test2));
    }

    private void testRenameEmptyDirectory(@NotNull final ContentResolver resolver) throws Throwable {
        final GenericFile test1 = new JavaFile(TEST_ROOT, "test4dir.txt");
        final GenericFile test2 = new JavaFile(TEST_ROOT, "test5dir.txt");

        test1.mkdir();
        assertTrue(test1.exists());
        assertTrue(test1.isDirectory());

        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test1);
        assertTrue(isFileInMediaStore(resolver, test1));

        MediaStoreUtils.renameFileOrDirectory(resolver, test1, test2);
        assertFalse(isFileInMediaStore(resolver, test1));
        assertTrue(isFileInMediaStore(resolver, test2));

        assertTrue(test1.delete());
        assertTrue(test2.delete());

        MediaStoreUtils.deleteFileOrDirectory(resolver, test2);
        assertFalse(isFileInMediaStore(resolver, test2));
    }

    private void testMoveFile(@NotNull final ContentResolver resolver) throws Throwable {
        final GenericFile test1dir = new JavaFile(TEST_ROOT, "test6.txt");
        final GenericFile test2dir = new JavaFile(TEST_ROOT, "test7.txt");
        test1dir.mkdir();
        assertTrue(test1dir.exists());
        assertTrue(test1dir.isDirectory());

        test2dir.mkdir();
        assertTrue(test2dir.exists());
        assertTrue(test2dir.isDirectory());

        final GenericFile test1file = new JavaFile(test1dir.toFile(), "test8.txt");
        final GenericFile test2file = new JavaFile(test2dir.toFile(), "test8.txt");
        test1file.createNewFile();
        assertTrue(test1file.exists());
        assertFalse(test1file.isDirectory());

        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test1dir);
        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test2dir);
        MediaStoreUtils.addEmptyFileOrDirectory(resolver, test1file);

        assertTrue(isFileInMediaStore(resolver, test1dir));
        assertTrue(isFileInMediaStore(resolver, test2dir));
        assertTrue(isFileInMediaStore(resolver, test1file));


        FileUtils.moveFile(test1file.toFile(), test2file.toFile());
        final List<Pair<GenericFile, GenericFile>> files = new ArrayList<>(1);
        files.add(new Pair<>(test1file, test2file));
        MediaStoreUtils.moveFiles(getContext(), files);

        assertTrue(isFileInMediaStore(resolver, test1dir));
        assertTrue(isFileInMediaStore(resolver, test2dir));
        assertFalse(isFileInMediaStore(resolver, test1file));
        assertTrue(isFileInMediaStore(resolver, test2file));

        test2file.delete();
        test1dir.delete();
        test2dir.delete();

        MediaStoreUtils.deleteFileOrDirectory(resolver, test2file);
        MediaStoreUtils.deleteFileOrDirectory(resolver, test1dir);
        MediaStoreUtils.deleteFileOrDirectory(resolver, test2dir);

        assertFalse(isFileInMediaStore(resolver, test1dir));
        assertFalse(isFileInMediaStore(resolver, test2dir));
        assertFalse(isFileInMediaStore(resolver, test2file));
    }


    private static boolean isFileInMediaStore(final ContentResolver resolver, final GenericFile file) {
        final Uri uri = MediaStoreUtils.getContentUri(file);
        final Pair<String, String[]> selection = MediaStoreUtils.dataSelection(file.toFile());
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
