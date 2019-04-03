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
 *
 */

package com.instructure.speedgrader.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.instructure.canvasapi.api.OAuthAPI;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.loginapi.login.interfaces.AnalyticsEventHandling;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.speedgrader.R;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.configuration.PSPDFConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.exceptions.PSPDFInitializationFailedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import retrofit.client.Response;

public class App extends MultiDexApplication implements AnalyticsEventHandling{

    public final static String PREF_FILE_NAME = "speedgrader";
    public final static String OTHER_SIGNED_IN_USERS_PREF_NAME = "sg_other_signed_in_users";
    public final static String PREF_NAME_PREVIOUS_DOMAINS = "sg_name_prev_domains";
    public final static String MULTI_SIGN_IN_PREF_NAME = "sg_multi_pref_name";
    public final static String MASQ_PREF_NAME = "masqueradeSpeedGraderSP";

    private static Prefs mPrefs = null;
    private static Context applicationContext = null;
    private Tracker mTracker;

    @Override
    public void onCreate(){
        super.onCreate();
        applicationContext = getApplicationContext();
        CanvasContextColor.init(getPrefs(), R.color.sg_defaultPrimary, R.color.sg_defaultPrimaryDark);
        initPSPDFKit();
    }

    private void initPSPDFKit() {
        try {
            PSPDFKit.initialize(this, Const.PSPDFKIT_LICENSE_KEY);
        } catch (PSPDFInitializationFailedException e) {
            Log.e(PREF_FILE_NAME, "Current device is not compatible with PSPDFKit!");
        }
    }

    public PSPDFConfiguration getConfig(){
        return new PSPDFConfiguration.Builder(Const.PSPDFKIT_LICENSE_KEY)
                .scrollDirection(PageScrollDirection.VERTICAL)
                .build();
    }

    public static Prefs getPrefs(){
        if(mPrefs == null){
            mPrefs = new Prefs(applicationContext, PREF_FILE_NAME);
        }
        return mPrefs;
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
                public void cache(Response response) {}

                @Override
                public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {}
            };

            OAuthAPI.deleteToken(deleteTokenCallback);
            //Remove Signed In User from sharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(OTHER_SIGNED_IN_USERS_PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getGlobalUserId(APIHelpers.getDomain(this), APIHelpers.getCacheUser(this)));
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
        SharedPreferences masq_settings = getSharedPreferences(App.MASQ_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor masq_editor = masq_settings.edit();
        masq_editor.clear();
        masq_editor.commit();
    }

    public void safeClearSharedPreferences() {
        //Get the Shared Preferences
        SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        //Don't make them redo tutorials
        boolean tutorialViewed = settings.getBoolean(Const.shouldShowTutorial, false);
        boolean shouldDisplayVideoMessage = settings.getBoolean(Const.SHOW_VIDEO_MESSAGE, false);
        boolean shouldShowBounce = settings.getBoolean(Const.SHOW_BOUNCE, false);
        boolean shouldShowUngradedCount = settings.getBoolean(Const.VIEW_UNGRADED_COUNT, true);
        boolean shouldShowUngradedStudentsFirst = settings.getBoolean(Const.SHOW_UNGRADED_FIRST, true);
        boolean shouldShowStudentNames = settings.getBoolean(Const.SHOW_STUDENT_NAMES, true);
        String lastDomain = settings.getString("last-domain", "");

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        //Replace the information about tutorials/last domain
        editor.putString("last-domain", lastDomain);
        editor.putBoolean(Const.shouldShowTutorial, tutorialViewed);
        editor.putBoolean(Const.SHOW_VIDEO_MESSAGE, shouldDisplayVideoMessage);
        editor.putBoolean(Const.SHOW_BOUNCE, shouldShowBounce);
        editor.putBoolean(Const.VIEW_UNGRADED_COUNT, shouldShowUngradedCount);
        editor.putBoolean(Const.SHOW_UNGRADED_FIRST, shouldShowUngradedStudentsFirst);
        editor.putBoolean(Const.SHOW_STUDENT_NAMES, shouldShowStudentNames);

        editor.commit();
    }

