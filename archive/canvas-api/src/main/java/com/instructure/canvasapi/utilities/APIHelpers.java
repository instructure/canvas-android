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
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.instructure.canvasapi.api.AlertAPI;
import com.instructure.canvasapi.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.client.Header;
import retrofit.client.Response;

public class APIHelpers {

    /**
     * SharedPreferences tags
     */

    //We would need migration code to update NAME and MASQUERADED_USER to snake case, so we will leave them as is for now.
    private final static String SHARED_PREFERENCES_NAME = "canvas-kit-sp";
    private final static String SHARED_PREFERENCES_MASQUERADED_USER = "masq-user";

    private final static String SHARED_PREFERENCES_USER = "user";
    private final static String SHARED_PREFERENCES_DOMAIN = "domain";
    private final static String SHARED_PREFERENCES_MASQUERADED_DOMAIN = "masq-domain";

    private final static String SHARED_PREFERENCES_KALTURA_DOMAIN = "kaltura_domain";
    private final static String SHARED_PREFERENCES_TOKEN = "token";
    private final static String SHARED_PREFERENCES_KALTURA_TOKEN = "kaltura_token";
    private final static String SHARED_PREFERENCES_USER_AGENT = "user_agent";
    private final static String SHARED_PREFERENCES_API_PROTOCOL = "api_protocol";
    private final static String SHARED_PREFERENCES_KALTURA_PROTOCOL = "kaltura_protocol";
    private final static String SHARED_PREFERENCES_ERROR_DELEGATE_CLASS_NAME = "error_delegate_class_name";
    private final static String SHARED_PREFERENCES_DISMISSED_NETWORK_ERROR = "dismissed_network_error";
    private final static String SHARED_PREFERENCES_AIRWOLF_DOMAIN = "airwolf_domain";


    /**
     * Log Tag
     */
    public final static String LOG_TAG = "canvas-api";

