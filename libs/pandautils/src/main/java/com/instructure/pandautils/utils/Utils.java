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

package com.instructure.pandautils.utils;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.R;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    /**
     * Check if the device has a camera. If it doesn't, return false
     */
    public static boolean hasCameraAvailable(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    public static File getAttachmentsDirectory(Context context) {
        File file;
        if (context.getExternalCacheDir() != null) {
            file = new File(context.getExternalCacheDir(), "attachments");
        } else {
            file = context.getFilesDir();
        }
        return file;
    }

    public static boolean isNetworkAvailable(Context context) {
        if(context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * The fromHTML method can cause a character that looks like [obj]
     * to show up. This is undesired behavior most of the time.
     *
     * Replace the [obj] with an empty space
     * [obj] is char 65532 and an empty space is char 32
     * @param sequence The fromHTML typically
     * @return The modified charSequence
     */
    public static String simplifyHTML(CharSequence sequence) {
        if(sequence != null) {
            CharSequence toReplace = sequence;
            toReplace = toReplace.toString().replace(((char) 65532), (char) 32).trim();
            return toReplace.toString();
        }
        return "";
    }

    public static Map<String, String> getReferer(Context context){
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", ApiPrefs.getDomain());

        return extraHeaders;
    }

    public static Map<String, String> getRefererAndAuthentication(Context context) {
        String token = ApiPrefs.getToken();
        String headerValue = null;
        if(token != null) {
            headerValue = String.format("Bearer %s", token);
        }
        Map<String,String> extraHeaders = new HashMap<String,String>();
        extraHeaders.put("Authorization", headerValue);
        extraHeaders.put("Referer", ApiPrefs.getDomain());

        return extraHeaders;
    }

    /**
     * Sets a views content description based on the build flavor
     * 
     * @param view view to be set
     * @param testDescription String for testers, standard is text_#position
     * @param a11yDescription String to display for a11y
     * @param isDebug BuildConfig.DEBUG
     */
    public static void testSafeContentDescription(View view, String testDescription, String a11yDescription, boolean isDebug) {
        if(isDebug) {
            view.setContentDescription(testDescription);
        } else {
            view.setContentDescription(a11yDescription);
        }
    }

    public static float dpToPx(Context context, float dp){
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static String generateUserAgent(Context context, String userAgentString) {
        String userAgent;
        try {
            userAgent = userAgentString + "/" +
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName +
                    " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            userAgent = userAgentString;
        }
        return userAgent;
    }

    public static void goToAppStore(AppType appType, Context context) {
        String packageName = "";
        if (appType == AppType.STUDENT) {
            packageName = "com.instructure.candroid";
        } else if (appType == AppType.POLLING) {
            packageName = "com.instructure.androidpolling";
        } else if (appType == AppType.PARENT) {
            packageName = "com.instructure.parentapp";
        } else if (appType == AppType.TEACHER) {
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

    public static void copyToClipboard(Context context, String stringToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(context.getString(R.string.link), stringToCopy);
            clipboard.setPrimaryClip(clip);

            // Let the user know
            Toast.makeText(context, context.getString(R.string.linkCopied), Toast.LENGTH_SHORT).show();
        }
    }

}
