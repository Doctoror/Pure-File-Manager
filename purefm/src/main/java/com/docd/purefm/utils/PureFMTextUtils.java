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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateFormat;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.text.style.DashSpan;

import org.jetbrains.annotations.NotNull;

public final class PureFMTextUtils {

    private PureFMTextUtils() {}
    
    private static SimpleDateFormat format;
    private static final Calendar CALENDAR = Calendar.getInstance(
            TimeZone.getDefault(), Locale.getDefault());
    
    public static void init(Context context) {
        if (DateFormat.is24HourFormat(context)) {
            format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        } else {
            format = new SimpleDateFormat("dd/MM/yyyy KK:mm aa", Locale.getDefault());
        }
    }

    @NotNull
    public static synchronized String humanReadableDate(
            final long date, final boolean isUtc) {
        if (isUtc) {
            final long offset = -(CALENDAR.get(Calendar.ZONE_OFFSET) + CALENDAR.get(Calendar.DST_OFFSET));
            return format.format(date - offset);
        } else {
            return format.format(date);
        }
    }
    
    public static int stringMonthToInt(@NotNull final String month) {
        switch (month) {
            case "Jan":
                return Calendar.JANUARY;

            case "Feb":
                return Calendar.FEBRUARY;

            case "Mar":
                return Calendar.MARCH;

            case "Apr":
                return Calendar.APRIL;

            case "May":
                return Calendar.MAY;

            case "Jun":
                return Calendar.JUNE;

            case "Jul":
                return Calendar.JULY;

            case "Aug":
                return Calendar.AUGUST;

            case "Sep":
                return Calendar.SEPTEMBER;

            case "Oct":
                return Calendar.OCTOBER;

            case "Nov":
                return Calendar.NOVEMBER;

            case "Dec":
                return Calendar.DECEMBER;

            default:
                return 0;
        }
    }

    @NotNull
    public static SpannableString fileListToDashList(@NotNull final Iterable<GenericFile> files) {
        final StringBuilder fileList = new StringBuilder(66);
        for (final GenericFile file : files) {
            fileList.append(file.getName());
            fileList.append('\n');
        }
        // remove last '\n'
        fileList.deleteCharAt(fileList.length() - 1);
        final SpannableString ss = new SpannableString(fileList.toString());
        ss.setSpan(new DashSpan(), 0, ss.length(), 0);
        return ss;
    }
}
