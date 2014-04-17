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
package com.docd.purefm.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.webkit.MimeTypeMap;

import com.docd.purefm.R;

public final class MimeTypes {

    private MimeTypes() {}

    @NonNull
    public static final String ALL_MIME_TYPES = "*/*";

    @NonNull
    private static final HashMap<String, Integer> EXT_ICONS = new HashMap<>();
    
    /**
     * This is not a replacement for libcore. This is an addition
     */
    @NonNull
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    static {
        // BINARY
        EXT_ICONS.put("a",     R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("bin",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("class", R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("com",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("dex",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("dump",  R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("exe",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("dat",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("dll",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("lib",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("o",     R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("obj",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("pyc",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("pyo",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("ser",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("swf",   R.drawable.ic_fso_type_binary);
        EXT_ICONS.put("so",    R.drawable.ic_fso_type_binary);
        
        EXT_ICONS.put("dmg",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("cue",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("img",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("iso",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("msd",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("nrg",   R.drawable.ic_fso_type_cdimage);
        EXT_ICONS.put("uif",   R.drawable.ic_fso_type_cdimage);
        
        // TEXT
        EXT_ICONS.put("conf",       R.drawable.ic_fso_type_text);
        EXT_ICONS.put("csv",        R.drawable.ic_fso_type_text);
        EXT_ICONS.put("diff",       R.drawable.ic_fso_type_text);
        EXT_ICONS.put("in",         R.drawable.ic_fso_type_text);
        EXT_ICONS.put("list",       R.drawable.ic_fso_type_text);
        EXT_ICONS.put("log",        R.drawable.ic_fso_type_text);
        EXT_ICONS.put("prop",       R.drawable.ic_fso_type_text);
        EXT_ICONS.put("properties", R.drawable.ic_fso_type_text);
        EXT_ICONS.put("rc",         R.drawable.ic_fso_type_text);
        EXT_ICONS.put("text",       R.drawable.ic_fso_type_text);
        EXT_ICONS.put("txt",        R.drawable.ic_fso_type_text);
        EXT_ICONS.put("tsv",        R.drawable.ic_fso_type_text);
        
        EXT_ICONS.put("dtd",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("htm",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("html", R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("mht",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("mhtml",R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("mxml", R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("sgm",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("sgml", R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("wsdl", R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("xht",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("xhtml",R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("xml",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("xsl",  R.drawable.ic_fso_type_markup_document);
        EXT_ICONS.put("xslt", R.drawable.ic_fso_type_markup_document);
        
        // DOCUMENT
        EXT_ICONS.put("doc",  R.drawable.ic_fso_type_document);
        EXT_ICONS.put("docx", R.drawable.ic_fso_type_document);
        EXT_ICONS.put("odp",  R.drawable.ic_fso_type_document);
        EXT_ICONS.put("odt",  R.drawable.ic_fso_type_document);
        EXT_ICONS.put("rtf",  R.drawable.ic_fso_type_document);
        
        EXT_ICONS.put("fdf",  R.drawable.ic_fso_type_pdf);
        EXT_ICONS.put("pdf",  R.drawable.ic_fso_type_pdf);
        
        EXT_ICONS.put("ppt",  R.drawable.ic_fso_type_presentation);
        EXT_ICONS.put("pptx", R.drawable.ic_fso_type_presentation);
        
        EXT_ICONS.put("ods",  R.drawable.ic_fso_type_spreadsheet);
        EXT_ICONS.put("xls",  R.drawable.ic_fso_type_spreadsheet);
        EXT_ICONS.put("xlsx", R.drawable.ic_fso_type_spreadsheet);
        
        // e-Book
        EXT_ICONS.put("azv",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("djv",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("djvu",  R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("epub",  R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("kf8",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("lit",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("lrf",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("lrx",   R.drawable.ic_fso_type_ebook);
        EXT_ICONS.put("ibooks",R.drawable.ic_fso_type_ebook);
        
        // Internet document
        EXT_ICONS.put("ics",  R.drawable.ic_fso_type_calendar);
        EXT_ICONS.put("ifb",  R.drawable.ic_fso_type_calendar);
        EXT_ICONS.put("vcs",  R.drawable.ic_fso_type_calendar);
        
        EXT_ICONS.put("eml",  R.drawable.ic_fso_type_email);
        EXT_ICONS.put("msg",  R.drawable.ic_fso_type_email);
        
        EXT_ICONS.put("vcf",  R.drawable.ic_fso_type_contact);
        
        // Compress
        EXT_ICONS.put("ace",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("bz",   R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("bz2",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("cab",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("cpio", R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("gz",   R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("lha",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("lrf",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("lzma", R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("jar",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("rar",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("tar",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("tgz",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("xz",   R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("zip",  R.drawable.ic_fso_type_compress);
        EXT_ICONS.put("Z",    R.drawable.ic_fso_type_compress);
        
        // Executable
        EXT_ICONS.put("bar", R.drawable.ic_fso_type_shell);
        EXT_ICONS.put("csh", R.drawable.ic_fso_type_shell);
        EXT_ICONS.put("ksh", R.drawable.ic_fso_type_shell);
        EXT_ICONS.put("sh",  R.drawable.ic_fso_type_shell);
        
        // Database
        EXT_ICONS.put("db",  R.drawable.ic_fso_type_database);
        EXT_ICONS.put("db3", R.drawable.ic_fso_type_database);
        EXT_ICONS.put("mdb", R.drawable.ic_fso_type_database);
        
        //Font
        EXT_ICONS.put("otf", R.drawable.ic_fso_type_font);
        EXT_ICONS.put("ttf", R.drawable.ic_fso_type_font);
        EXT_ICONS.put("gsf", R.drawable.ic_fso_type_font);
        EXT_ICONS.put("psf", R.drawable.ic_fso_type_font);
        
        //Image
        EXT_ICONS.put("bmp",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("cgm",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("g3",   R.drawable.ic_fso_type_image);
        EXT_ICONS.put("gif",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("ief",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("jpe",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("jpeg", R.drawable.ic_fso_type_image);
        EXT_ICONS.put("jpg",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("png",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("btif", R.drawable.ic_fso_type_image);
        EXT_ICONS.put("svg",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("svgz", R.drawable.ic_fso_type_image);
        EXT_ICONS.put("tif",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("tiff", R.drawable.ic_fso_type_image);
        EXT_ICONS.put("psd",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("dwg",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("dxf",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("fbs",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("fpx",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("fst",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("mmr",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("rlc",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("mdi",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("npx",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("wbmp", R.drawable.ic_fso_type_image);
        EXT_ICONS.put("xif",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("ras",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("ico",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("pcx",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("pct",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("pic",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("xbm",  R.drawable.ic_fso_type_image);
        EXT_ICONS.put("xwd",  R.drawable.ic_fso_type_image);
        
        // Audio
        
        EXT_ICONS.put("aac",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("adp",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("aif",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("aifc", R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("aiff", R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("amr",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("ape",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("au",   R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("dts",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("eol",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("flac", R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("kar",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("lvp",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("m2a",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("m3a",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("m3u",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("m4a",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mid",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mid",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mka",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mp2",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mp3",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("mpga", R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("oga",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("ogg",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("pya",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("ram",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("rmi",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("snd",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("spx",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("wav",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("wax",  R.drawable.ic_fso_type_audio);
        EXT_ICONS.put("wma",  R.drawable.ic_fso_type_audio);
        
        // Video
        EXT_ICONS.put("3gp",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("3gpp", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("3g2",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("3gpp2",R.drawable.ic_fso_type_video);
        EXT_ICONS.put("h261", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("h263", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("h264", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("jpgv", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("jpgm", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("jpm",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mj2",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mp4",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mp4v", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mpg4", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("m1v",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("m2v",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mpa",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mpe",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mpg",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mpeg", R.drawable.ic_fso_type_video);
        EXT_ICONS.put("ogv",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mov",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("qt",   R.drawable.ic_fso_type_video);
        EXT_ICONS.put("fvt",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("m4u",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("pyv",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("viv",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("f4v",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("fli",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("flv",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("m4v",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("asf",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("asx",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("avi",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("wmv",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("wmx",  R.drawable.ic_fso_type_video);
        EXT_ICONS.put("mkv",  R.drawable.ic_fso_type_video);
        
        //Application
        
        EXT_ICONS.put("apk", R.drawable.ic_fso_type_app);
        
        
        /*
         *  ================= MIME TYPES ====================
         */
        
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        
        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");
        
        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");
        
        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");
        
        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");
        
        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");
        
        MIME_TYPES.put("otf", "x-font-otf");
        MIME_TYPES.put("ttf", "x-font-ttf");
        MIME_TYPES.put("psf", "x-font-linux-psf");
        
        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");
        
        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");
        
        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");
        
    }
    
    public static int getIconForExt(String ext) {
        final Integer res = EXT_ICONS.get(ext);
        return res == null ? 0 : res.intValue();
    }
    
    public static int getTypeIcon(File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        if (extension != null && !extension.isEmpty()) {
            return getIconForExt(extension);
        }
        return 0;
    }

    /**
     * Returns mime type from a File based on a file name
     *
     * @param file File to get mime type for
     * @return mime type of the file or null if file is directory or has unknown mime type
     */
    @Nullable
    public static String getMimeType(@NonNull final File file) {
        if (file.isDirectory()) {
            return null;
        }
        return getMimeType(file.getAbsolutePath());
    }

    /**
     * It is impossible to detect whether path is a directory for sure.
     * Call this only if you sure the path does not point to a directory
     *
     * @param path File path
     * @return mime type based on file extension
     */
    @Nullable
    public static String getMimeType(@NonNull final String path) {
        if (path.endsWith(File.separator)) {
            return null;
        }
        String type = null;
        final String extension = FilenameUtils.getExtension(FilenameUtils.getName(path));
        if (extension != null && !extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase(Locale.US);
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        return type;
    }
    
    public static boolean mimeTypeMatch(@NonNull final String mime, @NonNull final String input) {
        return Pattern.matches(mime.replace("*", ".*"), input);
    }

    public static boolean isPicture(@NonNull final File f) {
        final String mime = getMimeType(f);
        return mime != null && mimeTypeMatch("image/*", mime);
    }

    public static boolean isAudio(@NonNull final File f) {
        final String mime = getMimeType(f);
        return mime != null && mimeTypeMatch("audio/*", mime);
    }
    
    public static boolean isVideo(@NonNull final File f) {
        final String mime = getMimeType(f);
        return mime != null && mimeTypeMatch("video/*", mime);
    }
}
