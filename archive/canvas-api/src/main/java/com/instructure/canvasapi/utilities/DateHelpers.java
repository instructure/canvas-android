/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.utilities;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateHelpers {

    /**
     * Date Format Standards for Android
     * SHORT: 12/31/2000 or 1/3/2000
     * MEDIUM: Jan 3, 2000
     * LONG: Monday, January 3, 2000
     */

    public static Format getPreferredDateFormat(Context context) {
        if(context == null) {
            return null;
        }
        return android.text.format.DateFormat.getMediumDateFormat(context);
    }

    public static Format getShortDateFormat(Context context){
        if(context == null) {
            return null;
        }
        return android.text.format.DateFormat.getDateFormat(context);
    }

    public static String getShortDate(Context context, Date date){
        if(context == null) {
            return null;
        }
        Format format = getShortDateFormat(context);
        String sFormat = format.format(date.getTime());
        return sFormat;
    }

    public static String getFormattedDate(Context context, Date date) {
        if(context == null) {
            return null;
        }
        Format format = getPreferredDateFormat(context);
        return format.format(date.getTime());
    }

    /**
     * @param context
     * @param date
     * @return first 3 letters of month with day of the month if >24hr ago, else time of day
     */
    public static String getDayMonthDateString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        Format format = getDayMonthDateFormat(context);
        return format.format(date.getTime());
    }

    /**
     * @param context
     * @param date
     * @return Month/Day/Year
     */
    public static String getMonthDayYearShortDateString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        Format format = getMonthDayYearShortFormat(context);
        return format.format(date.getTime());
    }

    public static String getDayMonthDateStringUniversal(Context context, Date date) {
        if(context == null) {
            return null;
        }
        Format format = getDayMonthDateFormatUniversal(context);
        return format.format(date.getTime());
    }

    public static String getMessageDateString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        if (!DateUtils.isToday(date.getTime())) {
            return DateUtils.getRelativeTimeSpanString(context, date.getTime()).toString() + ", " + getPreferredTimeFormat(context).format(date);
        }
        return DateUtils.getRelativeTimeSpanString(context, date.getTime()).toString();
    }
    /**
     * @param context
     * @param date
     * @return abbreviated day format and time, with an '@' symbol if >24hr ago, otherwise time of day
     *         example: "Mon @ 3:12pm"
     */
    public static String getDayHourDateString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        Format format = getDayAbbreviationFormat(context);
        return format.format(date.getTime());
    }

    public static SimpleDateFormat getPreferredTimeFormat(Context context) {
        if(DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
        return new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    public static SimpleDateFormat getDayMonthDateFormat(Context context) {
        if(DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
        return new SimpleDateFormat("MMM d", Locale.getDefault());
    }

    public static SimpleDateFormat getMonthDayYearShortFormat(Context context) {
        return new SimpleDateFormat("M/d/yy", Locale.getDefault());
    }

    public static SimpleDateFormat getDayMonthDateFormatUniversal(Context context) {
        return new SimpleDateFormat("MMM d", Locale.getDefault());
    }

    public static SimpleDateFormat getDayAbbreviationFormat(Context context) {
        if (DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault());
        }
    }

    public static SimpleDateFormat getDayMonthTimeAbbreviationFormat(Context context) {
        if(DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
        return new SimpleDateFormat("MMM dd, h:mma", Locale.getDefault());
    }

    public static String getFormattedTime(Context context, Date date) {
        if(context == null) {
            return null;
        }
        return DateHelpers.getPreferredTimeFormat(context).format(date);
    }

    public static String createPrefixedDateString(Context context, String prefix, Date date) {
        if(context == null) {
            return null;
        }
        return prefix + ": " + getFormattedDate(context, date);
    }

    public static String createPrefixedDateString(Context context, int prefixResId,Date date) {
        if(context == null) {
            return null;
        }
        return createPrefixedDateString(context, context.getResources().getString(prefixResId), date);
    }

    public static String createPrefixedShortDateString(Context context, int prefixResId, Date date) {
        if(context == null) {
            return null;
        }
        return context.getString(prefixResId) + ": " + getDayMonthDateString(context, date);
    }

    public static String createPrefixedTimeString(Context context, String prefix, Date date) {
        if(context == null) {
            return null;
        }
        return prefix + ": " + getFormattedTime(context, date);
    }

    public static String createPrefixedTimeString(Context context, int prefixResId, Date date) {
        if(context == null) {
            return null;
        }
        return createPrefixedTimeString(context, context.getResources().getString(prefixResId), date);
    }

    public static String createPrefixedDateTimeString(Context context, String prefix, Date date) {
        if(context == null) {
            return null;
        }
        return prefix + ": " + DateHelpers.getFormattedDate(context, date) + " " + getFormattedTime(context, date);
    }

    public static String createPrefixedDateTimeString(Context context, int prefixResId, Date date) {
        if(context == null) {
            return null;
        }
        return createPrefixedDateTimeString(context, context.getResources().getString(prefixResId), date);
    }

    public static String getDateTimeString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        return getFormattedDate(context, date) + " " + getFormattedTime(context, date);
    }

    public static String getShortDateTimeString(Context context, Date date) {
        if(context == null) {
            return null;
        }
        return getDayMonthDateString(context, date) + " " + getFormattedTime(context, date);
    }

    /**
     * Behaves the same regardless of 24hr format
     * @param context
     * @param date
     * @return
     */
    public static String getShortDateTimeStringUniversal(Context context, Date date) {
        if(context == null) {
            return null;
        }
        return getDayMonthDateStringUniversal(context, date) + " " + getFormattedTime(context, date);
    }
}
