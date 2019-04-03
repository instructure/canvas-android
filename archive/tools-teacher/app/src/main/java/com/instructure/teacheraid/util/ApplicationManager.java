/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
 */

package com.instructure.teacheraid.util;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.instructure.canvasapi.api.OAuthAPI;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.loginapi.login.OAuthWebLogin;
import com.instructure.loginapi.login.model.SignedInUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import retrofit.client.Response;

public class ApplicationManager extends MultiDexApplication {

    public final static String PREF_NAME = "teacher_aid";
    public final static String PREF_CHANGELOG_NAME = "ta_changelog_SP";
    public final static String PREF_FILE_NAME = "TA_SP";
    public final static String MULTI_SIGN_IN_PREF_NAME = "multipleSignInTA_SP";
    public final static String OTHER_SIGNED_IN_USERS_PREF_NAME = "otherSignedInUsersTA_SP";
    public final static String MASQ_PREF_NAME = "masqueradeTA_SP";
    public final static String PREF_NAME_PREVIOUS_DOMAINS = "TA_SP_previous_domains";
    public final static String WIDGET_PREF = "TA_SP_widget";

    public final static String APID = "APID";
    public final static String LAST_DOMAIN = "last-domain";
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());



        SharedPreferences pref = this.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        //If we don't have one, generate one.
        if (!pref.contains(APID)) {
            String uuid = UUID.randomUUID().toString();

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(APID, uuid);
            editor.commit();
        }

    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception E) {
            return "";
        }
    }


    /**
     * Switch out the current signed in user. Temporarily remove credentials. Save them elsewhere so we can repopulate it when necessary.
     *
     * @return
     */
    public boolean switchUsers() {
        if (!OAuthWebLogin.isMultipleUsersSupported(getApplicationContext(), MULTI_SIGN_IN_PREF_NAME)) {
            return false;
        }


        SignedInUser signedInUser = new SignedInUser();
        signedInUser.user = APIHelpers.getCacheUser(this);
        signedInUser.domain = APIHelpers.getDomain(this);
        signedInUser.protocol = APIHelpers.loadProtocol(this);
        signedInUser.token = APIHelpers.getToken(this);
        signedInUser.lastLogoutDate = new Date();

        //Save Signed In User to sharedPreferences
        OAuthWebLogin.addToPreviouslySignedInUsers(signedInUser, getApplicationContext(), ApplicationManager.OTHER_SIGNED_IN_USERS_PREF_NAME);

        //Clear shared preferences, but keep the important stuff.
        safeClearSharedPreferences();

        //CLear masquerading preferences.
        clearMasqueradingPreferences();

        //Clear all Shared Preferences.
        APIHelpers.clearAllData(this);

        return true;
    }


    /**
     * Log out the currently signed in user. Permanently remove credential information.
     *
     * @return
     */
    public boolean logoutUser() {

        //It is possible for multiple APIs to come back 'simultaneously' as HTTP401s causing a logout
        //if this has already ran, data is already cleared causing null pointer exceptions
        if (APIHelpers.getToken(this) != null && !APIHelpers.getToken(this).equals("")) {

            //Delete token from server
            //We don't actually care about this coming back. Fire and forget.
            CanvasCallback<Response> deleteTokenCallback = new CanvasCallback<Response>(APIHelpers.statusDelegateWithContext(this)) {
                @Override
                public void cache(Response response) {
                }

                @Override
                public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                }
            };

            OAuthAPI.deleteToken(deleteTokenCallback);
            //Remove Signed In User from sharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(OTHER_SIGNED_IN_USERS_PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(OAuthWebLogin.getGlobalUserId(APIHelpers.getDomain(this), APIHelpers.getCacheUser(this)));
            editor.commit();

            //Clear shared preferences, but keep the important stuff.
            safeClearSharedPreferences();

            //CLear masquerading preferences.
            clearMasqueradingPreferences();

            //Clear all Shared Preferences.
            APIHelpers.clearAllData(this);


        }

        return true;
    }

    public void clearMasqueradingPreferences() {
        //stop masquerading
        Masquerading.stopMasquerading(this);

        //clear any shared preferences for the masqueraded user
        SharedPreferences masq_settings = getSharedPreferences(ApplicationManager.MASQ_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor masq_editor = masq_settings.edit();
        masq_editor.clear();
        masq_editor.commit();
    }


    public void safeClearSharedPreferences() {
        //Get the Shared Preferences
        SharedPreferences settings = getSharedPreferences(ApplicationManager.PREF_FILE_NAME, MODE_PRIVATE);


        String lastDomain = settings.getString(LAST_DOMAIN, "");
        String UUID = settings.getString(APID, null);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        //Replace the information about last domain

        editor.putString(LAST_DOMAIN, lastDomain);

        if (UUID != null) {
            editor.putString(APID, UUID);
        }

        editor.commit();
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

    //DO NOT CALL IN UI THREAD!
    public static boolean writeAttachmentsDirectoryFromURL(final Activity activity, String url2, File toWriteTo, final ProgressDialog myProgress, Handler mHandler) {
        try {
            //create the new connection
            URL url = new URL(url2);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();

            //this will be used to write the downloaded data into the file we created
            String name = toWriteTo.getName();
            toWriteTo.getParentFile().mkdirs();
            FileOutputStream fileOutput = null;
            //if there is an external cache, we want to write to that
            if (activity.getApplicationContext().getExternalCacheDir() != null) {
                fileOutput = new FileOutputStream(toWriteTo);
            }
            //otherwise, use internal cache.
            else {
                fileOutput = activity.getApplicationContext().openFileOutput(name,
                        MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
            }
            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file
            final int totalSize = urlConnection.getContentLength();

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    myProgress.setMax(totalSize);
                }
            });

            //variable to store total downloaded bytes
            int downloadedSize = 0;

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer
            int progress = 0;
            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                downloadedSize += bufferLength;
                //Here we update the progress
                progress = downloadedSize;
                final int currentProgress = progress;
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        myProgress.setProgress(currentProgress);
                    }
                });
            }


            fileOutput.flush();
            fileOutput.close();
            myProgress.dismiss();

            return true;
        } catch (Exception E) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //AppMsg.makeText(activity, activity.getApplicationContext().getResources().getString(R.string.unexpectedErrorOpeningFile), AppMsg.STYLE_ERROR).show();
                    myProgress.dismiss();
                }
            });
            return false;
        }
    }


    public static boolean writeCacheData(String filename, String data, Context context) {
        return writeCacheData(filename, data, context, false);
    }

    public static boolean writeCacheData(String filename, String data, Context context, boolean append) {
        try {
            //save the json.
            File file;
            //if we're masquerading use a different cache
            if (Masquerading.isMasquerading(context)) {
                file = new File(context.getFilesDir(), "cache_masquerade");
            } else {
                file = new File(context.getFilesDir(), "cache");
            }
            file.mkdirs();
            File f = new File(file, filename);

            f.createNewFile();

            FileOutputStream fos;

            fos = new FileOutputStream(f);

            fos.write(data.getBytes());
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCacheData(String filename, Context context) {

        //load the cached data
        FileInputStream fis = null;
        try {
            File file;
            //if we're masquerading use a different cache
            if (Masquerading.isMasquerading(context)) {
                file = new File(context.getFilesDir(), "cache_masquerade");
            } else {
                file = new File(context.getFilesDir(), "cache");
            }
            File f = new File(file, filename);

            if (!f.exists())
                return "";

            fis = new FileInputStream(f);

            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer));
            }

            fis.close();

            return fileContent.toString();
        } catch (Exception e) {

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                }
            }

            return null;

        }
    }

    /**
     * @param context used to check the device version and DownloadManager information
     * @return true if the download manager is available
     */
    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

}
