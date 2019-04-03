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

package com.instructure.canvasapi.api;

import android.content.Context;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.Profiler;
import retrofit.RestAdapter;

public class BuildInterfaceAPI {

    private static RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;

    public static <T> T buildCacheInterface(Class<T> clazz, CanvasCallback callback, CanvasContext canvasContext, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, canvasContext, true, perPageQueryParam);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheInterface(Class<T> clazz, CanvasCallback callback, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, null, true, perPageQueryParam);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheInterface(Class<T> clazz, String domain, CanvasCallback callback, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, domain, null, true, perPageQueryParam);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheGenericInterface(Class<T> clazz, final Context context, String domain, boolean perPageQueryParam, boolean shouldIgnoreToken) {
        RestAdapter restAdapter = CanvasRestAdapter.buildGenericAdapter(context, domain, true, perPageQueryParam, shouldIgnoreToken);
        return restAdapter.create(clazz);
    }


    /**
     * Creates a rest adapter that will only read from the cache.
     */
    public static <T> T buildCacheInterface(Class<T> clazz, CanvasCallback callback, CanvasContext canvasContext) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, true, canvasContext);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheInterface(Class<T> clazz, Context context, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context, true, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheInterface(Class<T> clazz, CanvasCallback callback) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, true, null);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildCacheInterface(Class<T> clazz, String domain, CanvasCallback callback) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, domain, true, null);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }
    public static <T> T buildInterface(Class<T> clazz, CanvasCallback callback, CanvasContext canvasContext) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, canvasContext);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, CanvasCallback callback, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, null, false, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, String domain, CanvasCallback callback, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, domain, null, false, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterfaceNoRedirects(Class<T> clazz, String domain, CanvasCallback callback, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapterNoRedirects(callback, domain, null, false, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, CanvasCallback callback, CanvasContext canvasContext, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback, canvasContext, false, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, Context context) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, Context context, boolean perPageQueryParam) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context, perPageQueryParam);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, final Context context, CanvasContext canvasContext) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context, canvasContext);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, CanvasCallback callback) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(callback);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildInterface(Class<T> clazz, String domain, CanvasCallback callback) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(domain, callback);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildGenericInterface(Class<T> clazz, final Context context, String domain, boolean perPageQueryParam, boolean shouldIgnoreToken) {
        RestAdapter restAdapter = CanvasRestAdapter.buildGenericAdapter(context, domain, false, perPageQueryParam, shouldIgnoreToken);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildUploadInterface(Class<T> clazz, String hostURL) {
        RestAdapter restAdapter = CanvasRestAdapter.getGenericHostAdapter(hostURL);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildUploadInterfaceXML(Class<T> clazz, Context context, String hostURL) {
        RestAdapter restAdapter = CanvasRestAdapter.getGenericHostAdapterXML(context, hostURL);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

    public static <T> T buildPingInterface(Class<T> clazz, String hostURL, Profiler profiler) {
        RestAdapter restAdapter = CanvasRestAdapter.buildPingRestAdapter(hostURL, profiler);
        restAdapter.setLogLevel(LOG_LEVEL);
        return restAdapter.create(clazz);
    }

}
