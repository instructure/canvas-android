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

package com.instructure.canvasapi2.utils;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class DateHelper {

    public static Date stringToDate(final String iso8601string) {
        try {
            String s = iso8601string.replace("Z", "+00:00");
            s = s.substring(0, 22) + s.substring(23);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDateWithMillis(final String iso8601string) {
        try {
            String s = iso8601string.replace("Z", "+00:00");
            s = s.substring(0, 22) + s.substring(23);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Source: https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/src-html/org/apache/commons/lang/time/DateUtils.html#line.196
     * @param cal1
     * @param cal2
     * @return a boolean
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTimeInMillis(date1.getTime());

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTimeInMillis(date2.getTime());

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static int compareDays(Calendar cal1, Calendar cal2) {
        return ((Integer)cal1.get(Calendar.DAY_OF_WEEK)).compareTo(cal2.get(Calendar.DAY_OF_WEEK));
    }

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

    @Nullable
    public static String getFormattedDate(Context context, Date date) {
        if(context == null || date == null) {
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

    @NonNull
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

    public static SimpleDateFormat getDayAbbreviationFormat(Context context) {
        if (DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
            return new SimpleDateFormat("h:mm a", Locale.getDefault());
        }
    }

    public static SimpleDateFormat getTimeNoMinutesFormat(Context context) {
        if (DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH", Locale.getDefault());
        } else {
            return new SimpleDateFormat("hha", Locale.getDefault());
        }
    }

    public static SimpleDateFormat getDayMonthTimeAbbreviationFormat(Context context) {
        if(DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
        return new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
    }


    public static SimpleDateFormat getFullDayAndMonthFormat() {
        return new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
    }

    public static SimpleDateFormat getFullDayFormat() {
        return new SimpleDateFormat("EEEE,", Locale.getDefault());
    }

    public static SimpleDateFormat getFullMonthAndDateFormat() {
        return new SimpleDateFormat("MMMM dd", Locale.getDefault());
    }

    @NonNull
    public static SimpleDateFormat getFullMonthNoLeadingZeroDateFormat() {
        return new SimpleDateFormat("MMMM d", Locale.getDefault());
    }

    public static SimpleDateFormat getDayMonthYearFormat() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    public static SimpleDateFormat getShortDayFormat() {
        return new SimpleDateFormat("EEE", Locale.getDefault());
    }

    public static String getFormattedTime(Context context, Date date) {
        if(context == null) {
            return null;
        }
        return getPreferredTimeFormat(context).format(date);
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
        return prefix + ": " + getFormattedDate(context, date) + " " + getFormattedTime(context, date);
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

    public static SimpleDateFormat getDayMonthDateFormatUniversal() {
        return new SimpleDateFormat("MMM d", Locale.getDefault());
    }

    public static SimpleDateFormat getMonthDayYearDateFormatUniversal() {
        if (Build.VERSION.SDK_INT >= 24) {
            return new SimpleDateFormat("MMMM d, YYYY", Locale.getDefault());
        } else {
            return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        }
    }

    public static String getDayMonthDateStringUniversal(Date date) {
        Format format = getDayMonthDateFormatUniversal();
        return format.format(date.getTime());
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
        return getDayMonthDateStringUniversal(date) + " " + getFormattedTime(context, date);
    }

    public static String getDateNoYearWeekDayAndTime(Context context, Date date) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME;
        //uses local default
        return DateUtils.formatDateTime(context, date.getTime(), flags);
    }

    /**
     * Simple date helper, formats date as:
     *
     * Month 15 separator 11:59 PM
     * @param context
     * @param date
     * @param separator example "at", spacing is handled by helper
     * @return
     */
    @Nullable
    public static String getMonthDayAtTime(Context context, Date date, String separator) {
        if (date == null) return null;
        String dateString;
        dateString = getDayMonthDateFormatUniversal().format(date);
        dateString += " " + separator + " ";

        dateString += getDayAbbreviationFormat(context).format(date);

        return dateString;
    }

    /**
     * Simple date helper, formats date as:
     *
     * Month 15 separator 11:59 PM
     * @param context
     * @param date
     * @param stringResSeparator String resource for separator; example "at", spacing is handled by helper
     * @return
     */
    public static String getMonthDayAtTime(Context context, Date date, int stringResSeparator) {
        String dateString;
        String separator = context.getString(stringResSeparator);
        dateString = getDayMonthDateFormatUniversal().format(date);
        dateString += " " + separator + " ";

        dateString += getDayAbbreviationFormat(context).format(date);

        return dateString;
    }

    /**
     * Simple date helper, examples:
     *
     * Sep 15, 2018 {separator} 9:02am
     *
     * or
     *
     * Sep 15 {separator} 11pm <- No year if year matches the curren year; Minutes not displayed if there are none
     *
     * Year is added only if it is not the current year.
     * @param context
     * @param date
     * @param stringResSeparator String resource for separator; example "at", spacing is handled by helper
     * @return If date is null, will return a null string
     */
    @Nullable
    public static String getMonthDayTimeMaybeMinutesMaybeYear(Context context, @Nullable Date date, int stringResSeparator) {
        if (date == null) {
            return null;
        }

        StringBuilder dateString;
        String separator = context.getString(stringResSeparator);
        if (isThisYear(date)) {
                dateString = new StringBuilder(getDayMonthDateFormatUniversal().format(date));
        } else {
            dateString = new StringBuilder(getMonthDayYearDateFormatUniversal().format(date));
        }

        dateString
                .append(" ")
                .append(separator)
                .append(" ");

        if (timeHasMinutes(date)) {
            dateString.append(getDayAbbreviationFormat(context).format(date));
        } else {
            dateString.append(getTimeNoMinutesFormat(context).format(date));
        }

        return dateString.toString();
    }

    public static boolean timeHasMinutes(Date date) {
        Calendar srcCal = Calendar.getInstance();
        srcCal.setTime(date);

        return srcCal.get(Calendar.MINUTE) > 0;
    }

    public static boolean isThisYear(Date date) {
        Calendar srcCal = Calendar.getInstance();
        srcCal.setTime(date);

        Date compare = new Date();
        Calendar compareCal = Calendar.getInstance();
        compareCal.setTime(compare);

        return srcCal.get(Calendar.YEAR) == compareCal.get(Calendar.YEAR);
    }

    /**
     * Transform Calendar to ISO 8601 string.
     */
    @Nullable
    public static String dateToDayMonthYearString(Context context, final @Nullable Date date) {
        if (date == null){
            return null;
        }

        return getFormattedDate(context, date);
    }

    /**
     * Used for making a clean date when comparing items.
     * @param dateTime
     * @return
     */
    public static Date getCleanDate(long dateTime) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(dateTime);
        GregorianCalendar genericDate = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        return new Date(genericDate.getTimeInMillis());
    }
}
