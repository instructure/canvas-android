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
import android.os.AsyncTask;
import android.util.Log;

import com.instructure.canvasapi.api.BuildInterfaceAPI;
import com.instructure.canvasapi.model.CanvasError;

import java.io.Serializable;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * CanvasCallback is a parameterized class that handles pagination and caching automatically.
 */
public abstract class CanvasCallback<T> implements Callback<T> {

    protected APIStatusDelegate statusDelegate;

    // Controls whether of not the cache callbacks are called. Useful for pull-to-refresh, where the cache results should be ignored
    protected APICacheStatusDelegate cacheStatusDelegate;

    private String cacheFileName;
    private boolean isNextPage = false;
    private boolean isCancelled = false;
    private boolean isFinished = true;
    private boolean hasReadFromCache = false;

    public static ErrorDelegate defaultErrorDelegate;
    private ErrorDelegate errorDelegate;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public boolean hasReadFromCache(){
        return hasReadFromCache;
    }

    public void setHasReadFromCache(boolean hasReadFromCache){
        this.hasReadFromCache = hasReadFromCache;
    }

    public APIStatusDelegate getStatusDelegate() {
        return statusDelegate;
    }

    public APICacheStatusDelegate getCacheStatusDelegate() {
        return cacheStatusDelegate;
    }