    //Used for MultipleUserSignIn
    public String getGlobalUserId(String domain, User user) {
        return domain + "-" + user.getId();
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
                    Toast.makeText(activity, activity.getApplicationContext().getResources().getString(R.string.unexpectedErrorDownloadingFile), Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    static long uniqueId = Long.MIN_VALUE;
    public static long getUniqueId(){
        return uniqueId++;
    }
    ///////////////////////////////////////////////////////////////////////////
    // User Settings
    ///////////////////////////////////////////////////////////////////////////
    public SharedPreferences getSettings(){
        return getSharedPreferences(App.PREF_FILE_NAME, MODE_PRIVATE);
    }

    public boolean showStudentNames() {
        return getSettings().getBoolean(Const.SHOW_STUDENT_NAMES, true);
    }

    public void setShowStudentNames(boolean showGrades) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.SHOW_STUDENT_NAMES, showGrades);
        editor.commit();
    }

    public boolean showUngradedStudentsFirst() {
        return getSettings().getBoolean(Const.SHOW_UNGRADED_FIRST, true);
    }

    public void setShowUngradedStudentsFirst(boolean showUngraded) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.SHOW_UNGRADED_FIRST, showUngraded);
        editor.commit();
    }

    public boolean showUngradedCount() {
        return getSettings().getBoolean(Const.VIEW_UNGRADED_COUNT, true);
    }

    public void setShowUngradedCount(boolean showUngradedCount) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.VIEW_UNGRADED_COUNT, showUngradedCount);
        editor.commit();
    }

    public boolean showHTML() {
        return getSettings().getBoolean(Const.VIEW_HTML, true);
    }

    public void setShowHTML(boolean showHTML) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.VIEW_HTML, showHTML);
        editor.commit();
    }

    public boolean shouldShowBounce() {
        return getSettings().getBoolean(Const.SHOW_BOUNCE, true);
    }

    public void setShowBounce(boolean showBounce) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.SHOW_BOUNCE, showBounce);
        editor.commit();
    }

    public boolean shouldShowVideoMessage() {
        return getSettings().getBoolean(Const.SHOW_VIDEO_MESSAGE, true);
    }

    public void setShouldShowVideoMessage(boolean showVideoMessage) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.SHOW_VIDEO_MESSAGE, showVideoMessage);
        editor.commit();
    }

    public boolean shouldShowTutorial() {
        return getSettings().getBoolean(Const.shouldShowTutorial, true);
    }

    public void setShouldShowTutorial(boolean showTutorial) {
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Const.shouldShowTutorial, showTutorial);
        editor.commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tablet/Orientation Helpers
    ///////////////////////////////////////////////////////////////////////////
    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static boolean isPortrait(Context context){
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isLandscape(Context context){
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    //region Analytics Event Handling

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.analytics);
        }
        return mTracker;
    }

    @Override
    public void trackButtonPressed(String buttonName, Long buttonValue) {
        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("UI Actions")
                .setAction("Button Pressed")
                .setLabel(buttonName)
                .setValue(buttonValue)
                .build());
    }

    @Override
    public void trackScreen(String screenName) {
        Tracker tracker = getDefaultTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void trackEnrollment(String enrollmentType) {
        getDefaultTracker().send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, enrollmentType)
                .build());
    }

    @Override
    public void trackDomain(String domain) {
        getDefaultTracker().send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(2, domain)
                .build());
    }

    @Override
    public void trackEvent(String category, String action, String label, long value) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    @Override
    public void trackUIEvent(String action, String label, long value) {
        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    @Override
    public void trackTiming(String category, String name, String label, long duration) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category)
                .setLabel(label)
                .setVariable(name)
                .setValue(duration)
                .build());
    }
    //endregion
}
