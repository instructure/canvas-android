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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.NotoriousConfig
import com.instructure.canvasapi2.models.NotoriousSession
import com.instructure.canvasapi2.models.notorious.NotoriousResultWrapper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.Logger
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


internal object NotoriousAPI {

    // Interface talking to Canvas servers
    private interface NotoriousConfigurationInterface {
        @GET("services/kaltura")
        fun getConfiguration(): Call<NotoriousConfig>

        @POST("services/kaltura_session")
        fun startSession(): Call<NotoriousSession>
    }

    // Interface talking to Notorious servers
    private interface NotoriousInterface {
        @POST("index.php?service=uploadtoken&action=add")
        fun getUploadToken(@Query("ks") sessionToken: String): Call<NotoriousResultWrapper>

        @Multipart
        @POST("index.php?service=uploadtoken&action=upload")
        fun uploadFile(
                @Part("ks") sessionTokenPart: RequestBody,
                @Part("uploadTokenId") uploadTokenPart: RequestBody,
                @Part filePart: MultipartBody.Part): Call<Void>

        @POST("index.php?service=media&action=addFromUploadedFile")
        fun getMediaId(
                @Query("ks") sessionToken: String,
                @Query("uploadTokenId") uploadToken: String,
                @Query("mediaEntry:name") name: String,
                @Query("mediaEntry:mediaType") mediaType: String): Call<NotoriousResultWrapper>
    }

    fun getConfiguration(adapter: RestBuilder, params: RestParams, callback: StatusCallback<NotoriousConfig>) {
        callback.addCall(adapter.build(NotoriousConfigurationInterface::class.java, params).getConfiguration()).enqueue(callback)
    }

    fun startSession(adapter: RestBuilder, params: RestParams, callback: StatusCallback<NotoriousSession>) {
        callback.addCall(adapter.build(NotoriousConfigurationInterface::class.java, params).startSession()).enqueue(callback)
    }

    fun getUploadToken(adapter: RestBuilder, callback: StatusCallback<NotoriousResultWrapper>) {
        callback.addCall(adapter.buildNotorious(NotoriousInterface::class.java).getUploadToken(ApiPrefs.notoriousToken)).enqueue(callback)
    }

    fun uploadFileSynchronous(notoriousToken: String, uploadToken: String, filePart: MultipartBody.Part, adapter: RestBuilder): Response<Void>? {
        return try {
            val notoriousTokenPart = notoriousToken.toRequestBody("text/plain".toMediaTypeOrNull())
            val uploadTokenPart = uploadToken.toRequestBody("text/plain/".toMediaTypeOrNull())
            adapter.buildNotorious(NotoriousInterface::class.java).uploadFile(notoriousTokenPart, uploadTokenPart, filePart).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun getMediaIdSynchronous(notoriousToken: String, uploadToken: String, fileName: String, mimetype: String, adapter: RestBuilder): NotoriousResultWrapper? {
        return try {
            val mediaTypeConverted = FileUtils.notoriousCodeFromMimeType(mimetype)
            val response = adapter.buildNotorious(NotoriousInterface::class.java).getMediaId(notoriousToken, uploadToken, fileName, mediaTypeConverted).execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Logger.e(e.message)
            null
        }
    }

}