    /**
     * setIsNextPage sets whether you're on the NextPages (2 or more) of pagination.
     * @param nextPage
     */
    public void setIsNextPage(boolean nextPage){
        isNextPage = nextPage;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param statusDelegate Delegate to get the context
     */
    public CanvasCallback(APIStatusDelegate statusDelegate) {
        setupDelegates(statusDelegate, null);
    }

    /**
     * Overload constructor to override default error delegate.
     */
    public CanvasCallback(APIStatusDelegate statusDelegate, ErrorDelegate errorDelegate){
        setupDelegates(statusDelegate, errorDelegate);
    }

    private void setupDelegates(APIStatusDelegate statusDelegate, ErrorDelegate errorDelegate) {
        this.statusDelegate = statusDelegate;

        if (statusDelegate instanceof APICacheStatusDelegate) {
            this.cacheStatusDelegate = (APICacheStatusDelegate) statusDelegate;
        }


        if (errorDelegate == null) {
            this.errorDelegate = getDefaultErrorDelegate(statusDelegate.getContext());
        } else {
            this.errorDelegate = errorDelegate;
        }

        if(this.errorDelegate == null){
            Log.e(APIHelpers.LOG_TAG, "WARNING: No ErrorDelegate Set.");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the default Error Delegate
     * @param context
     * @return
     */
    public static ErrorDelegate getDefaultErrorDelegate(Context context){
        if(defaultErrorDelegate == null ){
            String defaultErrorDelegateClass = APIHelpers.getDefaultErrorDelegateClass(context);

            if(defaultErrorDelegateClass != null){
                try {
                    Class<?> errorDelegateClass = (Class.forName(defaultErrorDelegateClass));
                    defaultErrorDelegate = (ErrorDelegate) errorDelegateClass.newInstance();
                } catch (Exception E) {
                    Log.e(APIHelpers.LOG_TAG,"WARNING: Invalid defaultErrorDelegateClass Set: "+defaultErrorDelegateClass);
                }
            }
        }

        return defaultErrorDelegate;
    }

    private void finishLoading() {
        isFinished = true;
        statusDelegate.onCallbackFinished(SOURCE.API);
    }

    /**
     * @return Current context, can be null
     */
    public Context getContext(){
        return statusDelegate.getContext();
    }

    /**
     * setShouldCache sets whether or not a call should be cached and the filename where it'll be cached to.
     * Should only be called by the API
     */
    @Deprecated
    public void setShouldCache(String fileName) {
        cacheFileName = fileName;
    }

    /**
     * shouldCache is a helper for whether or not a cacheFileName has been set.
     * @return
     */
    @Deprecated
    public boolean shouldCache() {
        return cacheFileName != null;
    }

    /**
     * Intended to work as AsyncTask.cancel() does.
     * The network call is still made, but no response is made.
     *
     * Gotchas:
     *       Cache is still called.
     *       The callback has to be reinitialized as you can't 'uncancel'
     */
    public void cancel(){
        isCancelled = true;
    }

    /**
     * readFromCache reads from the cache filename and simultaneously sets the cache filename
     * Use {@link BuildInterfaceAPI#buildCacheInterface} instead
     * @param path
     */
    @Deprecated
    public void readFromCache(String path) {
        new ReadCacheData().execute(path);
    }

    @Deprecated
    public boolean deleteCache(){
        return FileUtilities.DeleteFile(getContext(), cacheFileName);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface
    ///////////////////////////////////////////////////////////////////////////

    /**
     * cache is a function you can override to get the cached values.
     * @param t
     */
    @Deprecated
    public void cache(T t) {

    }

    public void cache(T t, LinkHeaders linkHeaders, Response response) {
        firstPage(t, linkHeaders, response);
    }

    /**
     * firstPage is the first (or only in some cases) of the API response.
     * @param t
     * @param linkHeaders
     * @param response
     */
    public abstract void firstPage(T t, LinkHeaders linkHeaders, Response response);

    /**
     *
     * nextPage is the second (or more) page of the API response.
     * Defaults to calling firstPage
     * Override if you want to change this functionality
     * @param t
     * @param linkHeaders
     * @param response
     */
    public void nextPage(T t, LinkHeaders linkHeaders, Response response){
        firstPage(t, linkHeaders, response);
    }

    /**
     * onFailure is a way to handle a failure instead using the
     * default error handling
     * @param retrofitError
     * @return true if the failure was handled, false otherwise
     */
    public boolean onFailure(RetrofitError retrofitError) {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Retrofit callback methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If you want caching and pagination, you must call this function using super or leave it alone..
     * @param t
     * @param response
     */
    @Override
    public void success(T t, Response response) {
        RetrofitCounter.decrement();

        // check if it's been cancelled or detached
        Log.d("URL_STATUS", APIHelpers.isCachedResponse(response) ?  "From cache " +  response.getUrl() : "From API "  + response.getUrl());
        if(isCancelled || t == null || getContext() == null) {
            return;
        }

        new CacheData(t, response).execute(t);
    }

    /**
     * failure calls the correct method on the ErrorDelegate that's been set.
     * @param retrofitError
     */
    @Override
    public void failure(RetrofitError retrofitError) {
        RetrofitCounter.decrement();

        // check if it's cancelled or detached
        if (isCancelled || getContext() == null) {
            return;
        }

        finishLoading();

        Log.e(APIHelpers.LOG_TAG, "ERROR: " + retrofitError.getUrl());
        Log.e(APIHelpers.LOG_TAG, "ERROR: " + retrofitError.getMessage());

        // Return if the failure was already handled
        if (onFailure(retrofitError)) {
            return;
        }

        if (errorDelegate == null) {
            Log.d(APIHelpers.LOG_TAG, "WARNING: No ErrorDelegate Provided ");
            return;
        }

        CanvasError canvasError;
        switch (retrofitError.getKind()) {
            case CONVERSION:
                canvasError = CanvasError.createError("Conversion Error", "An exception was thrown while (de)serializing a body");
                errorDelegate.generalError(retrofitError, canvasError, getContext());
                break;
            case HTTP:
                // A non-200 HTTP status code was received from the server.
                handleHTTPError(retrofitError);
                break;
            case NETWORK:
                // An IOException occurred while communicating to the server.
                statusDelegate.onNoNetwork();
                errorDelegate.noNetworkError(retrofitError, getContext());
                break;
            case UNEXPECTED:
                canvasError = CanvasError.createError("Unexpected Error", "An internal error occurred while attempting to execute a request.");
                errorDelegate.generalError(retrofitError, canvasError, getContext());
                break;
            default:
                canvasError = CanvasError.createError("Unexpected Error", "An unexpected error occurred.");
                errorDelegate.generalError(retrofitError, canvasError, getContext());
                break;
        }
    }

    private void handleHTTPError(RetrofitError retrofitError) {
        Response response = retrofitError.getResponse();
        if (response == null) {
            return;
        }
        Log.e(APIHelpers.LOG_TAG, "Response code: " + response.getStatus());
        Log.e(APIHelpers.LOG_TAG, "Response body: " + response.getBody());

        CanvasError canvasError = null;
        try {
            canvasError = (CanvasError) retrofitError.getBodyAs(CanvasError.class);
        } catch (Exception exception) {
        }

        if (response.getStatus() == 200) {
            errorDelegate.generalError(retrofitError, canvasError, getContext());
        } else if (response.getStatus() == 401) {
            errorDelegate.notAuthorizedError(retrofitError, canvasError, getContext());
        } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
            errorDelegate.invalidUrlError(retrofitError, getContext());
        } else if (response.getStatus() >= 500 && response.getStatus() < 600) {
            //don't do anything for a 504 (Unsatisfiable Request (only-if-cached)).
            //It will happen when we try to read from the http cache and there isn't
            //anything there
            if (response.getStatus() == 504 && APIHelpers.isCachedResponse(response)) {
                if (!CanvasRestAdapter.isNetworkAvaliable(getContext())) { // Purposely not part of the above if statement. First if statement is prevent the error delegate from a 504 cache response
                    statusDelegate.onNoNetwork(); // Only call when no items were cached and there isn't a network
                }
                // do nothing
            } else {
                errorDelegate.serverError(retrofitError, getContext());
            }
        }
    }

    public enum SOURCE{
        API, CACHE;

        public boolean isAPI(){
            return this == API;
        }

        public boolean isCache(){
            return this == CACHE;
        }
    }

    private class CacheData extends AsyncTask<T, Void, LinkHeaders> {

        private T t;
        private Response response;

        public CacheData(T t, Response response) {
            this.t = t;
            this.response = response;
        }

        @Override
        protected LinkHeaders doInBackground(T... params) {
            LinkHeaders linkHeaders = APIHelpers.parseLinkHeaderResponse(getContext(), response.getHeaders());

            if (shouldCache() && !isNextPage && getContext() != null) {
                if(t instanceof Serializable) {
                    try {
                        FileUtilities.SerializableToFile(getContext(), cacheFileName, (Serializable)params[0]);
                    } catch (Exception E) {
                        Log.e(APIHelpers.LOG_TAG, "Could not cache serializable: " + E);
                    }
                }
            }

            return linkHeaders;
        }

        @Override
        protected void onPostExecute(LinkHeaders linkHeaders) {
            super.onPostExecute(linkHeaders);
            if (isCancelled) {
                return;
            }
            boolean isCache = APIHelpers.isCachedResponse(response);
            boolean isIgnoreCache = false;

            if (cacheStatusDelegate != null) {
                isIgnoreCache = cacheStatusDelegate.shouldIgnoreCache();
            }

            if (isCache && !isIgnoreCache) {
                Log.v(APIHelpers.LOG_TAG, "Cache");
                cache(t);
                cache(t, linkHeaders, response);
                statusDelegate.onCallbackFinished(SOURCE.CACHE);
            } else if (isNextPage) {
                nextPage(t, linkHeaders, response);
                statusDelegate.onCallbackFinished(SOURCE.API);
            } else if (!isCache) {
                firstPage(t, linkHeaders, response);
                statusDelegate.onCallbackFinished(SOURCE.API);

                // since we have had a successful network call, reset the variable that tracks whether the user has seen the
                // no network error
                if (getContext() != null) {
                    APIHelpers.setHasSeenNetworkErrorMessage(getContext(), false);
                }
            }

            isFinished = true;
        }
    }

    private class ReadCacheData extends AsyncTask<String, Void, Serializable> {

        private String path = null;

        @Override
        protected Serializable doInBackground(String... params) {
            path = params[0];
            try {
                return FileUtilities.FileToSerializable(getContext(), path);
            } catch (Exception E) {
                Log.e(APIHelpers.LOG_TAG, "NO CACHE: " + path);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Serializable serializable) {
            super.onPostExecute(serializable);

            if (serializable != null && getContext() != null) {
                cache((T) serializable);
            }

            setHasReadFromCache(true);
            setShouldCache(path);
            statusDelegate.onCallbackFinished(SOURCE.CACHE);
        }
    }
}
