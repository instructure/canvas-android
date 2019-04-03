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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instructure.canvasapi.model.CanvasContext;
import com.mobprofs.retrofit.converters.SimpleXmlConverter;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;



public class CanvasRestAdapter {

    private static int numberOfItemsPerPage = 30;
    private static int TIMEOUT_IN_SECONDS = 60;

    private static CanvasOkClient okHttpClient;
    public static int getNumberOfItemsPerPage() {
        return numberOfItemsPerPage;
    }

    private static final Interceptor mCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            com.squareup.okhttp.Request request = chain.request();

            Response response = chain.proceed(request);

            // Re-write response CC header to force use of cache
            // Displayed cached data will always be followed by a response from the server with the latest data.
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=1209600") //60*60*24*14 = 1209600 2 weeks; Essentially means cached data will only be valid offline for 2 weeks. When network is available, the cache is always updated on every request.
                    .build();
        }
    };

    public static void deleteHttpCache() {
        if(okHttpClient != null) {
            try {
                okHttpClient.getClient().getCache().evictAll();
            } catch (IOException e) {
                Log.d(APIHelpers.LOG_TAG, "Failed deleting the cache");
            }

        }
    }

    private static OkClient getOkHttp(Context context) {
        if (okHttpClient == null) {
            File httpCacheDirectory = new File(context.getCacheDir(), "responses");
            Cache cache = new Cache(httpCacheDirectory, 20 * 1024 * 1024); // cache size
            OkHttpClient httpClient = new OkHttpClient();
            httpClient.setCache(cache);
            httpClient.setReadTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            /** Dangerous interceptor that rewrites the server's cache-control header. */
            httpClient.networkInterceptors().add(mCacheControlInterceptor);
            okHttpClient = new CanvasOkClient(httpClient);
        }
        return okHttpClient;
    }

    private static OkClient getOkHttpNoRedirects(Context context) {
        CanvasOkClient client = (CanvasOkClient)getOkHttp(context);
        client.getClient().setFollowRedirects(false);
        return client;
    }

    private static OkClient getOkHttpClientForUploads(Context context) {
        CanvasOkClient client = (CanvasOkClient)getOkHttp(context);
        client.getClient().setConnectTimeout(10, TimeUnit.MINUTES);
        client.getClient().setReadTimeout(10, TimeUnit.MINUTES);
        client.getClient().setWriteTimeout(10, TimeUnit.MINUTES);
        client.getClient().setFollowRedirects(false);
        return client;
    }

    /**
     * Returns a RestAdapter Instance that points at :domain/api/v1
     *
     * @param context An Android context.
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(final Context context) {
        return buildAdapterHelper(context, null, false, true);
    }

    /**
     * Returns a RestAdapter Instance that points at :domain/api/v1
     *
     * @param  callback A Canvas Callback
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(CanvasCallback callback) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), null, false, true);
    }

    /**
     * Returns a RestAdapter Instance that points at domain
     *
     * @param  callback A Canvas Callback
     * @param  domain   Domain that you want to use for the API call
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(String domain, CanvasCallback callback) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), domain, null, false, true);
    }

    /**
     * Returns a RestAdapter instance that points at :domain/api/v1/groups or :domain/api/v1/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     *
     * @param callback A Canvas Callback
     * @param canvasContext A Canvas Context
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(CanvasCallback callback, CanvasContext canvasContext) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), canvasContext, false, true);
    }

    public static RestAdapter buildAdapter(CanvasCallback callback, boolean isOnlyReadFromCache, CanvasContext canvasContext) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), canvasContext, isOnlyReadFromCache, true);
    }

    public static RestAdapter buildAdapter(CanvasCallback callback, String domain, boolean isOnlyReadFromCache, CanvasContext canvasContext) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), domain, canvasContext, isOnlyReadFromCache, true);
    }

    /**
     * Returns a RestAdapter instance that points at :domain/api/v1/groups or :domain/api/v1/courses depending on the CanvasContext
     **
     * @param callback A Canvas Callback
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(CanvasCallback callback, boolean addPerPageQueryParam) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), null, false, addPerPageQueryParam);
    }

    /**
     * Returns a RestAdapter instance that points at :domain/api/v1/groups or :domain/api/v1/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     *
     * @param context An Android context.
     * @param canvasContext A Canvas Context
     * @return A Canvas RestAdapterInstance. If setupInstance() hasn't been called, returns an invalid RestAdapter.
     */
    public static RestAdapter buildAdapter(final Context context, CanvasContext canvasContext) {
        return buildAdapterHelper(context, canvasContext, false, true);
    }

    public static RestAdapter buildAdapter(final Context context, final boolean addPerPageQueryParam) {
        return buildAdapterHelper(context, null, false, addPerPageQueryParam);
    }

    public static RestAdapter buildAdapter(final Context context, boolean isOnlyReadFromCache, final boolean addPerPageQueryParam) {
        return buildAdapterHelper(context, null, isOnlyReadFromCache, addPerPageQueryParam);
    }
    /**
     * Returns a RestAdapter instance that points at :domain/api/v1/groups or :domain/api/v1/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     * @param callback A Canvas Callback
     * @param canvasContext A Canvas Context
     * @param isOnlyReadFromCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */
    public static RestAdapter buildAdapter(CanvasCallback callback, CanvasContext canvasContext, boolean isOnlyReadFromCache, boolean addPerPageQueryParam) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), canvasContext, isOnlyReadFromCache, addPerPageQueryParam);
    }

    /**
     * Returns a RestAdapter instance that points at :domain/groups or :domain/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     * @param callback A Canvas Callback
     * @param domain Domain that you want to use for the API call
     * @param canvasContext A Canvas Context
     * @param isOnlyReadFromCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */
    public static RestAdapter buildAdapter(CanvasCallback callback, String domain, CanvasContext canvasContext, boolean isOnlyReadFromCache, boolean addPerPageQueryParam) {
        callback.setFinished(false);
        return buildAdapterHelper(callback.getContext(), domain, canvasContext, isOnlyReadFromCache, addPerPageQueryParam);
    }

    /**
     * Returns a RestAdapter instance that points at :domain/groups or :domain/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     * @param callback A Canvas Callback
     * @param domain Domain that you want to use for the API call
     * @param canvasContext A Canvas Context
     * @param isOnlyReadFromCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */
    public static RestAdapter buildAdapterNoRedirects(CanvasCallback callback, String domain, CanvasContext canvasContext, boolean isOnlyReadFromCache, boolean addPerPageQueryParam) {
        callback.setFinished(false);
        //Check for null values or invalid CanvasContext types.
        if(callback.getContext() == null) {
            return null;
        }

        if (callback.getContext() instanceof APIStatusDelegate) {
            ((APIStatusDelegate)callback.getContext()).onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (domain == null || domain.equals("")) {
            Log.d(APIHelpers.LOG_TAG, "The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new RestAdapter.Builder().setEndpoint("http://invalid.domain.com").build();
        }

        String apiContext = "";
        if (canvasContext != null) {
            if (canvasContext.getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (canvasContext.getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (canvasContext.getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }

        GsonConverter gsonConverter = new GsonConverter(getGSONParser());

        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(domain + apiContext) // The base API endpoint.
                .setRequestInterceptor(new CanvasRequestInterceptor(callback.getContext(), addPerPageQueryParam, isOnlyReadFromCache))
                .setConverter(gsonConverter)
                .setClient(getOkHttpNoRedirects(callback.getContext())).build();
    }
    /**
     * Returns a RestAdapter instance that points at :domain/api/v1/groups or :domain/api/v1/courses depending on the CanvasContext
     *
     * If CanvasContext is null, it returns an instance that simply points to :domain/api/v1/
     * @param context An Android context.
     * @param canvasContext A Canvas Context
     * @param isOnlyReadFromCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */
    public static RestAdapter buildAdapter(final Context context, CanvasContext canvasContext, boolean isOnlyReadFromCache, boolean addPerPageQueryParam) {
        return buildAdapterHelper(context, canvasContext, isOnlyReadFromCache, addPerPageQueryParam);
    }

    private static RestAdapter buildAdapterHelper(final Context context, CanvasContext canvasContext, boolean isForcedCache, boolean addPerPageQueryParam) {
        //Check for null values or invalid CanvasContext types.
        if(context == null) {
            return null;
        }

        if (context instanceof APIStatusDelegate) {
            ((APIStatusDelegate)context).onCallbackStarted();
        }

        String domain = APIHelpers.getFullDomain(context);

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (domain == null || domain.equals("")) {
            Log.d(APIHelpers.LOG_TAG, "The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new RestAdapter.Builder().setEndpoint("http://invalid.domain.com").build();
        }

        String apiContext = "";
        if (canvasContext != null) {
            if (canvasContext.getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (canvasContext.getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (canvasContext.getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }

        GsonConverter gsonConverter = new GsonConverter(getGSONParser());

        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(domain + "/api/v1/" + apiContext) // The base API endpoint.
                .setRequestInterceptor(new CanvasRequestInterceptor(context, addPerPageQueryParam, isForcedCache))
                .setConverter(gsonConverter)
                .setClient(getOkHttp(context)).build();
    }

    /**
     * This helper can be used when you don't want to use the saved domain or the /api/v1/ in the API call
     *
     * @param context   Android context
     * @param domain    domain that you want to use for the API call
     * @param canvasContext A Canvas Context
     * @param isForcedCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */

    private static RestAdapter buildAdapterHelper(final Context context, String domain, CanvasContext canvasContext, boolean isForcedCache, boolean addPerPageQueryParam) {
        //Check for null values or invalid CanvasContext types.
        if(context == null) {
            return null;
        }

        if (context instanceof APIStatusDelegate) {
            ((APIStatusDelegate)context).onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (domain == null || domain.equals("")) {
            Log.d(APIHelpers.LOG_TAG, "The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new RestAdapter.Builder().setEndpoint("http://invalid.domain.com").build();
        }

        String apiContext = "";
        if (canvasContext != null) {
            if (canvasContext.getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (canvasContext.getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (canvasContext.getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }

        GsonConverter gsonConverter = new GsonConverter(getGSONParser());

        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(domain + apiContext) // The base API endpoint.
                .setRequestInterceptor(new CanvasRequestInterceptor(context, addPerPageQueryParam, isForcedCache))
                .setConverter(gsonConverter)
                .setClient(getOkHttp(context)).build();
    }


    /**
     * This adapter can be used for generic canvas requests that don't require a token. For example, getting account domains requires that you don't have
     * a token set
     *
     * @param context   Android context
     * @param isForcedCache Specify if you only want to read from cache
     * @param addPerPageQueryParam Specify if you want to add the per page query param
     * @return
     */

    public static RestAdapter buildGenericAdapter(final Context context, String domain, boolean isForcedCache, boolean addPerPageQueryParam, boolean shouldIgnoreToken) {
        //Check for null values or invalid CanvasContext types.
        if(context == null) {
            return null;
        }

        if (context instanceof APIStatusDelegate) {
            ((APIStatusDelegate)context).onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (domain == null || domain.equals("")) {
            Log.d(APIHelpers.LOG_TAG, "The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new RestAdapter.Builder().setEndpoint("http://invalid.domain.com").build();
        }

        GsonConverter gsonConverter = new GsonConverter(getGSONParser());

        //Sets the auth token, user agent, and handles masquerading.
        return new RestAdapter.Builder()
                .setEndpoint(domain + "/api/v1/") // The base API endpoint.
                .setRequestInterceptor(new CanvasRequestInterceptor(context, addPerPageQueryParam, isForcedCache, shouldIgnoreToken))
                .setConverter(gsonConverter)
                .setClient(getOkHttp(context)).build();
    }

    /**
     * Returns a RestAdapter Instance that points at :domain/
     *
     * Used ONLY in the login flow!
     *
     * @param  context An Android context.
     */
    public static RestAdapter buildTokenRestAdapter(final Context context){

        if(context == null ){
            return null;
        }

        String domain = APIHelpers.getFullDomain(context);

        return new RestAdapter.Builder()
                .setEndpoint(domain) // The base API endpoint.
                .setRequestInterceptor(new CanvasRequestInterceptor(context, true))
                .build();
    }


    /**
     * Returns a RestAdapter Instance that points at :domain/
     *
     * Used ONLY in the login flow!
     *
     */
    public static RestAdapter buildTokenRestAdapter(final String token, final String protocol, final String domain){

        if(token == null || protocol == null || domain == null ){
            return null;
        }

        RetrofitCounter.increment();

        return new RestAdapter.Builder()
                .setEndpoint(protocol + "://" + domain) // The base API endpoint.
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        requestFacade.addHeader("Authorization", "Bearer " + token);
                    }
                })
                .build();
    }

    /**
     * Creates a new RestAdapter for a generic endpoint. Useful for 3rd party api calls such as amazon s3 uploads.
     * @param hostUrl : url for desired endpoint
     * @return
     */
    public static RestAdapter getGenericHostAdapter(String hostUrl){

        RetrofitCounter.increment();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(hostUrl)
                .build();

        return restAdapter;
    }

    /**
     * Creates a new RestAdapter for a generic endpoint, utilizes XML converter instead of json
     * @param hostUrl : url for desired endpoint
     * @return
     */
    public static RestAdapter getGenericHostAdapterXML(Context context, String hostUrl){

        RetrofitCounter.increment();

        return new RestAdapter.Builder()
                .setConverter(new SimpleXmlConverter())
                .setEndpoint(hostUrl)
                .setClient(getOkHttpClientForUploads(context))
                .build();
    }


    /**
     * Creates a RestAdapter to ping an endpoint so we can get elapsed time of API calls
     * @param url
     * @return
     */
    public static RestAdapter buildPingRestAdapter(String url, Profiler profiler) {
        if(TextUtils.isEmpty(url)) {
            return null;
        }

        RetrofitCounter.increment();

        return new RestAdapter.Builder()
                .setEndpoint(url)
                .setProfiler(profiler).build();
    }
    /**
     * Class that's used as to inject the user agent, token, and handles masquerading.
     */

    public static class CanvasRequestInterceptor implements RequestInterceptor{

        Context context;
        boolean addPerPageQueryParam;
        boolean isForcedCache;
        boolean shouldIgnoreToken;

        CanvasRequestInterceptor(Context context, boolean addPerPageQueryParam){
            this.context = context;
            this.addPerPageQueryParam = addPerPageQueryParam;

        }

        CanvasRequestInterceptor(Context context, boolean addPerPageQueryParam, boolean isForcedCache){
            this.context = context;
            this.addPerPageQueryParam = addPerPageQueryParam;
            this.isForcedCache = isForcedCache;
        }

        CanvasRequestInterceptor(Context context, boolean addPerPageQueryParam, boolean isForcedCache, boolean shouldIgnoreToken){
            this.context = context;
            this.addPerPageQueryParam = addPerPageQueryParam;
            this.isForcedCache = isForcedCache;
            this.shouldIgnoreToken = shouldIgnoreToken;
        }
        @Override
        public void intercept(RequestFacade requestFacade) {

            RetrofitCounter.increment();

            final String token = APIHelpers.getToken(context);
            final String userAgent = APIHelpers.getUserAgent(context);
            final String domain = APIHelpers.loadProtocol(context) + "://" + APIHelpers.getDomain(context);

            //Set the UserAgent
            if(userAgent != null && !userAgent.equals(""))
                requestFacade.addHeader("User-Agent", userAgent);

            //Authenticate if possible
            if(!shouldIgnoreToken && token != null && !token.equals("")){
                requestFacade.addHeader("Authorization", "Bearer " + token);
            }

            if (isForcedCache) {
                requestFacade.addHeader("Cache-Control", "only-if-cached");
            } else {
                requestFacade.addHeader("Cache-Control", "no-cache");
            }
            //HTTP referer (originally a misspelling of referrer) is an HTTP header field that identifies the address of the webpage that linked to the resource being requested
            //Source: https://en.wikipedia.org/wiki/HTTP_referer
            //Some schools use an LTI tool called SlideShare that whitelists domains to be able to inject content into assignments
            //They check the referrer in order to do this. 	203
            requestFacade.addHeader("Referer", domain);

            //Masquerade if necessary
            if (Masquerading.isMasquerading(context)) {
                requestFacade.addQueryParam("as_user_id", Long.toString(Masquerading.getMasqueradingId(context)));
            }

            if(addPerPageQueryParam) {
                //Sets the per_page count so we can get back more items with less round-trip calls.
                requestFacade.addQueryParam("per_page", Integer.toString(numberOfItemsPerPage));
            }

            //Add Accept-Language header for a11y
            requestFacade.addHeader("accept-language", getAcceptedLanguageString());
        }
    }

    public static String getLocale() {
        // This is kinda gross, but Android is terrible and doesn't use the standard for lang strings...
        return Locale.getDefault().toString().replace("_", "-");
    }

    public static String getAcceptedLanguageString() {
        return getLocale() + "," + Locale.getDefault().getLanguage();
    }

    public static String getSessionLocaleString() {
        String lang = getLocale();

        // Canvas supports Chinese (Traditional) and Chinese (Simplified)
        if (lang.equalsIgnoreCase("zh-hk") || lang.equalsIgnoreCase("zh-tw") || lang.equalsIgnoreCase("zh-hant-hk") || lang.equalsIgnoreCase("zh-hant-tw")) {
            lang = "zh-Hant";
        } else if (lang.equalsIgnoreCase("zh") || lang.equalsIgnoreCase("zh-cn") || lang.equalsIgnoreCase("zh-hans-cn")) {
            lang = "zh-Hans";
        } else if (!lang.equalsIgnoreCase("pt-BR") && !lang.equalsIgnoreCase("en-AU") && !lang.equalsIgnoreCase("en-GB")) {
            // Canvas only supports 3 region tags (not including Chinese), remove any other tags
            lang = Locale.getDefault().getLanguage();
        }

        return "?session_locale=" + lang;
    }

    public static boolean isNetworkAvaliable(Context context) {
        ConnectivityManager connectivity =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * set a new default for the number of items returned per page.
     *
     * @param itemsPerPage
     */
    public static void setDefaultNumberOfItemsPerPage(int itemsPerPage) {
        if(itemsPerPage > 0){
            numberOfItemsPerPage = itemsPerPage;
        }
    }

    /**
     * Gets our custom GSON parser.
     *
     * @return Our custom GSON parser with custom deserializers.
     */

    public static Gson getGSONParser(){
        GsonBuilder b = new GsonBuilder();
        //TODO:Register custom parsers here!
        return b.create();
    }

    /**
     * Sets up the CanvasRestAdapter.
     *
     * Short hand for setdomain, setToken, and setProtocol.
     *
     * Clears out any old data before setting the new data.
     *
     * @param context An Android context.
     * @param token An OAuth2 Token
     * @param domain The domain for the signed in user.
     * @param itemsPerPage The number of items to return per page. Default is 30.
     * @return Whether or not the instance was setup. Only returns false if the data is empty or invalid.
     */
    public static boolean setupInstance(Context context, String token, String domain, int itemsPerPage){
        setDefaultNumberOfItemsPerPage(itemsPerPage);
        return setupInstance(context,token,domain);
    }

    /**
     * Sets up the CanvasRestAdapter.
     *
     * Short hand for setdomain, setToken, and setProtocol.
     *
     * Clears out any old data before setting the new data.
     *
     * @param context An Android context.
     * @param token An OAuth2 Token
     * @param domain The domain for the signed in user.
     *
     * @return Whether or not the instance was setup. Only returns false if the data is empty or invalid.
     */
    public static boolean setupInstance(Context context, String token, String domain){
        if (token == null ||
                token.equals("") ||
                domain == null) {

            return false;
        }

        String protocol = "https";
        if(domain.startsWith("http://")) {
            protocol = "http";
        }
        return (APIHelpers.setDomain(context, domain) && APIHelpers.setToken(context, token) && APIHelpers.setProtocol(protocol, context));
    }
}