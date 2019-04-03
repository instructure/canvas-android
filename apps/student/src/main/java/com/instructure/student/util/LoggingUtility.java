/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

public class LoggingUtility {

    private static String TAG = "canvasLog";
    private static String TAG_JSON = "canvasLog_json";

    /**
     * Logs JSON using the adb logcat tool using tag "candroid-json" {@link android.util.Log#d}
     * <p>
     *
     * @param msg The message to log to console
     * @see android.util.Log#d
     */
    public static void LogConsoleJSON(String msg) {
        //Only logcat.
        android.util.Log.d(LoggingUtility.TAG_JSON, msg);
    }

    /**
     * Logs using the adb logcat tool using tag "candroid" {@link android.util.Log#d}
     * <p>
     *
     * @param msg The message to log to console
     * @see android.util.Log#d
     */
    public static void LogConsole(String msg) {
        //Only logcat.
        android.util.Log.d(LoggingUtility.TAG, msg);
    }

    /**
     * Logs using Crashlytics {@link Crashlytics#log(String)}
     * <p>
     *
     * @param msg The message to log to console
     * @see Crashlytics#log(String)
     */
    public static void LogCrashlytics(String msg) {
        //Only log crashlytics.
        if (Logger.canLogUserDetails()) Crashlytics.log(msg);
    }

    /**
     * Posts the given url as an exception to Crashlytics
     *
     * @param url the url to post as an exception to Crashlytics
     * @see com.crashlytics.android.Crashlytics#logException(Throwable)
     */
    public static void postCrashlyticsLogs(String url) {
        if (Logger.canLogUserDetails()) Crashlytics.logException(getUrlAsException(url));
    }

    /**
     * Logs Console {@link android.util.Log#d} and Crashlytics {@link Crashlytics#log(String)}}
     * <p>
     *
     * @param context  The application context
     * @param priority The priority of the logging. Examples are {@link android.util.Log#DEBUG} and {@link android.util.Log#ERROR}
     * @param msg      The message to log to console
     * @see Crashlytics#log(String)
     * @see android.util.Log#d
     */
    public static void Log(Context context, int priority, String msg) {
        //Will write to crashlytics and logcat
        if (Logger.canLogUserDetails()) Crashlytics.log(priority, TAG, msg);
    }

    /**
     * Logs Exception using HelpDesk, Console {@link android.util.Log#d}
     * <p>
     *
     * @param context The application context
     * @param E       The exception to log.
     * @return The string that was actually logged.
     */

    public static String LogException(Context context, Exception E) {
        return LogException(context, E, false);
    }

    /**
     * Logs Exception using HelpDesk, Console {@link android.util.Log#d}, and Crashlytics {@link Crashlytics#log(String)}
     * <p>
     *
     * @param context The application context
     * @param E       The exception to log.
     * @return The string that was actually logged.
     */
    public static String LogExceptionPlusCrashlytics(Context context, Exception E) {
        return LogException(context, E, true);
    }

    private static String LogException(Context context, Exception E, boolean crashlytics) {
        if (E == null) return "";

        if (crashlytics && Logger.canLogUserDetails()) {
            Crashlytics.logException(E);
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        E.printStackTrace(pw);
        String msg = sw.toString(); // stack trace as a string

        Log(context, Log.ERROR, msg);
        return msg;
    }

    public static String LogIntent(Context context, Intent intent) {
        if (intent == null) return "";

        Bundle bundle = intent.getExtras();
        return LogBundle(context, bundle);
    }

    /**
     * Logs all data of Intent using HelpDesk, Console {@link android.util.Log#d}, and Crashlytics {@link Crashlytics#log(String)}
     * <p>
     *
     * @param context The application context
     * @param bundle  The bundle that we want to log.
     * @return The string that was actually logged.
     */
    public static String LogBundle(Context context, Bundle bundle) {

        if (bundle == null) return "";

        Set<String> key = bundle.keySet();
        if (key == null) return "";

        Iterator<String> iterator = key.iterator();


        String logMSG = "";
        try {
            logMSG += bundle.getString("__previous");
        } catch (Exception ignore) {}

        try {
            logMSG += " --> " + bundle.getString("__current") + ";";
        } catch (Exception ignore) {}

        logMSG += "\n";

        while (iterator.hasNext()) {
            String keyString = iterator.next();

            if (keyString == null || keyString.equals("__current") || keyString.equals("__previous")) {
                continue;
            }

            Object o = bundle.get(keyString);

            if (o == null) {
                continue;
            }

            logMSG += keyString + ":" + o.toString() + ";\n";
        }

        Log(context, Log.DEBUG, logMSG);

        return logMSG;
    }


    private static Exception getUrlAsException(String url) {
        url = APIHelper.INSTANCE.removeDomainFromUrl(url);

        if (url == null) {
            return new Exception("null url");
        }

        String[] split = url.split("/");
        if (split.length > 0 && (split[0].equals("courses") || split[0].equals("groups"))) {
            url = "";
            int i = 0;
            if (split.length > 2) {
                i = 2;
            }

            while (i < split.length) {
                url += split[i] + "/";

                i++;
            }

        }

        return new Exception(url);
    }

}
