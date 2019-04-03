/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.loginapi.login.rating.RatingDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static long mAnimationDelay = 300l;
    public static long mAnimationDelayExtended = 600l;

    public static boolean isSpeedGraderInstalled(Context context){
        PackageManager pm = context.getPackageManager();
        try{
            pm.getPackageInfo(Const.SPEEDGRADER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

    public static void openApplication(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Utils.e("COULD NOT FIND ACTIVITY FOR INTENT: " + e);
        }
    }

    public static float convertDipsToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static void logTime(String...log) {

        long dtMili = System.currentTimeMillis();
        Date dt = new Date(dtMili);
        CharSequence dateTimeString = DateFormat.format("EEEE, MMMM d, yyyy ", dt.getTime());

        String l = "";
        if(log != null && log.length > 0) {
            l = log[0];
            Utils.d(">>> " + dateTimeString + " MESSAGE: " + l);
        } else {
            Utils.d(">>> " + dateTimeString);
        }
    }

    public static void logFrag(Fragment fragment) {
        if (fragment != null) {
            Utils.d(fragment.getClass().getSimpleName());
        } else {
            Utils.d("FRAGMENT WAS NULL");
        }
    }

    public static void d(final String log) {
        Log.d("abcde", log);
    }

    public static void e(final String log) {
        Log.e("abcde", log);
    }

    public static void logIfNull(final String log, Object o) {
        if(o == null) {
            d(log);
        }
    }

    public static void logIfNotNull(final String log, Object o) {
        if(o != null) {
            d(log);
        }
    }

    public static String getFragmentName(Fragment fragment) {
        if(fragment != null) {
            return fragment.getClass().getName();
        }
        return "UNKNOWN";
    }

    public static Class getClassClass(Fragment fragment) {
        if(fragment != null) {
            return fragment.getClass();
        }
        return null;
    }

    public static String logVisibility(int visibility){
        if(visibility == View.VISIBLE) {
            return "VISIBLE";
        } else if(visibility == View.INVISIBLE) {
            return "INVISIBLE";
        } else if(visibility == View.GONE) {
            return "GONE";
        }
        return "????";
    }

    public static void logBundle(final Bundle extras) {
        if(extras != null) {
            Utils.d("---====---LOGGING BUNDLE---====---");
            if(extras.size() == 0) {
                Utils.d("- Bundle was empty.");
            }
            for (String key: extras.keySet()){
                Utils.d("- Bundle: " + key);

                if("bundledExtras".equals(key)) {
                    Bundle innerExtras = extras.getBundle("bundledExtras");
                    if(innerExtras != null) {
                        for (String innerKey: innerExtras.keySet()) {
                            Utils.d("   -> Inner Bundle: " + innerKey);
                        }
                    }
                }
            }
        } else {
            Utils.d("Bundle was null.");
        }
    }

    public static void goToAppStore(RatingDialog.APP_NAME appName, Context context) {
        if(com.instructure.pandautils.utils.Utils.isAmazonDevice()) {
            String marketURL = "";
            if(appName == RatingDialog.APP_NAME.CANDROID) {
                marketURL = "http://www.amazon.com/gp/mas/dl/android?p=com.instructure.candroid";
            }
            else if(appName == RatingDialog.APP_NAME.POLLING) {

            }
            else if(appName == RatingDialog.APP_NAME.SPEEDGRADER) {

            }
            Intent goToAppstore = new Intent(Intent.ACTION_VIEW);
            goToAppstore.setData(Uri.parse(marketURL));
            context.startActivity(goToAppstore);
        }
        else {
            String packageName = "";
            if (appName == RatingDialog.APP_NAME.CANDROID) {
                packageName = "com.instructure.candroid";
            } else if (appName == RatingDialog.APP_NAME.POLLING) {
                packageName = "com.instructure.androidpolling";
            } else if (appName == RatingDialog.APP_NAME.SPEEDGRADER) {

            } else if (appName == RatingDialog.APP_NAME.PARENT) {
                packageName = "com.instructure.parentapp";
            } else if (appName == RatingDialog.APP_NAME.TEACHER) {
                packageName = "com.instructure.teacher";
            }
            try {
                Intent goToMarket = new Intent(Intent.ACTION_VIEW);
                goToMarket.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                //the device might not have the play store installed, open it in a webview
                Intent goToMarket = new Intent(Intent.ACTION_VIEW);
                goToMarket.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                context.startActivity(goToMarket);
            }
        }
    }

    public static boolean isNumber(String possibleNum) {
        if(TextUtils.isEmpty(possibleNum)) {
            return false;
        }
        try {
            long d = Long.parseLong(possibleNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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

    public static Map<String, String> getReferer(Context context){
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", APIHelpers.getDomain(context));

        return extraHeaders;
    }

    public static Map<String, String> getRefererAndAuthentication(Context context){
        Map<String, String> extraHeaders = CanvasAPI.getAuthenticatedURL(context);
        extraHeaders.put("Referer", APIHelpers.getDomain(context));

        return extraHeaders;
    }
}
