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

package com.instructure.canvasapi2.builders

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger

import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory


class RestBuilder(callback: StatusCallback<*> = object : StatusCallback<Any>(){}, authUser: String? = null) : CanvasRestAdapter(callback, authUser) {

    fun <T> build(clazz: Class<T>, params: RestParams): T {
        val restParams = params.copy(isForceReadFromCache = false)
        val restAdapter = if (isPact) buildAdapterForTest(restParams) else buildAdapter(restParams)

        return restAdapter.create(clazz)
    }

    fun <T> buildNotorious(clazz: Class<T>): T {
        return Retrofit.Builder()
                .baseUrl(ApiPrefs.fullNotoriousDomain + "/api_v3/")
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(Persister(AnnotationStrategy())))
                .client(CanvasRestAdapter.okHttpClient)
                .build()
                .create(clazz)
    }

    fun <T> buildSerializeNulls(clazz: Class<T>, params: RestParams): T {
        val restParams = params.copy(isForceReadFromCache = false)
        val restAdapter = buildAdapterSerializeNulls(restParams)

        return restAdapter.create(clazz)
    }

    fun <T> buildNoRedirects(clazz: Class<T>, params: RestParams): T {
        val restParams = params.copy(isForceReadFromCache = false)
        val restAdapter = buildAdapterNoRedirects(restParams)

        return restAdapter.create(clazz)
    }

    fun <T> buildUpload(clazz: Class<T>, params: RestParams): T {
        val restParams = params.copy(isForceReadFromCache = false)
        val restAdapter = buildAdapterUpload(restParams)

        return restAdapter.create(clazz)
    }

    fun <T> buildRollCall(clazz: Class<T>, params: RestParams): T {
        val restAdapter = buildRollCallAdapter(params.domain ?: "")
        return restAdapter.create(clazz)
    }

    companion object {
        var isPact: Boolean = false

        fun clearCacheDirectory(): Boolean {
            return try {
                cacheDirectory.delete()
            } catch (e: Exception) {
                Logger.e("Could not delete cache $e")
                false
            }
        }
    }
}