    /**
     *
     * GetAssetsFile allows you to open a file that exists in the Assets directory.
     *
     * @param context
     * @param fileName
     * @return the contents of the file.
     */
    public static String getAssetsFile(Context context, String fileName) {
        try {
            String file = "";
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            // do reading
            String line = "";
            while (line != null) {
                file += line;
                line = reader.readLine();
            }

            reader.close();
            return file;

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * clearAllData is essentially a Logout.
     * Clears all data including credentials and cache.
     *
     * @param context
     * @return
     */
    public static boolean clearAllData(Context context) {
        if(context == null){
            return false;
        }

        //Clear the cached API Responses
        CanvasRestAdapter.deleteHttpCache();

        //Clear credentials.
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        boolean sharedPreferencesDeleted =  editor.commit();

        //Delete cache.
        File cacheDir = new File(context.getFilesDir(), FileUtilities.FILE_DIRECTORY);
        boolean cacheDeleted = FileUtilities.deleteAllFilesInDirectory(cacheDir);

        return sharedPreferencesDeleted && cacheDeleted;
    }


    /**
     * setCacheUser saves the currently signed in user to cache.
     * @param context
     * @param user
     * @return
     */

    public static boolean setCacheUser(Context context, User user) {

        if (user == null) {
            return false;
        } else {
            Gson gson = CanvasRestAdapter.getGSONParser();
            String userString = gson.toJson(user);
            if (userString == null) {
                return false;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            String sharedPrefsKey = SHARED_PREFERENCES_USER;
            if(Masquerading.isMasquerading(context)){
                sharedPrefsKey = SHARED_PREFERENCES_MASQUERADED_USER;
            }

            editor.putString(sharedPrefsKey, userString);
            return  editor.commit();
        }
    }

    /**
     * setCachedAvatarURL is a helper to set a value on the cached user.
     * @param context
     * @param avatarURL
     * @return
     */
    public static boolean setCachedAvatarURL(Context context, String avatarURL){
        User user = getCacheUser(context);

        if(user == null){
            return false;
        }

        user.setAvatarURL(avatarURL);
        return setCacheUser(context, user);
    }

    /**
     * setCachedShortName is a helper to set a value on the cached user.
     * @param context
     * @param shortName
     * @return
     */
    public static boolean setCachedShortName(Context context, String shortName){
        User user = getCacheUser(context);

        if(user == null){
            return false;
        }

        user.setShortName(shortName);
        return setCacheUser(context, user);
    }

    /**
     * setCachedEmail is a helper to set a value on the cached user.
     * @param context
     * @param email
     * @return
     */

    public static boolean setCachedEmail(Context context, String email){
        User user = getCacheUser(context);

        if(user == null){
            return false;
        }

        user.setEmail(email);
        return setCacheUser(context, user);
    }

    /**
     * setCachedName is a helper to set a value on the cached user.
     * @param context
     * @param name
     * @return
     */

    public static boolean setCachedName(Context context, String name){
        User user = getCacheUser(context);

        if(user == null){
            return false;
        }

        user.setName(name);
        return setCacheUser(context, user);
    }


    /**
     * getCacheUser returns the signed-in user from cache. Returns null if there isn't one.
     * @param context
     * @return
     */

    public static User getCacheUser(Context context) {

        if(context == null){
            return null;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String sharedPrefsKey = SHARED_PREFERENCES_USER;
        if(Masquerading.isMasquerading(context)){
            sharedPrefsKey = SHARED_PREFERENCES_MASQUERADED_USER;
        }

        String userString = sharedPreferences.getString(sharedPrefsKey, null);
        if (userString == null) {
            return null;
        } else {
            Gson gson = CanvasRestAdapter.getGSONParser();
            return gson.fromJson(userString, User.class);
        }
    }


    /**
     * getUserAgent returns the current user agent.
     * @param context
     * @return
     */
    public static String getUserAgent(Context context) {

        if(context == null){
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFERENCES_USER_AGENT, "");

    }

    /**
     * setUserAgent sets the user agent
     * @param context
     * @param userAgent
     * @return
     */
    public static boolean setUserAgent(Context context, String userAgent) {

        if(userAgent == null || userAgent.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_USER_AGENT, userAgent);
        return editor.commit();
    }

    /**
     * getFullDomain returns the protocol plus the domain.
     * @return "" if context is null or if the domain/token isn't set.
     */
    public static String getFullDomain(Context context){
        String protocol = loadProtocol(context);
        String domain = getDomain(context);

        if (protocol == null || domain == null || protocol.equals("") || domain.equals("") ){
            return "";
        }

        if(URLUtil.isHttpsUrl(domain) || URLUtil.isHttpUrl(domain)) {
            //already begins with https or http
            return domain;
        }

        return protocol + "://" + domain;
    }

    /**
     * getDomain returns the current domain. This function strips off all trailing / characters and the protocol.
     * @link APIHelpers.loadProtocol(context)
     * @param context
     * @return
     */
    public static String getDomain(Context context) {

        if(context == null){
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String sharedPrefsKey = SHARED_PREFERENCES_DOMAIN;

        if(Masquerading.isMasquerading(context)){
            sharedPrefsKey = SHARED_PREFERENCES_MASQUERADED_DOMAIN;
        }
        String domain =  sharedPreferences.getString(sharedPrefsKey, "");

        while (domain != null && domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        return domain;
    }

    /**
     * getFullKalturaDomain returns the protocol plus the domain.
     *
     * Returns "" if context is null or if the domain/token isn't set.
     * @return
     */
    public static String getFullKalturaDomain(Context context){
        String protocol = loadProtocol(context);
        String domain = getKalturaDomain(context);

        if (protocol == null || domain == null || protocol.equals("") || domain.equals("") ){
            return "";
        }

        return protocol + "://" + domain;
    }

    /**
     * getKalturaDomain returns the current domain. This function strips off all trailing / characters and the protocol.
     * @link APIHelpers.loadProtocol(context)
     * @param context
     * @return
     */
    public static String getKalturaDomain(Context context){
        if(context == null){
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String domain =  sharedPreferences.getString(SHARED_PREFERENCES_KALTURA_DOMAIN, "");

        while (domain != null && domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        return domain;
    }

    /**
     * setDomain sets the current domain. It strips off the protocol.
     *
     * @param context
     * @param domain
     * @return
     */

    public static boolean setDomain(Context context, String domain) {


        if(domain == null || domain.equals("")){
            return false;
        }

       domain = removeProtocol(domain);

        String sharedPrefsKey = SHARED_PREFERENCES_DOMAIN;
        if(Masquerading.isMasquerading(context)){
            sharedPrefsKey = SHARED_PREFERENCES_MASQUERADED_DOMAIN;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sharedPrefsKey, domain);
        return editor.commit();
    }

    /**
     * setDomain sets the current Kaltura domain. It strips off the protocol.
     *
     * @param context
     * @param kalturaDomain
     * @return
     */

    public static boolean setKalturaDomain(Context context, String kalturaDomain) {


        if(kalturaDomain == null || kalturaDomain.equals("")){
            return false;
        }

        kalturaDomain = removeProtocol(kalturaDomain);

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_KALTURA_DOMAIN, kalturaDomain);
        return editor.commit();
    }

    /**
     * Check to see if the Airwolf domain is set. We don't want to return an empty domain, so this check
     * will return whether the user has set a domain.
     *
     * @param context
     * @return True if an Airwolf domain has been set, false otherwise
     */
    public static boolean isAirwolfDomainSet(Context context) {
        if(context == null){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        //if the shared preference here is empty, that means a domain hasn't been set
        return !TextUtils.isEmpty(sharedPreferences.getString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, ""));
    }

    /**
     * Get the Airwolf region to use. This will be set when the user first opens the app depending on which region is fastest
     *
     * If no region is set it will use the American Region as default
     *
     * @param context
     * @return Domain to use for Airwolf API calls
     */
    public static String getAirwolfDomain(Context context) {
        if(context == null){
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        //make america the default region
        return sharedPreferences.getString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, AlertAPI.AIRWOLF_DOMAIN_AMERICA);
    }

    public static boolean airwolfDomainExists(Context context) {
        if(context == null){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(SHARED_PREFERENCES_AIRWOLF_DOMAIN);
    }

    /**
     * Sets the current Airwolf domain
     *
     * @param context
     * @param airwolfDomain
     * @return True if saved successfully, false otherwise
     */

    public static boolean setAirwolfDomain(Context context, String airwolfDomain) {

        if(airwolfDomain == null || airwolfDomain.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, airwolfDomain);
        return editor.commit();
    }

    /**
     * getToken returns the OAuth token or "" if there isn't one.
     * @param context
     * @return
     */
    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFERENCES_TOKEN, "");
    }

    /**
     * setToken sets the OAuth token
     * @param context
     * @param token
     * @return
     */
    public static boolean setToken(Context context, String token) {
        if(token == null || token.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_TOKEN, token);
        return editor.commit();
    }

    /**
     * resetToken sets the OAuth token to an empty string
     * @param context
     * @return
     */
    public static boolean resetToken(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_TOKEN, "");
        return editor.commit();
    }

    /**
     * getToken returns the OAuth token or "" if there isn't one.
     * @param context
     * @return
     */
    public static String getKalturaToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFERENCES_KALTURA_TOKEN, "");
    }

    /**
     * setToken sets the OAuth token
     * @param context
     * @param token
     * @return
     */
    public static boolean setKalturaToken(Context context, String token) {
        if(token == null || token.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_KALTURA_TOKEN, token);
        return editor.commit();
    }

    /**
     * loadProtocol returns the protocol or 'https' if there isn't one.
     * @param context
     * @return
     */
    public static String loadProtocol(Context context) {

        if(context == null){
            return "https";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFERENCES_API_PROTOCOL, "https");
    }

    /**
     * setProtocol sets the protocol
     * @param protocol
     * @param context
     * @return
     */
    public static boolean setProtocol(String protocol, Context context) {

        if(protocol == null || protocol.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_API_PROTOCOL, protocol);
        return editor.commit();
    }


    /**
     * Sets the default error delegate. This is the default if one isn't specified in the constructor
     *
     * @param errorDelegateClassName
     */
    public static void setDefaultErrorDelegateClass(Context context, String errorDelegateClassName) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_ERROR_DELEGATE_CLASS_NAME, errorDelegateClassName);
        editor.apply();
    }

    /**
     * Get the default error delegate.
     *
     * @param context
     */
    public static String getDefaultErrorDelegateClass(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFERENCES_ERROR_DELEGATE_CLASS_NAME, null);

    }
    /**
     * booleanToInt is a Helper function for Converting boolean to URL booleans (ints)
     */
    public static int booleanToInt(boolean bool) {
        if (bool) {
            return 1;
        }
        return 0;
    }

    /**
     * removeDomainFromUrl is a helper function for removing the domain from a url. Used for pagination/routing
     * @param url
     * @return
     */
    public static String removeDomainFromUrl(String url) {
        if(url == null){
            return null;
        }

        String prefix = "/api/v1/";
        int index = url.indexOf(prefix);
        if (index != -1) {
            url = url.substring(index + prefix.length());
        }
        return url;
    }

    public static boolean isCachedResponse(Response response) {
        return response != null && response.getHeaders() != null &&
                response.getHeaders().contains(new Header(CanvasOkClient.CANVAS_API_CACHE_HEADER, CanvasOkClient.CANVAS_API_CACHE_HEADER_VALUE));
    }

    /**
     * Helper methods for handling ISO 8601 strings of the following format:
     * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
     */

    /**
     * Transform Calendar to ISO 8601 string.
     */
    public static String dateToString(final Date date) {
        if (date == null){
            return null;
        }

        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * Transform Calendar to ISO 8601 string.
     */
    public static String dateToDayMonthYearString(Context context, final Date date) {
        if (date == null){
            return null;
        }

        return DateHelpers.getFormattedDate(context, date);
    }

    /**
     * Helper methods for handling ISO 8601 strings of the following format:
     * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
     */


    /**
     * Transform ISO 8601 string to Calendar.
     */
    public static Date stringToDate(final String iso8601string) {
        try {
            String s = iso8601string.replace("Z", "+00:00");
            s = s.substring(0, 22) + s.substring(23);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * parseLinkHeaderResponse parses HTTP headers to return the first, next, prev, and last urls. Used for pagination.
     * @param context
     * @param headers
     * @return
     */
    public static LinkHeaders parseLinkHeaderResponse(Context context, List<Header> headers) {
        LinkHeaders linkHeaders = new LinkHeaders();

        for (int i = 0; i < headers.size(); i++) {
            if ("link".equalsIgnoreCase(headers.get(i).getName())) {
                String[] split = headers.get(i).getValue().split(",");
                for (int j = 0; j < split.length; j++) {
                    int index = split[j].indexOf(">");
                    String url = split[j].substring(0, index);
                    url = url.substring(1);

                    //Remove the domain.
                    url = removeDomainFromUrl(url);

                    if (split[j].contains("rel=\"next\"")) {
                        linkHeaders.nextURL = url;
                    } else if (split[j].contains("rel=\"prev\"")) {
                        linkHeaders.prevURL = url;
                    } else if (split[j].contains("rel=\"first\"")) {
                        linkHeaders.firstURL = url;
                    } else if (split[j].contains("rel=\"last\"")) {
                        linkHeaders.lastURL = url;
                    }
                }

                break;
            }
        }

        return linkHeaders;
    }

    public static LinkHeaders parseLinkHeaderResponse(String linkField) {
        LinkHeaders linkHeaders = new LinkHeaders();
        if (TextUtils.isEmpty(linkField)) {
            return linkHeaders;
        }

        String[] split = linkField.split(",");
        for (int j = 0; j < split.length; j++) {
            int index = split[j].indexOf(">");
            String url = split[j].substring(0, index);
            url = url.substring(1);

            //Remove the domain.
            url = removeDomainFromUrl(url);

            if (split[j].contains("rel=\"next\"")) {
                linkHeaders.nextURL = url;
            } else if (split[j].contains("rel=\"prev\"")) {
                linkHeaders.prevURL = url;
            } else if (split[j].contains("rel=\"first\"")) {
                linkHeaders.firstURL = url;
            } else if (split[j].contains("rel=\"last\"")) {
                linkHeaders.lastURL = url;
            }
        }
        return linkHeaders;
    }



    public static APIStatusDelegate statusDelegateWithContext(final Context context) {
        return new APIStatusDelegate() {
            @Override public void onCallbackStarted() { }
            @Override public void onCallbackFinished(CanvasCallback.SOURCE source) { }
            @Override public void onNoNetwork() { }

            @Override public Context getContext() {
                return context;
            }
        };
    }

    /**
     * paramIsNull is a helper function for determining if callbacks/other objects are null;
     * @param callback
     * @param args
     * @return
     */
    public static boolean paramIsNull(CanvasCallback<?> callback, Object... args) {
        if (callback == null || callback.getContext() == null) {
            logParamsNull();
            return true;
        }
        return paramIsNull(args);
    }

    /**
     * paramIsNull is a helper function for determining if callbacks/other objects are null;
     * @param args
     * @return
     */
    public static boolean paramIsNull(Object... args) {

        for (Object arg : args) {
            if (arg == null) {
                logParamsNull();
                return true;
            }
        }
        return false;
    }


    /**
     * logParamsNull is a logging function helper
     */
    private static void logParamsNull() {
        Log.d(APIHelpers.LOG_TAG, "One or more parameters is null");
    }

    private static String removeProtocol(String domain){
        if (domain.contains("https://")) {
          return domain.substring(8);
        }
        if (domain.startsWith("http://")) {
            return domain.substring(7);
        }
        else return domain;
    }

    /**
     * Check to see if the user has seen the network error message so we don't need to display it repeatedly
     * @param context
     * @return True if the user has seen the network error message, false otherwise
     */
    public static boolean hasSeenNetworkErrorMessage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(SHARED_PREFERENCES_DISMISSED_NETWORK_ERROR, false);
    }

    /**
     * Sets whether the user has seen the network error message
     *
     * @param context
     * @param hasSeenErrorMessage
     */
    public static void setHasSeenNetworkErrorMessage(Context context, boolean hasSeenErrorMessage) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sharedPrefsKey = SHARED_PREFERENCES_DISMISSED_NETWORK_ERROR;
        editor.putBoolean(sharedPrefsKey, hasSeenErrorMessage);
        editor.apply();
    }

}
